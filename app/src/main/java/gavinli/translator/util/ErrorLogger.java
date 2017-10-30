package gavinli.translator.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

import gavinli.translator.App;
import gavinli.translator.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 全局异常处理
 *
 * Created by gavin on 10/28/17.
 */

public class ErrorLogger {
    /**
     * 异常信息文件名
     */
    public static final String ERROR_LOG_NAME = "error.log";

    /**
     * {@link OkHttpClient}文本数据类型
     */
    private static final MediaType PLAIN = MediaType.parse("text/plain; charset=utf-8");

    public synchronized static void handleGlobalException(Activity activity) {
        new ErrorLogger(activity);
    }

    private ErrorLogger(Activity activity) {
        realHandleGlobalException(activity);
    }

    public void realHandleGlobalException(Activity activity) {
        checkMainThread();

        uploadErrorLogIfExist(activity);

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            File file = new File(activity.getFilesDir().getPath() + File.separator + ERROR_LOG_NAME);

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

            String exceptionInfo = buildExceptionInfo(activity, e);
            writer.write(exceptionInfo);
            writer.flush();
            writer.close();

            //必须调用默认Handler，否则应用无法退出
            defaultHandler.uncaughtException(t, e);
        });
    }

    /**
     * 构建手机异常信息
     *
     * @param e 异常栈信息
     */
    private String buildExceptionInfo(Context context, Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        String date = SimpleDateFormat.getDateTimeInstance()
                .format(System.currentTimeMillis());
        StringBuilder report = new StringBuilder();
        report.append("************ CAUSE OF ERROR ************\n");
        report.append("************ ").append(date).append(" ************\n\n");

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),
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

    /**
     * 从{@link Build}中获取手机配置信息
     */
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

    /**
     * 检查是否存在异常信息，如果存在则显示异常信息对话框
     */
    private void uploadErrorLogIfExist(Activity activity) {
        File file = new File(activity.getFilesDir().getPath(), ERROR_LOG_NAME);
        if(file.exists()) {
            showUploadLogDialog(activity, file);
        }
    }

    /**
     * 显示异常信息上传对话框
     *
     * @param file 异常信息文件
     */
    private void showUploadLogDialog(Activity activity, File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.dialog_title));
        builder.setMessage(activity.getString(R.string.dialog_message));
        builder.setPositiveButton(R.string.confirm_text, (dialog, which) -> {
            new Thread(() -> {
                try {
                    realUploadLog(file);
                } catch (IOException e) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, activity.getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
        builder.setNegativeButton(R.string.cancel_text, (dialog, which) -> file.delete());
        builder.create().show();
    }

    /**
     * 上传异常信息
     *
     * @param file 异常信息文件
     *
     * @throws IOException 网络出错或磁盘I/O出错
     */
    private void realUploadLog(File file) throws IOException {
        String log = getLogFromFile(file);
        // delete the log regardless of whether the upload is sucessful.
        file.delete();

        Request request = new Request.Builder()
                .url(App.HOST + "/log/error")
                .post(RequestBody.create(PLAIN, log))
                .build();
        new OkHttpClient().newCall(request).execute();
    }

    /**
     * 读取指定文本文件内容
     *
     * @param file 指定文件
     *
     * @throws IOException 磁盘I/O出错
     */
    private String getLogFromFile(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder log = new StringBuilder();
            String temp;
            while((temp = reader.readLine()) != null) {
                log.append(temp).append('\n');
            }
            // remove the last '\n'
            log.deleteCharAt(log.length() - 1);
            return log.toString();
        }
    }

    /**
     * 检查是否处于主线程
     *
     * @throws RuntimeException 该线程不是主线程
     */
    private void checkMainThread() throws RuntimeException {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("该方法必须在主线程调用");
        }
    }
}
