package kr.elara.android.framework.provider.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class AbstractContentProviderTest extends ProviderTestCase2<MockContentProvider> {

    private static final int STUB_ITEM_NUMBER = 10;
    private Uri mUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + MockContentProvider.AUTHORITY + "/" +
            ContentContract.Item.ITEMS);

    public AbstractContentProviderTest() {
        super(MockContentProvider.class, MockContentProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        insertStubItems();
    }

    public void testInsertSingleItem() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Item.TITLE, "title");
        contentValues.put(Item.DESCRIPTION, "description");
        contentValues.put(Item.NUMBER, 1);

        Uri result = getMockContentResolver().insert(mUri, contentValues);

        Uri expected = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + MockContentProvider.AUTHORITY + "/" +
                ContentContract.Item.ITEMS + "/" + (STUB_ITEM_NUMBER + 1));

        assertEquals(expected, result);
    }

    public void testInsertMultipleItems() {
        int result = insertStubItems();

        assertEquals(STUB_ITEM_NUMBER, result);
    }

    public void testQueryWithSelection() {
        Cursor cursor = getMockContentResolver().query(mUri, new String[]{Item.NUMBER}, Item.NUMBER + " = ?",
                new String[]{"3"}, null);

        assertEquals(1, cursor.getCount());
        assertEquals(Item.NUMBER, cursor.getColumnNames()[0]);

        cursor.moveToFirst();
        assertEquals("3", cursor.getString(0));
    }

    public void testQueryAll() {
        Cursor cursor = getMockContentResolver().query(mUri, new String[]{Item.TITLE}, null, null, null);
        int count = cursor.getCount();

        assertEquals(STUB_ITEM_NUMBER, count);
    }

    public void testDeleteSingleItem() {

        int delete = getMockContentResolver().delete(mUri, Item.NUMBER + " =?", new String[]{"3"});
        assertEquals(1, delete);

        Cursor cursor = getMockContentResolver().query(mUri, new String[]{Item.NUMBER}, Item.NUMBER + " = ?",
                new String[]{"3"}, null);
        assertEquals(0, cursor.getCount());
    }

    public void testDeleteMultipleItems() {
        int delete = getMockContentResolver().delete(mUri, null, null);
        assertEquals(STUB_ITEM_NUMBER, delete);
    }

    public void testUpdateSingleItem() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Item.TITLE, "update");

        int update = getMockContentResolver().update(mUri, contentValues, Item.NUMBER + " =?", new String[]{"3"});
        assertEquals(1, update);

        Cursor cursor = getMockContentResolver().query(mUri, new String[]{Item.TITLE}, Item.NUMBER + " = ?",
                new String[]{"3"}, null);
        cursor.moveToFirst();
        assertEquals("update", cursor.getString(0));
    }

    public void testUpdateMultipleItems() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Item.TITLE, "update");

        int update = getMockContentResolver().update(mUri, contentValues, null, null);
        assertEquals(10, update);

        Cursor cursor = getMockContentResolver().query(mUri, new String[]{Item.TITLE}, null, null, null);
        while(cursor.moveToNext()) {
            assertEquals("update", cursor.getString(0));
        }
    }

    private int insertStubItems() {
        ContentValues[] values = new ContentValues[STUB_ITEM_NUMBER];

        for (int i = 0; i < STUB_ITEM_NUMBER; ++i) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(Item.TITLE, "test " + i);
            contentValues.put(Item.DESCRIPTION, "des " + i);
            contentValues.put(Item.NUMBER, i);

            values[i] = contentValues;
        }

        return getMockContentResolver().bulkInsert(mUri, values);
    }
}
