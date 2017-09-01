package gavinli.translator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

import gavinli.translator.clipboard.ClipboardMonitor;

/**
 * Created by GavinLi
 * on 5/16/17.
 */

public class App extends Application {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;

    private Intent mClipboardMonitorIntent;

    private static final String ERROR_LOG_NAME = "error.log";

    @Override
    public void onCreate() {
        super.onCreate();

        uploadErrorLogIfExist();

        handleGlobalException();
    }

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

    //TODO upload error log
    private void uploadErrorLogIfExist() {

    }

    /**
     * 全局异常处理
     */
    private void handleGlobalException() {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            File file = new File(getFilesDir().getPath() + File.separator + ERROR_LOG_NAME);

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                return;
            }

            PrintWriter writer;
            try {
                writer = new PrintWriter(new FileOutputStream(file));
            } catch (FileNotFoundException exception) {
                return;
            }

            String exceptionInfo = buildExceptionInfo(e);
            writer.write(exceptionInfo);
            writer.flush();
            writer.close();

            //必须调用默认Handler，否则应用无法退出
            defaultHandler.uncaughtException(t, e);
        });
    }

    private String buildExceptionInfo(Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        String date = SimpleDateFormat.getDateTimeInstance()
                .format(System.currentTimeMillis());
        StringBuilder report = new StringBuilder();
        report.append("************ CAUSE OF ERROR ************\n");
        report.append("************ ").append(date).append(" ************\n\n");

        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            report.append("versionName=").append(packageInfo.versionName).append("\n");
            report.append("versionCode=").append(packageInfo.versionCode).append("\n");
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
        }

        report.append("SDK_INT=").append(Build.VERSION.SDK_INT).append("\n");
        report.append(getMobileInfo()).append("\n");

        report.append(stackTrace.toString());
        return report.toString();
    }

    private String getMobileInfo() {
        StringBuilder builder = new StringBuilder();
        Field[] fields = Build.class.getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            try {
                //不需要获取实例字段
                value = field.get(null).toString();
            } catch (Exception e) {
                value = null;
            }
            if(value != null) {
                builder.append(name).append("=").append(value).append("\n");
            }
        }
        return builder.toString();
    }
}
