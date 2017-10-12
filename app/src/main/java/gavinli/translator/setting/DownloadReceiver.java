package gavinli.translator.setting;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

import gavinli.translator.App;

/**
 * Created by gavin on 9/5/17.
 */

public class DownloadReceiver extends BroadcastReceiver {
    private long mQueueId;
    private String mFilePath;

    @SuppressWarnings("ConstantConditions")
    public void download(Context context) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(App.HOST + "/download/apk"));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        File file = new File(context.getExternalCacheDir(), "Translator.apk");
        if(file.exists()) {
            file.delete();
        }
        mFilePath = file.getPath();
        request.setDestinationUri(Uri.fromFile(file));
        mQueueId = downloadManager.enqueue(request);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if(id == mQueueId) {
            promptInstall(context, mFilePath);
            context.unregisterReceiver(this);
        }
    }

    private void promptInstall(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            apkUri = FileProvider.getUriForFile(context,
                    "gavinli.translator", new File(filePath));
        } else {
            apkUri = Uri.fromFile(new File(filePath));
        }
        intent.setDataAndType(apkUri,
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
