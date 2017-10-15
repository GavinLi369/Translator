package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.os.Looper;
import android.widget.ImageView;

import gavinli.translator.util.imageloader.drawable.FadeDrawable;

/**
 * 图片加载任务实体类
 *
 * Created by gavin on 10/14/17.
 */

public final class LoaderTask {
    private ImageLoader mImageLoader;

    /**
     * 任务标示符
     */
    private String mKey;

    /**
     * 请求url
     */
    private String mUrl;

    /**
     * 目标ImageView
     */
    private ImageView mImageView;

    /**
     * 图片最大宽度，用来缩放图片。
     */
    private int mMaxWidth;

    /**
     * 图片最大高度，用来缩放图片。
     */
    private int mMaxHeight;

    public LoaderTask(ImageLoader imageLoader,
                      String key,
                      String url,
                      ImageView imageView,
                      int maxWidth, int maxHeight) {
        mImageLoader = imageLoader;
        mKey = key;
        mUrl = url;
        mImageView = imageView;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }

    /**
     * 任务处理完成回调函数，必须在主线程回调。
     *
     * @param bitmap 图片
     */
    public void complete(Bitmap bitmap) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("不能在子线程操作UI");
        }
        FadeDrawable.setBitmap(mImageLoader.getContext(), mImageView, bitmap);
    }

    public String getKey() {
        return mKey;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public ImageView getImageView() {
        return mImageView;
    }
}
