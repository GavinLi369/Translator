package gavinli.translator.wordbook;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gavinli.translator.R;
import gavinli.translator.data.Explain;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private List<Explain> mWords;
    private OnItemClickListener mItemClickListener;

    public WordListAdapter(List<Explain> words) {
        mWords = words;
    }

    @Override
    public WordListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wordbook, parent, false));
    }

    @Override
    public void onBindViewHolder(WordListAdapter.ViewHolder holder, int position) {
        holder.mExplain = mWords.get(position);
        holder.mKeyView.setText(mWords.get(position).getKey());
        holder.mSummaryView.setText(mWords.get(position).getSummary());
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

    public void addItem(Explain word) {
        mWords.add(word);
        notifyItemInserted(mWords.size() - 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Explain mExplain;
        TextView mKeyView;
        TextView mSummaryView;

        public ViewHolder(View itemView) {
            super(itemView);
            mKeyView = itemView.findViewById(R.id.tv_word);
            mSummaryView = itemView.findViewById(R.id.tv_summary);
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
