package kr.elara.android.framework.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import kr.elara.android.framework.provider.util.Log;

import java.sql.SQLException;
import java.util.List;

class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();
    private final List<Class<? extends Entity>> mEntities;
    private final DatabaseUpdateStrategy mUpdateStrategy;

    public DatabaseHelper(Context context, String databaseName, int databaseVersion,
                          List<Class<? extends Entity>> entities, DatabaseUpdateStrategy updateStrategy) {
        super(context, databaseName, null, databaseVersion);
        if (entities == null) {
            throw new IllegalArgumentException("entities cannot be null");
        }
        mEntities = entities;
        mUpdateStrategy = updateStrategy;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.d(LOG_TAG, "onCreate()");
        for (Class<? extends Entity> entity : mEntities) {
            try {
                Log.d(LOG_TAG, "Creating db table for : " + entity.getSimpleName());
                TableUtils.createTable(connectionSource, entity);
            } catch (SQLException e) {
                Log.d(LOG_TAG, "Exception occurred during creating db table for : " + entity.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (mUpdateStrategy != null) {
            mUpdateStrategy.onUpgrade(database, connectionSource, oldVersion, newVersion);
        } else {
            Log.d(LOG_TAG, "UpdateStrategy is null. No database update process.");
        }
    }
}
