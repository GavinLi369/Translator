package gavinli.translator.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.image.ImageActivity;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class SearchFragment extends Fragment implements SearchContract.View, FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnQueryChangeListener, SearchSuggestionsAdapter.OnBindSuggestionCallback{
    private View mRootView;
    private TextView mExplainView;
    private ImageView mBackground;
    private FloatingSearchView mSearchBar;
    private ScrollView mScrollView;
    private DrawerLayout mDrawerLayout;
    private FloatingActionMenu mMenuFab;
    private FloatingActionButton mChineseFab;

    private SearchContract.Presenter mPresenter;

    public static final String INTENT_KEY = "key";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mScrollView = (ScrollView) mRootView.findViewById(R.id.scroll_view);
        mBackground = (ImageView) mRootView.findViewById(R.id.img_bg);
        mExplainView = (TextView) mRootView.findViewById(R.id.tv_define);
        mExplainView.setMovementMethod(LinkMovementMethod.getInstance());
        mMenuFab = (FloatingActionMenu) mRootView.findViewById(R.id.fab_menu);
        mMenuFab.setClosedOnTouchOutside(true);
        mMenuFab.hideMenu(false);
        FloatingActionButton starFab = (FloatingActionButton) mRootView.findViewById(R.id.fab_star);
        starFab.setOnClickListener(view -> {
            mMenuFab.close(true);
            mPresenter.saveWord();
        });
        FloatingActionButton imageFab = (FloatingActionButton) mRootView.findViewById(R.id.fab_image);
        imageFab.setOnClickListener(view -> {
            mMenuFab.close(true);
            Intent intent = new Intent(getContext(), ImageActivity.class);
            intent.putExtra(INTENT_KEY, mPresenter.getCurrentWord());
            startActivity(intent);
        });
        mChineseFab = (FloatingActionButton) mRootView.findViewById(R.id.fab_chinese);
        mChineseFab.setOnClickListener(view -> {
            mSearchBar.showProgress();
            mPresenter.loadChineseExplain(mPresenter.getCurrentWord());
            mMenuFab.close(true);
        });
        mSearchBar = (FloatingSearchView) mRootView.findViewById(R.id.search_view);
        mSearchBar.setOnSearchListener(this);
        mSearchBar.setOnQueryChangeListener(this);
        mSearchBar.setOnBindSuggestionCallback(this);
        if(mDrawerLayout != null) {
            mSearchBar.attachNavigationDrawerToMenuButton(mDrawerLayout);
        }
        mSearchBar.setSelected(true);
        showBackground();
        return mRootView;
    }

    @Override
    public void showExplain(List<Spanned> explains) {
        mChineseFab.setVisibility(View.INVISIBLE);
        mSearchBar.hideProgress();
        mExplainView.setText("");
        mScrollView.scrollTo(0, 0);
        for (Spanned spanned : explains) {
            mExplainView.append(spanned);
            mExplainView.append("\n\n");
        }
        mMenuFab.showMenu(true);
    }

    @Override
    public void showChineseExplain(List<Spanned> explains) {
        mChineseFab.setVisibility(View.GONE);
        mSearchBar.hideProgress();
        mExplainView.setText("");
        mScrollView.scrollTo(0, 0);
        for (Spanned spanned : explains) {
            mExplainView.append(spanned);
            mExplainView.append("\n\n");
        }
    }

    @Override
    public void showSuggestion(List<String> suggestions) {
        if(suggestions != null && suggestions.size() != 0) {
            ArrayList<WordSuggestion> wordSuggestions = new ArrayList<>();
            for(String suggestion : suggestions) {
                wordSuggestions.add(new WordSuggestion(suggestion));
            }
            mSearchBar.swapSuggestions(wordSuggestions);
        } else {
            mSearchBar.swapSuggestions(new ArrayList<>());
        }
        mSearchBar.hideProgress();
    }

    @Override
    public void showNetworkError() {
        mSearchBar.hideProgress();
        Snackbar.make(mRootView, "网络连接失败", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showNotFoundWordError() {
        mMenuFab.hideMenu(true);
        mSearchBar.hideProgress();
        Snackbar.make(mRootView, "无该单词", Snackbar.LENGTH_SHORT).show();
        mExplainView.setText("");
        showBackground();
    }

    @Override
    public void showChineseExplainNotFoundError() {
        mSearchBar.hideProgress();
        Snackbar.make(mRootView, "该单词无汉语解释", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showWordInfo(String info) {
        Snackbar.make(mRootView, info, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showBackground() {
        mBackground.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideBackground() {
        mBackground.setVisibility(View.GONE);
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        mSearchBar.showProgress();
        mPresenter.loadExplain(searchSuggestion.getBody());
    }

    @Override
    public void onSearchAction(String currentQuery) {
        mSearchBar.showProgress();
        mPresenter.loadExplain(currentQuery);
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        if(newQuery.length() < 2){
            mSearchBar.hideProgress();
            mPresenter.cancelAutoCompleteIfCompleting();
            mSearchBar.swapSuggestions(new ArrayList<>());
            return;
        }
        mSearchBar.showProgress();

        mPresenter.loadAutoComplete(newQuery, 5);
    }

    @Override
    public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView,
                                 SearchSuggestion item, int itemPosition) {
        leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_arrow_right, null));
        textView.setText(item.getBody());
    }

    public void attachNavigationDrawerToMenuButton(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
