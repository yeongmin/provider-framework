package kr.elara.android.framework.provider;

import android.provider.BaseColumns;
import com.j256.ormlite.field.DatabaseField;

/**
 * Model classes which represent tables should implement Entity.
 * <p/>
 * A class which implements this should have no argument constructor.
 */
public interface Entity extends BaseColumns {

    // TODO : find how to force _ID for subclasses.
    @DatabaseField(columnName = _ID, generatedId = true)
    long mId = -1;

}
