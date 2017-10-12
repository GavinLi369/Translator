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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import gavinli.translator.App;
import gavinli.translator.R;
import gavinli.translator.clipboard.ClipboardMonitor;
import gavinli.translator.util.permisson.RequestPermissons;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(getActivity())) {
                mTapToTranslate.setChecked(false);
            }
        }
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
            } else {
                Snackbar.make(getView(), getString(R.string.tap_to_translate_on),
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(getView(), getString(R.string.self_open_draw_overlays),
                    Snackbar.LENGTH_LONG).show();
            mTapToTranslate.setChecked(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        confirmCanDrawOverlays();
    }

    private void confirmCanDrawOverlays() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(getActivity())) {
            Snackbar.make(getView(), getString(R.string.can_draw_overlays_off),
                    Snackbar.LENGTH_SHORT).show();
            mTapToTranslate.setChecked(false);
        } else {
            Snackbar.make(getView(), getString(R.string.tap_to_translate_on),
                    Snackbar.LENGTH_SHORT).show();
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
        Request request = new Request.Builder()
                .url(App.HOST + "/download/version")
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        VersionEntry versionEntry;
        try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            int versionCode = jsonObject.getInt("versionCode");
            String versionName = jsonObject.getString("versionName");
            String versionLog = jsonObject.getString("versionLog");
            versionEntry = new VersionEntry(versionCode, versionName, versionLog);
        } catch (JSONException e) {
            throw new IOException("Json格式错误");
        }
        return versionEntry;
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
        // 防止用户退出设置界面，这里使用Application类注册下载监听
        getActivity().getApplication().registerReceiver(downloadReceiver, intentFilter);
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
