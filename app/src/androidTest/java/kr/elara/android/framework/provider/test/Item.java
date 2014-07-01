package kr.elara.android.framework.provider.test;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import kr.elara.android.framework.provider.Entity;
import kr.elara.android.framework.provider.annotation.MimeType;
import kr.elara.android.framework.provider.annotation.UriPath;

import java.util.Date;

@DatabaseTable(tableName = "item")
@UriPath({ContentContract.Item.ITEMS, ContentContract.Item.ITEM_ID})
@MimeType("item")
public class Item implements Entity{

    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String NUMBER = "number";
    public static final String DATE = "date";

    @DatabaseField(columnName = _ID, generatedId = true)
    private long id;

    @DatabaseField(columnName = TITLE)
    private String mTitle;

    @DatabaseField(columnName = DESCRIPTION)
    private String mDescription;

    @DatabaseField(columnName = NUMBER)
    private int mNum;

    public Item() {

    }

    public Item(String title, String description, int num, Date date) {
        mTitle = title;
        mDescription = description;
        mNum = num;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setNumber(int num) {
        mNum = num;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getNumber() {
        return mNum;
    }
}
