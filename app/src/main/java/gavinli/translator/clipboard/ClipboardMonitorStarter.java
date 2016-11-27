package gavinli.translator.clipboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

/**
 * Created by GavinLi
 * on 16-11-27.
 */

public class ClipboardMonitorStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, ClipboardMonitor.class));
        }
    }
}
