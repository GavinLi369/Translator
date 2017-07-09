package gavinli.translator.image;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Bitmap> mImages = new ArrayList<>();
    private OnItemClickLinstener mLinstener;

    private boolean hasFooter = false;
    private boolean mNotMoreImages = false;
    private static final int TYPE_FOOTER = -1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FOOTER) {
            return new LoadingFooterHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false));
        } else {
            return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(hasFooter && position ==  mImages.size()) {
            return TYPE_FOOTER;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(!hasFooter || position != mImages.size() &&
                holder instanceof ImageViewHolder) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.mExplainView.setImageBitmap(mImages.get(position));
            ViewGroup.LayoutParams params = imageViewHolder.mExplainView.getLayoutParams();
            if (params instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams layoutParams = (FlexboxLayoutManager.LayoutParams) params;
                layoutParams.setFlexGrow(1.0f);
            }
            imageViewHolder.mExplainView.setOnClickListener(view -> {
                if (mLinstener != null) mLinstener.onClick(imageViewHolder.mExplainView, position);
            });
        } else if (hasFooter && mNotMoreImages &&
                position == mImages.size()) {
            LoadingFooterHolder loadingFooterHolder = (LoadingFooterHolder) holder;
            loadingFooterHolder.mProgressBar.setVisibility(View.GONE);
            loadingFooterHolder.mInfoTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return hasFooter ? mImages.size() + 1 : mImages.size();
    }

    public void setImage(Bitmap image, int postion) {
        mImages.set(postion, image);
    }

    public void addImages(List<Bitmap> images) {
        mImages.addAll(images);
    }

    public void showLoadingFooter() {
        hasFooter = true;
        notifyItemInserted(mImages.size());
    }

    public void removeLoadingFooter() {
        hasFooter = false;
        notifyItemRemoved(mImages.size());
    }

    public void showNotMoreImages() {
        mNotMoreImages = true;
        notifyItemChanged(mImages.size());
    }

    public List<Bitmap> getImages() {
        return mImages;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mExplainView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mExplainView = (ImageView) itemView.findViewById(R.id.iv_explain);
        }
    }

    class LoadingFooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        private TextView mInfoTextView;

        public LoadingFooterHolder(View itemView) {
            super(itemView);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            mInfoTextView = (TextView) itemView.findViewById(R.id.tv_info);
        }
    }

    public void setOnItemClickLinstener(OnItemClickLinstener linstener) {
        mLinstener = linstener;
    }

    public interface OnItemClickLinstener {
        void onClick(View view, int postion);
    }
}
