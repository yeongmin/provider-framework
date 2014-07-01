package kr.elara.android.framework.provider;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Interface to manage database version.
 */
public interface DatabaseUpdateStrategy {

    /**
     * Called when the database needs to be upgraded. The implementation should use this method to drop tables,
     * add tables, or do anything else it needs to upgrade to the new schema version.
     * <p/>
     * See {@link com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, com.j256.ormlite.support.ConnectionSource, int, int)}
     */
    void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion);

}
