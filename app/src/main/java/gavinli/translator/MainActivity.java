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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import gavinli.translator.account.AccountActivity;
import gavinli.translator.clipboard.ClipboardMonitor;
import gavinli.translator.data.Account;
import gavinli.translator.data.source.datebase.AccountDb;
import gavinli.translator.search.SearchFragment;
import gavinli.translator.search.SearchModel;
import gavinli.translator.search.SearchPresenter;
import gavinli.translator.setting.SettingsActivity;
import gavinli.translator.wordbook.WordbookFragment;
import gavinli.translator.wordbook.WordbookModel;
import gavinli.translator.wordbook.WordbookPresenter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private static final int ACCOUNT_REQUEST_CODE = 138;
    private static final MediaType PLAIN = MediaType.parse("text/plain; charset=utf-8");

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView mAccountImage;
    private TextView mNameText;

    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadErrorLogIfExist();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mAccountImage = mNavigationView.getHeaderView(0).findViewById(R.id.img_account);
        mAccountImage.setOnClickListener(view -> {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivityForResult(intent, ACCOUNT_REQUEST_CODE);
        });
        mNameText = mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
        AccountDb accountDb = new AccountDb(this);
        Account account = accountDb.getAccountData();
        if(account != null) {
            mAccountImage.setImageBitmap(account.face);
            mNameText.setText(account.name);
        }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACCOUNT_REQUEST_CODE) {
            AccountDb accountDb = new AccountDb(this);
            Account account = accountDb.getAccountData();
            if(account != null) {
                mAccountImage.setImageBitmap(account.face);
                mNameText.setText(account.name);
            } else {
                mAccountImage.setImageResource(R.drawable.img_default_face);
                mNameText.setText("");
            }
        }
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

    // TODO failed to upload log on Xiaomi phone
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
        String log = getLogFromFile(file);
        // delete the log regardless of whether the upload is sucessful.
        file.delete();

        Request request = new Request.Builder()
                .url(App.HOST + "/log/error")
                .post(RequestBody.create(PLAIN, log))
                .build();
        new OkHttpClient().newCall(request).execute();
    }

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
}
