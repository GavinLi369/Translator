package gavinli.translator;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by GavinLi
 * on 5/16/17.
 */

public class App extends Application {
    public static final String HOST = "http://192.243.117.153:4567";

    public static int VERSION_CODE;
    public static String VERSION_NAME;

    @Override
    public void onCreate() {
        super.onCreate();

        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        VERSION_CODE = packageInfo.versionCode;
        VERSION_NAME = packageInfo.versionName;
    }
}
