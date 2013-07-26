package kr.elara.android.framework.provider;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;

public interface DatabaseUpdateStrategy {

    void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion);

}
