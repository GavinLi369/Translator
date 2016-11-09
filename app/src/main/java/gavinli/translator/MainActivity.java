package gavinli.translator;

import android.app.SearchManager;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Spanned;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private TextView mDefineTextView;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDefineTextView = (TextView) findViewById(R.id.tv_define);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setOnQueryTextListener(this);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mSearchView.clearFocus();
        new MyTask().execute("http://dictionary.cambridge.org/dictionary/english/" + query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    class MyTask extends AsyncTask<String, Void, ArrayList<Spanned>> {
        @Override
        protected ArrayList<Spanned> doInBackground(String... urls) {
            Request request = new Request.Builder()
                    .url(urls[0])
                    .build();
            try {
                Response response = new OkHttpClient().newCall(request).execute();
                HtmlDecoder htmlDecoder = new HtmlDecoder(response.body().string(), MainActivity.this);
                return htmlDecoder.decode();
            } catch (IOException | IndexOutOfBoundsException  e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Spanned> spanneds) {
            mDefineTextView.setText("");
            if(spanneds != null) {
                for (Spanned spanned : spanneds) {
                    mDefineTextView.append(spanned);
                    mDefineTextView.append("\n\n");
                }
            } else {
                Toast.makeText(MainActivity.this, "无该单词", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
