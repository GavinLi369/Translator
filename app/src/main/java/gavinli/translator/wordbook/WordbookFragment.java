package gavinli.translator.wordbook;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookFragment extends Fragment implements WordbookContract.View {
    private RecyclerView mWordListView;
    private WordListAdapter mAdapter;

    private WordbookContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wordbook, container, false);
        mWordListView = (RecyclerView) root.findViewById(R.id.word_list);
        mWordListView.setLayoutManager(new LinearLayoutManager(getContext()));
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
    public void showWords(ArrayList<String> words) {
        mAdapter = new WordListAdapter(words);
        mWordListView.setAdapter(mAdapter);
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
            String word = ((WordListAdapter.ViewHolder) viewHolder).mTextView.getText().toString();
            mPresenter.removeWord(word);
            mAdapter.removeItem(viewHolder.getAdapterPosition());
        }
    }
}
