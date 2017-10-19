package gavinli.translator.util;

import android.util.Log;

/**
 * Log工具类
 *
 * Created by gavin on 10/19/17.
 */

public class GLog {
    /**
     * Log标签
     */
    private static String sTag = "GLog";

    /**
     * 设置Log标签
     *
     * @param tag 标签
     */
    public static void tag(String tag) {
        sTag = tag;
    }

    public static void d(String message) {
        Log.d(sTag, message);
    }

    public static void d(int message) {
        Log.d(sTag, String.valueOf(message));
    }

    public static void d(char message) {
        Log.d(sTag, String.valueOf(message));
    }

    public static void d(long message) {
        Log.d(sTag, String.valueOf(message));
    }

    public static void d(float message) {
        Log.d(sTag, String.valueOf(message));
    }

    public static void d(double message) {
        Log.d(sTag, String.valueOf(message));
    }
}