package gavinli.translator.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import gavinli.translator.R;
import gavinli.translator.clipboard.ClipboardMonitor;

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
                Toast.makeText(getActivity(), "Tap to Translate is on", Toast.LENGTH_SHORT).show();
                getActivity().startService(new Intent(getActivity(), ClipboardMonitor.class));
            } else {
                Toast.makeText(getActivity(), "Tap to Translate is off", Toast.LENGTH_SHORT).show();
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
