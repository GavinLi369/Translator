package gavinli.translator;

import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import gavinli.translator.search.SearchFragment;
import gavinli.translator.search.SearchModel;
import gavinli.translator.search.SearchPresenter;
import gavinli.translator.wordbook.WordbookFragment;
import gavinli.translator.wordbook.WordbookModel;
import gavinli.translator.wordbook.WordbookPresenter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SearchFragment searchFragment = new SearchFragment();
        transaction.replace(R.id.include_layout, searchFragment);
        transaction.commit();
        new SearchPresenter(searchFragment, new SearchModel(this));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.drawer_search) {
            if(!(getSupportFragmentManager().getFragments().get(0) instanceof SearchFragment)) {
                SearchFragment searchFragment = new SearchFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.include_layout, searchFragment);
                transaction.commit();
                new SearchPresenter(searchFragment, new SearchModel(this));
            }
            mDrawerLayout.closeDrawer(mNavigationView);
        } else if(item.getItemId() == R.id.drawer_wordbook) {
            if(!(getSupportFragmentManager().getFragments().get(0) instanceof WordbookFragment)) {
                WordbookFragment wordbookFragment = new WordbookFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.include_layout, wordbookFragment);
                transaction.commit();
                new WordbookPresenter(wordbookFragment, new WordbookModel(this));
            }
            mDrawerLayout.closeDrawer(mNavigationView);
        }
        return true;
    }
}
