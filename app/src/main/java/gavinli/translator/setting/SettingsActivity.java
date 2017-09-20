package gavinli.translator.setting;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import gavinli.translator.R;
import gavinli.translator.util.UiTool;

/**
 * Created by GavinLi
 * on 16-11-28.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(UiTool.dpToPx(this, 4));
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());
        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment()).commit();
    }
}
