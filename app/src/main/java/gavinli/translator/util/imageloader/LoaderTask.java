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
     * 是否需要被缩放
     */
    private boolean mShouldResize;

    /**
     * 图片宽度，用来缩放图片。
     */
    private int mTargetWidth;

    /**
     * 图片高度，用来缩放图片。
     */
    private int mTargetHeight;

    public LoaderTask(ImageLoader imageLoader,
                      String key,
                      String url,
                      ImageView imageView,
                      int targetWidth, int targetHeight) {
        mImageLoader = imageLoader;
        mKey = key;
        mUrl = url;
        mImageView = imageView;
        mShouldResize = false;
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
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

    public boolean shouldResize() {
        return mShouldResize;
    }

    public void setShouldResize(boolean shouldResize) {
        mShouldResize = shouldResize;
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public void setTargetWidth(int targetWidth) {
        mTargetWidth = targetWidth;
    }

    public void setTargetHeight(int targetHeight) {
        mTargetHeight = targetHeight;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public ImageView getImageView() {
        return mImageView;
    }
}
