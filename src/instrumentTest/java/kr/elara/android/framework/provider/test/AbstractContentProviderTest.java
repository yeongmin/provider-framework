package kr.elara.android.framework.provider.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class AbstractContentProviderTest extends ProviderTestCase2<MockContentProvider> {

    private Uri mUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + MockContentProvider.AUTHORITY + "/" +
            ContentContract.Item.ITEMS);

    public AbstractContentProviderTest() {
        super(MockContentProvider.class, MockContentProvider.AUTHORITY);
    }

    public void testInsertSingleItem() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Item.TITLE, "title");
        contentValues.put(Item.DESCRIPTION, "description");
        contentValues.put(Item.NUMBER, 1);

        Uri result = getMockContentResolver().insert(mUri, contentValues);

        Uri expected = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + MockContentProvider.AUTHORITY + "/" +
                ContentContract.Item.ITEMS + "/" + 1);

        assertEquals(expected, result);
    }

    public void testInsertMultipleItems() {
        ContentValues[] values = new ContentValues[10];

        for (int i = 0; i < 10; ++i) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(Item.TITLE, "test " + i);
            contentValues.put(Item.DESCRIPTION, "des " + i);
            contentValues.put(Item.NUMBER, i);

            values[i] = contentValues;
        }

        int result = getMockContentResolver().bulkInsert(mUri, values);

        assertEquals(10, result);
    }
}
