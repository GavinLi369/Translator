package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.os.Looper;
import android.widget.ImageView;

/**
 * 图片加载任务实体类
 *
 * Created by gavin on 10/14/17.
 */

public final class LoaderTask {
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

    public int postion;

    public LoaderTask(String key, String url, ImageView imageView, int maxWidth, int maxHeight) {
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
        mImageView.setImageBitmap(bitmap);
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
