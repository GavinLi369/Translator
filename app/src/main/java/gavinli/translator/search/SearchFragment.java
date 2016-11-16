package gavinli.translator.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class SearchFragment extends Fragment implements SearchContract.View, FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnQueryChangeListener, SearchSuggestionsAdapter.OnBindSuggestionCallback {
    private TextView mExplainView;
    private FloatingSearchView mSearchBar;
    private ScrollView mScrollView;

    private SearchContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        mScrollView = (ScrollView) root.findViewById(R.id.scroll_view);
        mExplainView = (TextView) root.findViewById(R.id.tv_define);
        mExplainView.setMovementMethod(LinkMovementMethod.getInstance());
        mSearchBar = (FloatingSearchView) root.findViewById(R.id.search_view);
        mSearchBar.setOnSearchListener(this);
        mSearchBar.setOnQueryChangeListener(this);
        mSearchBar.setOnBindSuggestionCallback(this);
        return root;
    }

    @Override
    public void showExplain(ArrayList<Spanned> explains) {
        mExplainView.setText("");
        mScrollView.scrollTo(0, 0);
        for (Spanned spanned : explains) {
            mExplainView.append(spanned);
            mExplainView.append("\n\n");
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
        Toast.makeText(getContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNotFoundWordError() {
        Toast.makeText(getContext(), "无该单词", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showWordInfo(String info) {
        Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
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
    public void setPresenter(SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
