package kr.elara.android.framework.provider.util;

import android.text.TextUtils;

public final class Log {

    public static boolean enabled = true;

    public static void d(String tag, String message) {
        if (enabled) android.util.Log.d(tag, message);
    }

    public static void deleteLog(String tag, String table, String selection, String[] selectionArgs) {
        if (enabled) {
            android.util.Log.d(tag, "DELETE FROM " + table + buildWhere(selection, selectionArgs));
        }
    }

    public static void queryLog(String tag, String table, String[] projection, String selection, String[] selectionArgs,
                                String orderBy) {
        if (enabled) {
            android.util.Log.d(tag, "SELELCT " + serializeArgs(projection) + " FROM " + table + buildWhere(selection,
                    selectionArgs) + " ORDER BY " + orderBy);
        }
    }

    private static String buildWhere(String selection, String[] selectionArgs) {
        String where = "";
        if (!TextUtils.isEmpty(selection)) {
            where = " WHERE " + selection.replace("?", "") + serializeArgs(selectionArgs);
        }
        return where;
    }

    private static String serializeArgs(String[] selectionArgs) {
        // For Logging, build string for value of where clause.
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (String value : selectionArgs) {
            stringBuilder.append(value);
            stringBuilder.append(", ");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
