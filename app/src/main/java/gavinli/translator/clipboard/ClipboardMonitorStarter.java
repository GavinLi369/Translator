package gavinli.translator.clipboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-27.
 */

public class ClipboardMonitorStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) &&
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.key_clipboard), false)) {
            context.startService(new Intent(context, ClipboardMonitor.class));
        }
    }
}
