package kr.elara.android.framework.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.j256.ormlite.table.DatabaseTable;
import kr.elara.android.framework.provider.annotation.MimeType;
import kr.elara.android.framework.provider.annotation.UriPath;
import kr.elara.android.framework.provider.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractContentProvider extends ContentProvider {

    private static final String LOG_TAG = AbstractContentProvider.class.getSimpleName();

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Map<String, String> mProjectionMap = Collections.emptyMap();
    private Map<Integer, Class<? extends Entity>> mCodeEntitiesMap = Collections.emptyMap();
    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext(), getProperty().getDatabaseName(),
                getProperty().getDatabaseVersion(),
                getEntityHolder().getEntities(), getProperty().getDatabaseUpdateStrategy());
        initUriMatcher();
        return false;
    }

    private void initUriMatcher() {
        String authority = getProperty().getAuthority();
        int code = 1;
        List<Class<? extends Entity>> entities = getEntityHolder().getEntities();

        mCodeEntitiesMap = new HashMap<Integer, Class<? extends Entity>>((int) (entities.size() / 0.75 + 1));

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

        Class<? extends Entity> entity = mCodeEntitiesMap.get(mUriMatcher.match(uri));
        MimeType mimeType = entity.getAnnotation(MimeType.class);

        if (mimeType != null) {
            result = mimeType.value();
        }

        return result;
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

        //TODO : extract this with getMimeType
        Class<? extends Entity> entity = mCodeEntitiesMap.get(mUriMatcher.match(uri));
        DatabaseTable table = entity.getAnnotation(DatabaseTable.class);

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
        Log.d(LOG_TAG, "query() - uri : " + uri + "\ntable : " + getTable(uri));

        checkUri(uri);
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(getTable(uri));

        // TODO : for multiple items in else {}
        if (isSingleRow(uri)) {
            qBuilder.appendWhere(Entity._ID + "=" + uri.getLastPathSegment());
        }

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = qBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        checkUri(uri);
        ContentValues values = (contentValues != null) ? new ContentValues(contentValues) : new ContentValues();

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        long rowId = db.insert(getTable(uri), null, values);

        if (rowId > 0) {
            Uri uriBase = getUri(uri);
            Uri uriResult = ContentUris.withAppendedId(uriBase, rowId);
            getContext().getContentResolver().notifyChange(uriResult, null);
            return uriResult;
        } else {
            // TODO : throw SQLException
            throw new IllegalStateException("Insert fail" + uri);
        }
    }

    private Uri getUri(Uri uri) {
        //TODO : check this is right or not.
        String baseUri = uri.getLastPathSegment();
        String uriString = ContentResolver.SCHEME_CONTENT + "://" + getProperty().getAuthority() + "/" +
                baseUri + "/";
        return Uri.parse(uriString);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    protected abstract Property getProperty();

    protected abstract EntityHolder getEntityHolder();
}
