package kr.elara.android.framework.provider.util;

public final class Log {

    public static boolean enabled = true;

    public static void d(String tag, String message) {
        if (enabled) android.util.Log.d(tag, message);
    }
}
