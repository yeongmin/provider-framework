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

    private static String[] getAllUriPaths(Class<? extends Entity> entity) {
        String[] result = new String[0];
        UriPath uriPath = entity.getAnnotation(UriPath.class);

        if (uriPath != null) {
            result = uriPath.value();
        }

        return result;
    }

    @Override
    public String getType(Uri uri) {
        validateUri(uri);

        String type = isSingleRow(uri) ? ContentResolver.CURSOR_ITEM_BASE_TYPE : ContentResolver.CURSOR_DIR_BASE_TYPE;

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

    private static boolean isSingleRow(Uri uri) {
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

    private String getTable(Uri uri) {
        DatabaseTable table = getEntity(uri).getAnnotation(DatabaseTable.class);

        String result = "";
        if (table != null) {
            result = table.tableName();
        }

        return result;
    }

    private void validateUri(Uri uri) {
        if (mUriMatcher.match(uri) == -1) {
            throw new IllegalArgumentException("Unknown Uri : " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query - uri : " + uri);
        validateUri(uri);

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

        Log.queryLog(LOG_TAG, getTable(uri), projection, selection, selectionArgs, orderBy);
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
        validateUri(uri);

        ContentValues values = (contentValues != null) ? new ContentValues(contentValues) : new ContentValues();

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        long rowId = db.insert(getTable(uri), null, values);

        if (rowId > 0) {
            Uri uriResult = ContentUris.withAppendedId(uri, rowId);
            // TODO : Log SQL statement.
            getContext().getContentResolver().notifyChange(uriResult, null);
            return uriResult;
        } else {
            throw new SQLiteException();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete : uri - " + uri);
        validateUri(uri);

        String reSelection = rewriteSelection(uri, selection);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count = db.delete(getTable(uri), reSelection, selectionArgs);

        Log.deleteLog(LOG_TAG, getTable(uri), selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "update : uri - " + uri);
        validateUri(uri);

        String reSelection = rewriteSelection(uri, selection);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count = db.update(getTable(uri), contentValues, reSelection, selectionArgs);

        // TODO : Log SQL statement.
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static String rewriteSelection(Uri uri, String selection) {
        String result = selection;

        if (isSingleRow(uri)) {
            result = Entity._ID + " = " + uri.getLastPathSegment();
            if (!TextUtils.isEmpty(selection)) {
                result = result + " AND " + selection;
            }
        }
        return result;
    }

    protected abstract Property getProperty();

    protected abstract EntityHolder getEntityHolder();
}
