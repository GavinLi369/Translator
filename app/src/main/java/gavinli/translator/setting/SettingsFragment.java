package gavinli.translator.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;

import gavinli.translator.App;
import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-28.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
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
}
