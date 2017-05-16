package gavinli.translator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import gavinli.translator.clipboard.ClipboardMonitor;

/**
 * Created by GavinLi
 * on 5/16/17.
 */

public class App extends Application {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;

    private Intent mClipboardMonitorIntent;

    public void startClipboardMonitor(Activity activity) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                activity.startActivityForResult(intent,
                        ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
        mClipboardMonitorIntent = new Intent(this, ClipboardMonitor.class);
        startService(mClipboardMonitorIntent);
    }

    public void stopClipboardMonitor() {
        if(mClipboardMonitorIntent != null) {
            stopService(mClipboardMonitorIntent);
        }
    }
}
