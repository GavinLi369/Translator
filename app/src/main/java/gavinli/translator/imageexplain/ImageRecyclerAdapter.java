package gavinli.translator.imageexplain;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder> {
    private List<Bitmap> mImages = new ArrayList<>();
    private Context mContext;
    private OnItemClickLinstener mLinstener;

    public ImageRecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.mExplainView.setImageBitmap(mImages.get(position));
        ViewGroup.LayoutParams params = holder.mExplainView.getLayoutParams();
        if(params instanceof FlexboxLayoutManager.LayoutParams) {
            FlexboxLayoutManager.LayoutParams layoutParams = (FlexboxLayoutManager.LayoutParams) params;
            layoutParams.setFlexGrow(1.0f);
        }
        holder.mExplainView.setOnClickListener(view -> {
            if (mLinstener != null) mLinstener.onClick(holder.mExplainView, position);
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public void setImage(Bitmap image, int postion) {
        mImages.set(postion, image);
    }

    public void addImages(List<Bitmap> images) {
        mImages.addAll(images);
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

    public void setOnItemClickLinstener(OnItemClickLinstener linstener) {
        mLinstener = linstener;
    }

    public interface OnItemClickLinstener {
        void onClick(View view, int postion);
    }
}
