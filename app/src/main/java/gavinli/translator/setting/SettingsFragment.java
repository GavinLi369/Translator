package gavinli.translator.setting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;

import java.io.BufferedReader;
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

        mCheckUpdate.setSummary(getString(R.string.setting_update_summary) + App.VERSION_NAME);
        mCheckUpdate.setOnPreferenceClickListener(preference -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissons = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                new RequestPermissons(getActivity())
                        .request(permissons, this::showUpdateInfo);
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

    private void showUpdateInfo() {
        Observable.create((Observable.OnSubscribe<VersionEntry>) subscriber -> {
            try {
                VersionEntry versionEntry = fetchRemoteVersionEntry();
                subscriber.onNext(versionEntry);
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkRemoteVersion, throwable -> {
                    Snackbar.make(getView(), getString(R.string.network_error),
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    private VersionEntry fetchRemoteVersionEntry() throws IOException {
        try (Socket socket = new Socket(App.SERVER_HOST, App.CHECK_UPDATE_PORT)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            int versionCode = Integer.parseInt(bufferedReader.readLine());
            String versionName = bufferedReader.readLine();
            StringBuilder versionLog = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                versionLog.append(line).append("\n");
            }
            versionLog.deleteCharAt(versionLog.length() - 1);
            return new VersionEntry(versionCode, versionName, versionLog.toString());
        }
    }

    private void checkRemoteVersion(VersionEntry versionEntry) {
        if(versionEntry.versionCode > App.VERSION_CODE) {
            buildUpdateDialog(versionEntry);
        } else {
            Snackbar.make(getView(), getString(R.string.update_absent),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void buildUpdateDialog(VersionEntry versionEntry) {
        String message = "最新版本：" + versionEntry.versionName + "\n\n" +
                versionEntry.versionLog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton(R.string.update_text, (dialog1, which) -> startDownload());
        builder.setNegativeButton(R.string.cancel_text, (dialog12, which) -> {});
        builder.create().show();
    }
    private void startDownload() {
        DownloadReceiver downloadReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(downloadReceiver, intentFilter);
        downloadReceiver.download(getActivity());
    }

    /**
     * 服务器传来的新版App信息
     */
    class VersionEntry {
        final int versionCode;
        final String versionName;
        final String versionLog;

        public VersionEntry(int versionCode, String versionName, String versionLog) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.versionLog = versionLog;
        }
    }
}
