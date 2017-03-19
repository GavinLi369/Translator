package gavinli.translator.imageexplain;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder> {
//    private List<String> mImageUrls = new ArrayList<>();
    private List<Bitmap> mImages = new ArrayList<>();
    private Context mContext;
    private double mWindowWidth;
//    private int mLeftTranslation = 0;

    public ImageRecyclerAdapter(Context context) {
        mContext = context;
        mWindowWidth = mContext.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
//        int translation;
//        if((position & 1) == 0) {
//            mLeftTranslation = (int) (Math.random() * 100);
//            translation = mLeftTranslation;
//        } else {
//            translation = - mLeftTranslation;
//        }
//
//        Glide.with(mContext)
//                .load(mImageUrls.get(position))
//                .fitCenter()
//                .into(holder.mExplainView);

        holder.mExplainView.setImageBitmap(mImages.get(position));
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

//    public void addImageUrls(List<String> imageUrls) {
//        mImageUrls.addAll(imageUrls);
//    }

    public void addImage(Bitmap image) {
        double scale = (mWindowWidth / 2) / image.getWidth();
        Bitmap resizedImage = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), false);
        mImages.add(resizedImage);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mExplainView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mExplainView = (ImageView) itemView.findViewById(R.id.iv_explain);
        }
    }
}
