package gavinli.translator.wordbook;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gavinli.translator.R;
import gavinli.translator.data.Explain;
import gavinli.translator.util.UiTool;
import gavinli.translator.worddetail.WordDetailActivity;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookFragment extends Fragment implements WordbookContract.View {
    private RecyclerView mWordListView;
    private WordListAdapter mAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private WordbookContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wordbook, container, false);
        Toolbar mToolbar = root.findViewById(R.id.toolbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(UiTool.dpToPx(getContext(), 4));
        }
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar,
                R.string.app_name, R.string.app_name);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mWordListView = root.findViewById(R.id.word_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mWordListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWordListView.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallBack());
        itemTouchHelper.attachToRecyclerView(mWordListView);
        return root;
    }

    @Override
    public void setPresenter(WordbookContract.Presenter presenter) {
        mPresenter = presenter;
        mPresenter.loadWords();
    }

    @Override
    public void showWords(List<Explain> explains) {
        mAdapter = new WordListAdapter(explains);
        mAdapter.setOnItemClickListener(view -> {
            TextView textView = view.findViewById(R.id.tv_word);
            Intent intent = new Intent(getActivity(), WordDetailActivity.class);
            intent.putExtra(WordDetailActivity.INTENT_WORD_KEY, textView.getText());
            startActivity(intent);
        });
        mWordListView.setAdapter(mAdapter);
    }

    @Override
    public void showBackground() {
        mWordListView.setBackgroundResource(R.drawable.bg_wordbook);
    }

    @Override
    public void hideBackground() {
        mWordListView.setBackground(null);
    }

    class ItemTouchCallBack extends ItemTouchHelper.SimpleCallback {
        public ItemTouchCallBack() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Explain explain = ((WordListAdapter.ViewHolder) viewHolder)
                    .mExplain;
            mPresenter.removeWord(explain);
            if(getView() != null) {
                Snackbar.make(getView(), "单词已移除",
                        Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            mPresenter.restoreWord();
                            mAdapter.addItem(explain);
                            Snackbar.make(getView(), "单词已恢复",
                                    Snackbar.LENGTH_SHORT).show();
                        }).show();
            }
            if(mAdapter.removeItem(viewHolder.getAdapterPosition()) == 0) {
                showBackground();
            }
        }
    }

    public void attachNavigationDrawerToToolbar(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
    }
}
