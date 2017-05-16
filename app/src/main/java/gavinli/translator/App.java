package gavinli.translator;

import android.app.Application;
import android.content.Intent;

import gavinli.translator.clipboard.ClipboardMonitor;

/**
 * Created by GavinLi
 * on 5/16/17.
 */

public class App extends Application {
    private Intent mClipboardMonitorIntent;

    public void startClipboardMonitor() {
        mClipboardMonitorIntent = new Intent(this, ClipboardMonitor.class);
        startService(mClipboardMonitorIntent);
    }

    public void stopClipboardMonitor() {
        if(mClipboardMonitorIntent != null) {
            stopService(mClipboardMonitorIntent);
        }
    }
}
