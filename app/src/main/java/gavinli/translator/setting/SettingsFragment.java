package gavinli.translator.setting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatDialog;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import gavinli.translator.App;
import gavinli.translator.R;
import gavinli.translator.clipboard.ClipboardMonitor;
import gavinli.translator.util.permisson.RequestPermissons;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-28.
 */

@SuppressWarnings("ConstantConditions")
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;

    private long mQueueId;
    private String mFilePath;

    private ListPreference mDictionary;
    @SuppressWarnings("FieldCanBeLocal")
    private Preference mCheckUpdate;
    private SwitchPreference mTapToTranslate;

    private String[] mValueArray;
    private String[] mDictionaryArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mDictionary = (ListPreference) findPreference(getString(R.string.key_dictionary));
        mTapToTranslate = (SwitchPreference) findPreference(getString(R.string.key_clipboard));
        mCheckUpdate = findPreference(getString(R.string.key_update));

        mValueArray = getResources()
                .getStringArray(R.array.explain_language_values);
        mDictionaryArray = getResources()
                .getStringArray(R.array.explain_languages);
        String currentValue = mDictionary.getValue();
        if(currentValue.equals(mValueArray[0])) {
            mDictionary.setSummary(mDictionaryArray[0]);
        } else if(currentValue.equals(mValueArray[1])) {
            mDictionary.setSummary(mDictionaryArray[1]);
        }

        mCheckUpdate.setSummary("Current version: " + App.VERSION_NAME);
        mCheckUpdate.setOnPreferenceClickListener(preference -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissons = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                new RequestPermissons(getActivity())
                        .request(permissons, this::showUpdateDialog);
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(getString(R.string.key_clipboard))) {
            if(mTapToTranslate.isChecked()) {
                startClipboardMonitor();
                Snackbar.make(getView(), getString(R.string.tap_to_translate_on),
                            Snackbar.LENGTH_SHORT).show();
            } else {
                stopClipboardMonitor();
                Snackbar.make(getView(), getString(R.string.tap_to_translate_off),
                            Snackbar.LENGTH_SHORT).show();
            }
        } else if(s.equals(getString(R.string.key_dictionary))) {
            String currentValue = mDictionary.getValue();
            if(currentValue.equals(mValueArray[0])) {
                mDictionary.setSummary(mDictionaryArray[0]);
            } else if(currentValue.equals(mValueArray[1])) {
                mDictionary.setSummary(mDictionaryArray[1]);
            }
        }
    }

    public void startClipboardMonitor() {
        checkCanDrawOverlays();

        Intent intent = new Intent(getActivity(), ClipboardMonitor.class);
        getActivity().startService(intent);
    }

    private void checkCanDrawOverlays() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(getActivity())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        } else {
            Snackbar.make(getView(), getString(R.string.self_open_draw_overlays),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        confirmCanDrawOverlays();
    }

    private void confirmCanDrawOverlays() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(getActivity()) &&
                getView() != null) {
            Snackbar.make(getView(), getString(R.string.can_draw_overlays_off),
                    Snackbar.LENGTH_SHORT).show();
            mTapToTranslate.setChecked(false);
        }
    }

    public void stopClipboardMonitor() {
        Intent intent = new Intent(getActivity(), ClipboardMonitor.class);
        getActivity().stopService(intent);
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
                socket = new Socket(App.SERVER_HOST, App.CHECK_UPDATE_PORT);
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
                    String[] versionInfoes = App.VERSION_NAME.split("\\.");
                    if(Integer.parseInt(updateVersionInfoes[0]) > Integer.parseInt(versionInfoes[0]) ||
                            Integer.parseInt(updateVersionInfoes[1]) > Integer.parseInt(versionInfoes[1])) {
                        updateInfo.setText("最新版本：" + updateVersion);
                        update.setEnabled(true);
                    } else {
                        updateInfo.setText(getString(R.string.find_update_fail));
                    }
                }, throwable -> {
                    Snackbar.make(getView(), getString(R.string.network_error),
                            Snackbar.LENGTH_SHORT).show();
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
}
