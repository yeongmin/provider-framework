package kr.elara.android.framework.provider;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;

/**
 * Model classes which are saved into db should implement Entity.
 */
public interface Entity extends BaseColumns{

    @DatabaseField(columnName = _ID, generatedId = true)
    long mId = -1;

}
