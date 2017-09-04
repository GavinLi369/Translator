package gavinli.translator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import gavinli.translator.clipboard.ClipboardMonitor;
import gavinli.translator.search.SearchFragment;
import gavinli.translator.search.SearchModel;
import gavinli.translator.search.SearchPresenter;
import gavinli.translator.setting.SettingsActivity;
import gavinli.translator.wordbook.WordbookFragment;
import gavinli.translator.wordbook.WordbookModel;
import gavinli.translator.wordbook.WordbookPresenter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadErrorLogIfExist();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mSearchFragment = new SearchFragment();
        mSearchFragment.attachNavigationDrawerToMenuButton(mDrawerLayout);
        transaction.replace(R.id.include_layout, mSearchFragment);
        transaction.commit();
        new SearchPresenter(mSearchFragment, new SearchModel(this));

        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_clipboard), false))
            startService(new Intent(this, ClipboardMonitor.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.drawer_search) {
            if(!(getSupportFragmentManager().getFragments().get(0) instanceof SearchFragment)) {
                mSearchFragment = new SearchFragment();
                mSearchFragment.attachNavigationDrawerToMenuButton(mDrawerLayout);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.include_layout, mSearchFragment);
                transaction.commit();
                new SearchPresenter(mSearchFragment, new SearchModel(this));
            }
            mDrawerLayout.closeDrawer(mNavigationView);
        } else if(item.getItemId() == R.id.drawer_wordbook) {
            if(!(getSupportFragmentManager().getFragments().get(0) instanceof WordbookFragment)) {
                mSearchFragment = null;
                WordbookFragment wordbookFragment = new WordbookFragment();
                wordbookFragment.attachNavigationDrawerToToolbar(mDrawerLayout);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.include_layout, wordbookFragment);
                transaction.commit();
                new WordbookPresenter(wordbookFragment, new WordbookModel(this));
            }
            mDrawerLayout.closeDrawer(mNavigationView);
        } else if(item.getItemId() == R.id.drawer_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            mDrawerLayout.closeDrawer(mNavigationView);
        }
        return true;
    }

    private void uploadErrorLogIfExist() {
        File file = new File(getFilesDir().getPath(), App.ERROR_LOG_NAME);
        if(file.exists()) {
            showUploadLogDialog(file);
        }
    }

    private void showUploadLogDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title));
        builder.setMessage(getString(R.string.dialog_message));
        builder.setPositiveButton(R.string.confirm_text, (dialog, which) -> {
            new Thread(() -> {
                try {
                    realUploadLog(file);
                } catch (IOException e) {
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
        builder.setNegativeButton(R.string.cancel_text, (dialog, which) -> file.delete());
        builder.create().show();
    }

    private void realUploadLog(File file) throws IOException {
        Socket socket = new Socket(App.SERVER_HOST, App.UPLOAD_LOG_PORT);
        OutputStream out = socket.getOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)))) {
                String temp;
                while((temp = reader.readLine()) != null) {
                    writer.write(temp, 0, temp.length());
                    writer.write("\n", 0, 1);
                }
            }
        }
        socket.close();
        file.delete();
    }
}
