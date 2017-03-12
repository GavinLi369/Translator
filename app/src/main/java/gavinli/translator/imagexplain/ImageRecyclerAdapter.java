package gavinli.translator.imagexplain;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder> {
    private List<Bitmap> mImageExplains = new ArrayList<>();
    private Context mContext;
    private double mWindowWidth;

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
        holder.mExplainView.setImageBitmap(mImageExplains.get(position));
    }

    @Override
    public int getItemCount() {
        return mImageExplains.size();
    }

    public void addImageExplain(Bitmap image) {
        double scale = (mWindowWidth / 2) / image.getWidth();
        Bitmap resizedImage = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), false);
        mImageExplains.add(resizedImage);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mExplainView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mExplainView = (ImageView) itemView.findViewById(R.id.iv_explain);
        }
    }
}
