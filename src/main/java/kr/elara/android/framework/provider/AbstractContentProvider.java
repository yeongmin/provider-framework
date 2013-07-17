package kr.elara.android.framework.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import kr.elara.android.framework.provider.annotation.DefaultSortOrder;
import kr.elara.android.framework.provider.annotation.MimeType;
import kr.elara.android.framework.provider.annotation.UriPath;
import kr.elara.android.framework.provider.util.Log;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractContentProvider extends ContentProvider {

    private static final String LOG_TAG = AbstractContentProvider.class.getSimpleName();

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Map<Integer, Class<? extends Entity>> mCodeEntitiesMap = new HashMap<Integer, Class<? extends Entity>>();
    private Map<Class<? extends Entity>, Map<String, String>> mEntityProjectionMap;
    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext(), getProperty().getDatabaseName(),
                getProperty().getDatabaseVersion(),
                getEntityHolder().getEntities(), getProperty().getDatabaseUpdateStrategy());
        initUriMatcher();
        initProjectionMap();
        return false;
    }

    private void initUriMatcher() {
        String authority = getProperty().getAuthority();
        int code = 1;
        List<Class<? extends Entity>> entities = getEntityHolder().getEntities();

        for (Class<? extends Entity> entity : entities) {
            Log.d(LOG_TAG, entity.getSimpleName() + " : ");
            for (String uriPath : getAllUriPaths(entity)) {
                Log.d(LOG_TAG, "add to uri matcher - authority : " + authority + ", uriPath : " + uriPath + ", " +
                        "code : " + code);
                mUriMatcher.addURI(authority, uriPath, code);
                mCodeEntitiesMap.put(code, entity);
                code++;
            }
        }
    }

    private void initProjectionMap() {
        List<Class<? extends Entity>> entities = getEntityHolder().getEntities();
        mEntityProjectionMap = new HashMap<Class<? extends Entity>, Map<String, String>>(
                (int) (entities.size() / 0.75 + 1));

        for (Class<? extends Entity> entity : entities) {
            Log.d(LOG_TAG, "create ProjectionMap for " + entity.getSimpleName());
            mEntityProjectionMap.put(entity, getProjectionMap(entity));
        }
    }

    private Map<String, String> getProjectionMap(Class<? extends Entity> entity) {

        // To get DatabaseField from entity, first obtain DatabaseTableConfig.
        DatabaseTableConfig<? extends Entity> tableConfig;
        try {
            tableConfig = DatabaseTableConfigUtil.fromClass
                    (mDatabaseHelper.getConnectionSource(), entity);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Cannot get projectionMap from " + entity.getSimpleName());
            return Collections.emptyMap();
        }

        List<DatabaseFieldConfig> fieldConfigs = tableConfig.getFieldConfigs();
        HashMap<String, String> projectionMap = new HashMap<String, String>((int) (fieldConfigs.size() / 0.75 +
                1));

        for (DatabaseFieldConfig fieldConfig : fieldConfigs) {
            projectionMap.put(fieldConfig.getColumnName(), fieldConfig.getColumnName());
            Log.d(LOG_TAG, "put column : \"" + fieldConfig.getColumnName() + "\" into projectionMap");
        }

        return projectionMap;
    }

    private String[] getAllUriPaths(Class<? extends Entity> entity) {
        String[] result = new String[0];
        UriPath uriPath = entity.getAnnotation(UriPath.class);

        if (uriPath != null) {
            result = uriPath.value();
        }

        return result;
    }

    @Override
    public String getType(Uri uri) {
        if (!isValidUri(uri)) {
            throw new IllegalArgumentException("Unknown Uri : " + uri);
        }
        String type = isSingleRow(uri) ? ContentResolver.CURSOR_DIR_BASE_TYPE : ContentResolver.CURSOR_ITEM_BASE_TYPE;

        return type + "/" + getMimeType(uri);
    }

    private String getMimeType(Uri uri) {
        String result = "";

        MimeType mimeType = getEntity(uri).getAnnotation(MimeType.class);

        if (mimeType != null) {
            result = mimeType.value();
        }

        return result;
    }

    private Class<? extends Entity> getEntity(Uri uri) {
        return mCodeEntitiesMap.get(mUriMatcher.match(uri));
    }

    private boolean isSingleRow(Uri uri) {
        long lastSegmentValue;
        try {
            lastSegmentValue = ContentUris.parseId(uri);
        } catch (NumberFormatException e) {
            return false;
        }

        if (lastSegmentValue == -1) {
            throw new UnsupportedOperationException("Path is empty : " + uri);
        }

        return true;
    }

    private boolean isValidUri(Uri uri) {
        return (mUriMatcher.match(uri) != -1);
    }

    private String getTable(Uri uri) {
        DatabaseTable table = getEntity(uri).getAnnotation(DatabaseTable.class);

        String result = "";
        if (table != null) {
            result = table.tableName();
        }

        return result;
    }

    private void checkUri(Uri uri) {
        if (mUriMatcher.match(uri) == -1) {
            throw new IllegalArgumentException("Unknown Uri : " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query - uri : " + uri);

        checkUri(uri);
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(getTable(uri));

        if (isSingleRow(uri)) {
            qBuilder.appendWhere(Entity._ID + "=" + uri.getLastPathSegment());
        }

        qBuilder.setProjectionMap(mEntityProjectionMap.get(getEntity(uri)));

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = getDefaultSortOrder(uri);
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = qBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);

        queryLog(getTable(uri), projection, selection, selectionArgs, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private String getDefaultSortOrder(Uri uri) {
        DefaultSortOrder sortOrder = getEntity(uri).getAnnotation(DefaultSortOrder.class);

        String result = "";
        if (sortOrder != null) {
            result = sortOrder.value();
        }
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(LOG_TAG, "insert : uri - " + uri);

        checkUri(uri);
        ContentValues values = (contentValues != null) ? new ContentValues(contentValues) : new ContentValues();

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        long rowId = db.insert(getTable(uri), null, values);

        if (rowId > 0) {
            Uri uriResult = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uriResult, null);
            return uriResult;
        } else {
            throw new SQLiteException();

        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete : uri - " + uri);

        checkUri(uri);
        String reSelection = selection;
        if (isSingleRow(uri)) {
            reSelection = Entity._ID + " = " + uri.getLastPathSegment();
            if (!TextUtils.isEmpty(selection)) {
                reSelection = reSelection + " AND " + selection;
            }
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count = db.delete(getTable(uri), reSelection, selectionArgs);

        deleteLog(getTable(uri), selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private static void deleteLog(String table, String selection, String[] selectionArgs) {
        if (Log.enabled) {
            Log.d(LOG_TAG, "DELETE FROM " + table + buildWhere(selection, selectionArgs));
        }
    }

    private static void queryLog(String table, String[] projection, String selection, String[] selectionArgs,
                                 String orderBy) {
        if (Log.enabled) {
            Log.d(LOG_TAG, "SELELCT " + serializeArgs(projection) + " FROM " + table + buildWhere(selection,
                    selectionArgs) + " ORDER BY " + orderBy);
        }
    }

    private static String buildWhere(String selection, String[] selectionArgs) {
        String where = "";
        if (!TextUtils.isEmpty(selection)) {
            where = " WHERE " + selection.replace("?", "") + serializeArgs(selectionArgs);
        }
        return where;
    }

    private static String serializeArgs(String[] selectionArgs) {
        // For Logging, build string for value of where clause.
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (String value : selectionArgs) {
            stringBuilder.append(value);
            stringBuilder.append(", ");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    protected abstract Property getProperty();

    protected abstract EntityHolder getEntityHolder();
}
