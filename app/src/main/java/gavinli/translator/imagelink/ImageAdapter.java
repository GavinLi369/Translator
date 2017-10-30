package gavinli.translator.imagelink;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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
import gavinli.translator.data.NetworkImage;
import gavinli.translator.util.imageloader.ImageLoader;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<NetworkImage> mImageLinks = new ArrayList<>();
    private OnItemClickListener mListener;

    private Context mContext;

    /**
     * 所有图片资源已加载完成
     */
    private boolean mNoMore = false;

    /**
     * 底部View类型
     *
     * @see #getItemViewType(int)
     */
    private static final int TYPE_FOOTER = -1;

    public ImageAdapter(Context context) {
        mContext = context;
    }

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
        if(position ==  mImageLinks.size()) {
            return TYPE_FOOTER;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ImageViewHolder) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            NetworkImage networkImage  = mImageLinks.get(position);
            GradientDrawable placeholder = (GradientDrawable) mContext.getResources()
                    .getDrawable(R.drawable.img_placeholder);
            placeholder.setSize(networkImage.getWidth(), networkImage.getHeight());
            ImageLoader.with(mContext)
                    .load(networkImage.getUrl())
//                    .resize(networkImage.getWidth(), networkImage.getHeight())
                    .placeholder(placeholder)
                    .into(imageViewHolder.mImageView);
            imageViewHolder.mImageView.setOnClickListener(view -> {
                if (mListener != null) mListener.onClick(imageViewHolder.mImageView, position);
            });
        } else {
            LoadingFooterHolder loadingFooterHolder = (LoadingFooterHolder) holder;
            if (mNoMore) {
                loadingFooterHolder.mProgressBar.setVisibility(View.GONE);
                loadingFooterHolder.mInfoTextView.setVisibility(View.VISIBLE);
            } else {
                loadingFooterHolder.mProgressBar.setVisibility(View.VISIBLE);
                loadingFooterHolder.mInfoTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mImageLinks.isEmpty() ? 0 : mImageLinks.size() + 1;
    }

    public void addImageLinks(NetworkImage link) {
        mImageLinks.add(link);
    }

    /**
     * 在{@link RecyclerView}底部显示信息，表示无更多图片。
     */
    public void showNotMoreImages() {
        mNoMore = true;
        notifyItemChanged(mImageLinks.size());
    }

    public List<NetworkImage> getImageLinks() {
        return mImageLinks;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_explain);
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            if (params instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams layoutParams = (FlexboxLayoutManager.LayoutParams) params;
                layoutParams.setFlexGrow(1.0f);
            }
        }
    }

    class LoadingFooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        private TextView mInfoTextView;

        public LoadingFooterHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mInfoTextView = itemView.findViewById(R.id.tv_info);
        }
    }

    /**
     * 设置Item点击监听
     *
     * @param listener Item点击监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Item点击监听接口
     */
    public interface OnItemClickListener {
        void onClick(View view, int position);
    }
}
