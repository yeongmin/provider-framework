package kr.elara.android.framework.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final List<Class<? extends Entity>> mEntities;
    private DatabaseUpgrade mDatabaseUpgrade;

    public DatabaseHelper(Context context, String databaseName, int databaseVersion, List<Class<? extends Entity>> entities) {
        super(context, databaseName, null, databaseVersion);
        if (entities == null) {
            throw new IllegalArgumentException("entities cannot be null");
        }
        mEntities = entities;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        for (Class<? extends Entity> entity : mEntities) {
            try {
                TableUtils.createTable(connectionSource, entity);
            } catch (SQLException e) {
                e.printStackTrace();
                // TODO : Log and teardown.
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO : FIXME should be static way.
        if (mDatabaseUpgrade != null) {
            mDatabaseUpgrade.onUpgrade(database, connectionSource, oldVersion, newVersion);
        }
    }

    public void setmDatabaseUpgrade(DatabaseUpgrade mDatabaseUpgrade) {
        this.mDatabaseUpgrade = mDatabaseUpgrade;
    }

    // TODO : FIXME should be static way.
    // TODO : rename interface.
    public interface DatabaseUpgrade {
        void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion);
    }
}
