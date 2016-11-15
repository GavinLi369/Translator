package gavinli.translator.search;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;

import gavinli.translator.R;

public class SearchActivity extends AppCompatActivity implements SearchContract.View, FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnQueryChangeListener, SearchSuggestionsAdapter.OnBindSuggestionCallback {
    private TextView mDefineTextView;
    private FloatingSearchView mSearchBar;
    private ScrollView mScrollView;

    private SearchContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mDefineTextView = (TextView) findViewById(R.id.tv_define);
        mSearchBar = (FloatingSearchView) findViewById(R.id.search_view);
        mSearchBar.setOnSearchListener(this);
        mSearchBar.setOnQueryChangeListener(this);
        mSearchBar.setOnBindSuggestionCallback(this);

        new SearchPresenter(this, new SearchModel(this));
    }

    @Override
    public void showExplain(ArrayList<Spanned> explains) {
        mDefineTextView.setText("");
        mScrollView.scrollTo(0, 0);
        for (Spanned spanned : explains) {
            mDefineTextView.append(spanned);
            mDefineTextView.append("\n\n");
        }
    }

    @Override
    public void showSuggestion(ArrayList<String> suggestions) {
        if(suggestions != null && suggestions.size() != 0) {
            ArrayList<WordSuggestion> wordSuggestions = new ArrayList<>();
            int num = Math.min(5, suggestions.size());
            for(int i = 0; i < num; i++) {
                wordSuggestions.add(new WordSuggestion(suggestions.get(i)));
            }
            mSearchBar.swapSuggestions(wordSuggestions);
        } else {
            mSearchBar.swapSuggestions(new ArrayList<>());
        }
        mSearchBar.hideProgress();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(SearchActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNotFoundWordError() {
        Toast.makeText(SearchActivity.this, "无该单词", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        mPresenter.loadExplain(searchSuggestion.getBody());
    }

    @Override
    public void onSearchAction(String currentQuery) {
        mPresenter.loadExplain(currentQuery);
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        if(newQuery.length() < 2){
            mSearchBar.swapSuggestions(new ArrayList<>());
            return;
        }
        mSearchBar.showProgress();

        mPresenter.loadAutoComplete(newQuery);
    }

    @Override
    public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView,
                                 SearchSuggestion item, int itemPosition) {
        leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_arrow_right, null));
        textView.setText(item.getBody());
    }

    @Override
    public void setPresent(SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
