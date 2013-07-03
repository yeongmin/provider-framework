package kr.elara.android.framework.provider;

import com.j256.ormlite.field.DatabaseField;

public interface Entity {

    @DatabaseField(columnName = "_ID", generatedId = true)
    long mId = -1;

}
