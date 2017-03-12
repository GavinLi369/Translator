package gavinli.translator.wordbook;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private ArrayList<String> mWords;
    private OnItemClickListener mItemClickListener;

    public WordListAdapter(ArrayList<String> words) {
        mWords = words;
    }

    @Override
    public WordListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wordbook, parent, false));
    }

    @Override
    public void onBindViewHolder(WordListAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mWords.get(position));
    }

    @Override
    public int getItemCount() {
        return mWords.size();
    }

    public int removeItem(int postion) {
        mWords.remove(postion);
        notifyItemRemoved(postion);
        return mWords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_word);
            itemView.setOnClickListener(view -> mItemClickListener.onItemClick(itemView));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }
}
