package gavinli.translator.util;

/**
 * Created by gavin on 10/18/17.
 */

public class Bspatch {
    static {
        System.loadLibrary("bspatch");
    }

    public static native int bspatch(String oldApk, String newApk, String patch);
}
