package kr.elara.android.framework.provider;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import kr.elara.android.framework.provider.annotation.MimeType;
import kr.elara.android.framework.provider.annotation.UriPath;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractContentProvider extends ContentProvider {

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Map<String, String> mProjectionMap = Collections.emptyMap();
    private Map<Integer, Class<? extends Entity>> mCodeEntitiesMap = Collections.emptyMap();

    @Override
    public boolean onCreate() {
        initUriMatcher();
        return false;
    }

    private void initUriMatcher() {
        String authority = getProperty().getAuthority();
        int code = 1;
        List<Class<? extends Entity>> entities = getEntityHolder().getEntities();

        mCodeEntitiesMap = new HashMap<Integer, Class<? extends Entity>>((int) (entities.size() / 0.75 + 1));

        for (Class<? extends Entity> entity : entities) {
            for (String uriPath : getAllUriPaths(entity)) {
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

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
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
