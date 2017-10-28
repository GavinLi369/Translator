package gavinli.translator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import gavinli.translator.account.AccountActivity;
import gavinli.translator.clipboard.ClipboardMonitor;
import gavinli.translator.data.Account;
import gavinli.translator.data.source.datebase.AccountDb;
import gavinli.translator.search.SearchFragment;
import gavinli.translator.search.SearchModel;
import gavinli.translator.search.SearchPresenter;
import gavinli.translator.setting.SettingsActivity;
import gavinli.translator.util.ErrorLogger;
import gavinli.translator.wordbook.WordbookFragment;
import gavinli.translator.wordbook.WordbookModel;
import gavinli.translator.wordbook.WordbookPresenter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private static final int ACCOUNT_REQUEST_CODE = 138;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView mAccountImage;
    private TextView mNameText;

    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ErrorLogger.handleGlobalException(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // 设置Fragment回退栈监听，当Fragment改变时，
        // 同时改变NavigationView的选择状态。
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFrament = getCurrentFragment();
            if (currentFrament instanceof SearchFragment) {
                mNavigationView.setCheckedItem(R.id.drawer_search);
            } else if (currentFrament instanceof WordbookFragment) {
                mNavigationView.setCheckedItem(R.id.drawer_wordbook);
            }
        });
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
        Fragment currentFragment = getCurrentFragment();
        if(item.getItemId() == R.id.drawer_search) {
            if(!(currentFragment instanceof SearchFragment)) {
                // 此时处于其他Fragment界面，直接弹出当前Fragment。
                getSupportFragmentManager().popBackStackImmediate();
            }
        } else if(item.getItemId() == R.id.drawer_wordbook) {
            if(!(currentFragment instanceof WordbookFragment)) {
                WordbookFragment wordbookFragment = new WordbookFragment();
                wordbookFragment.attachNavigationDrawerToToolbar(mDrawerLayout);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(mSearchFragment);
                transaction.add(R.id.include_layout, wordbookFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                new WordbookPresenter(wordbookFragment, new WordbookModel(this));
            }
        } else if(item.getItemId() == R.id.drawer_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        mDrawerLayout.closeDrawer(mNavigationView);
        return true;
    }

    /**
     * 获取当前正在显示的Fragment
     *
     * @return 当前正在显示的Fragment
     */
    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.include_layout);
    }
}
