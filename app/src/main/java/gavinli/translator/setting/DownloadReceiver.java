package gavinli.translator.setting;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

import gavinli.translator.App;
import gavinli.translator.util.Bspatch;
import gavinli.translator.util.GLog;

/**
 * Created by gavin on 9/5/17.
 */

public class DownloadReceiver extends BroadcastReceiver {
    private long mQueueId;

    /**
     * 下载文件路径
     */
    private String mFilePath;

    /**
     * 开始下载，同时注册监听器。
     */
    @SuppressWarnings("ConstantConditions")
    public void download(Activity activity) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        // 防止用户退出设置界面，这里使用Application类注册下载监听
        activity.getApplication().registerReceiver(this, intentFilter);
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(App.HOST + "/download/patch/" + App.VERSION_CODE));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        File file = new File(activity.getExternalCacheDir(), "PATCH.patch");
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
            String apkPath = context.getApplicationInfo().sourceDir;
            GLog.d(apkPath);
            String newApkName = "Translator-" + App.VERSION_NAME + "-release.apk";
            File newApk = new File(context.getExternalCacheDir(), newApkName);
            Bspatch.bspatch(apkPath,
                    newApk.getAbsolutePath(), mFilePath);
            promptInstall(context, newApk.getAbsolutePath());
            context.unregisterReceiver(this);
        }
    }

    /**
     * 跳转到安装界面
     *
     * @param filePath 安装包路径
     */
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
