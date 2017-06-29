package gavinli.translator.setting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import gavinli.translator.App;
import gavinli.translator.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-28.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private long mQueueId;
    private String mFilePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        ListPreference dictionaryPreference = (ListPreference) findPreference(getString(R.string.key_dictionary));
        if(dictionaryPreference.getValue().equals(getResources().getStringArray(R.array.explain_language_values)[0])) {
            dictionaryPreference.setSummary(getResources().getStringArray(R.array.explain_languages)[0]);
        } else if(dictionaryPreference.getValue().equals(getResources().getStringArray(R.array.explain_language_values)[1])) {
            dictionaryPreference.setSummary(getResources().getStringArray(R.array.explain_languages)[1]);
        }
        Preference update = findPreference(getString(R.string.key_update));
        try {
            update.setSummary("Current version: " + getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        update.setOnPreferenceClickListener(preference -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!checkStrogePermisson()) {
                    requestStrogePermisson();
                }
                if(!checkStrogePermisson()) return true;
            }
            showUpdateDialog();
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(getString(R.string.key_clipboard))) {
            SwitchPreference clipboardPreference = (SwitchPreference) findPreference(s);
            if(clipboardPreference.isChecked()) {
                ((App) getActivity().getApplication()).startClipboardMonitor(getActivity());
                if(getView() != null) {
                    Snackbar.make(getView(), "Tap to Translate is on", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                ((App) getActivity().getApplication()).stopClipboardMonitor();
                if(getView() != null) {
                    Snackbar.make(getView(), "Tap to Translate is off", Snackbar.LENGTH_SHORT).show();
                }
            }
        } else if(s.equals(getString(R.string.key_dictionary))) {
            ListPreference dictionaryPreference = (ListPreference) findPreference(s);
            if(dictionaryPreference.getValue().equals(getResources().getStringArray(R.array.explain_language_values)[0])) {
                dictionaryPreference.setSummary(getResources().getStringArray(R.array.explain_languages)[0]);
            } else if(dictionaryPreference.getValue().equals(getResources().getStringArray(R.array.explain_language_values)[1])) {
                dictionaryPreference.setSummary(getResources().getStringArray(R.array.explain_languages)[1]);
            }
        }
    }

    private void showUpdateDialog() {
        AppCompatDialog dialog = new AppCompatDialog(getActivity());
        dialog.setContentView(R.layout.dialog_update);
        dialog.show();
        TextView updateInfo = (TextView) dialog.findViewById(R.id.tv_update_info);
        Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button update = (Button) dialog.findViewById(R.id.btn_update);
        assert cancel != null;
        cancel.setOnClickListener(view -> dialog.cancel());
        assert update != null;
        update.setEnabled(false);
        update.setOnClickListener(view -> {
            getActivity().registerReceiver(new DowloadCompleteReceiver(),
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            downloadApk();
            dialog.cancel();
        });

        checkUpdate(updateInfo, update);
    }

    @SuppressLint("SetTextI18n")
    private void checkUpdate(TextView updateInfo, Button update) {
        Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            Socket socket = null;
            try {
                socket = new Socket("192.243.117.153", 8848);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String updateVersion = bufferedReader.readLine();
                subscriber.onNext(updateVersion);
            } catch (IOException e) {
                e.printStackTrace();
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateVersion -> {
                    String[] updateVersionInfoes = updateVersion.split("\\.");
                    try {
                        String version = getActivity().getPackageManager().getPackageInfo(
                                getActivity().getPackageName(), 0).versionName;
                        String[] versionInfoes = version.split("\\.");
                        assert updateInfo != null;
                        if(Integer.parseInt(updateVersionInfoes[0]) > Integer.parseInt(versionInfoes[0]) ||
                                Integer.parseInt(updateVersionInfoes[1]) > Integer.parseInt(versionInfoes[1])) {
                            updateInfo.setText("最新版本：" + updateVersion);
                            update.setEnabled(true);
                        } else {
                            updateInfo.setText(getString(R.string.find_update_fail));
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }, throwable -> {
                    View view = getView();
                    if(view != null) {
                        Snackbar.make(view, "网络连接出错", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void downloadApk() {
        DownloadManager downloadManager = (DownloadManager) getActivity()
                .getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse("http://192.243.117.153:8849/"));
        File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Translator.apk");
        if(file.exists()) {
            file.delete();
        }
        mFilePath = file.getPath();
        request.setDestinationUri(Uri.fromFile(file));
        mQueueId = downloadManager.enqueue(request);
    }

    class DowloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if(id == mQueueId) {
                promptInstall(context, mFilePath);
                getActivity().unregisterReceiver(this);
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
    private boolean checkStrogePermisson() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStrogePermisson() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
    }
}
