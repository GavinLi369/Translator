package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.Executor;

import gavinli.translator.util.imageloader.load.DiskCache;
import gavinli.translator.util.imageloader.load.MemoryCache;

/**
 * Created by gavin on 10/29/17.
 */

public class LoaderTaskBuilder {
    private final ImageLoader mImageLoader;
    private final MemoryCache mMemoryCache;
    private final Executor mExecutor;
    private final String mKey;

    /**
     * 图片加载完成前的占位图片
     */
    private Drawable mPlaceholder;

    /**
     * 图片加载任务实体类
     */
    private LoaderTask mLoaderTask;

    /**
     * 图片加载请求类
     */
    private ImageRequestor mRequestor;

    /**
     * 避免瀑布流异步加载图片乱序
     */
    private Map<ImageView, ImageRequestor> mRequestorMap;

    public LoaderTaskBuilder(ImageLoader imageLoader, String url, MemoryCache memoryCache, DiskCache diskCache,
                             Executor executor, Map<ImageView, ImageRequestor> requestorMap, Dispatcher dispatcher) {
        mImageLoader = imageLoader;
        mMemoryCache = memoryCache;
        mExecutor = executor;
        mKey = calculateMd5(url);
        mLoaderTask = new LoaderTask(imageLoader, mKey, url);
        mRequestor = new ImageRequestor(imageLoader, dispatcher, diskCache, mLoaderTask);
        mRequestorMap = requestorMap;
    }

    /**
     * 更改图片大小
     *
     * @param width 图片宽度
     *
     * @param height 图片高度
     */
    public LoaderTaskBuilder resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return this;
        }
        mLoaderTask.setShouldResize(true);
        mLoaderTask.setTargetWidth(width);
        mLoaderTask.setTargetHeight(height);
        return this;
    }

    /**
     * 设置占位图片
     *
     * @param placeholder 占位图片资源id
     */
    public LoaderTaskBuilder placeholder(@DrawableRes int placeholder) {
        mPlaceholder = mImageLoader.getContext().getResources().getDrawable(placeholder);
        return this;
    }

    /**
     * 设置占位图片
     *
     * @param placeholder 占位图片
     */
    public LoaderTaskBuilder placeholder(Drawable placeholder) {
        mPlaceholder = placeholder;
        return this;
    }

    /**
     * 异步图片加载
     *
     * @param imageView 目标ImageView
     */
    public void into(ImageView imageView) {
        // 处理异步图片加载错位
        ImageRequestor oldRequestor = mRequestorMap.get(imageView);
        if (oldRequestor != null) {
            oldRequestor.cancel();
        }
        Bitmap image = mMemoryCache.get(mKey);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            mRequestorMap.put(imageView, mRequestor);
            imageView.setImageDrawable(mPlaceholder);
            mLoaderTask.setImageView(imageView);
            mExecutor.execute(mRequestor);
        }
    }

    /**
     * 同步图片加载
     *
     * @throws IOException 磁盘加载错误，网络连接错误，网络图片解析错误
     */
    public Bitmap get() throws IOException {
        //从内存获取
        Bitmap image = mMemoryCache.get(mKey);
        if(image == null) {
            image = mRequestor.request();
            mMemoryCache.put(mKey, image);
        }
        return image;
    }

    /**
     * 获取目标字符串的MD5表示
     *
     * @param target 目标字符串
     */
    private String calculateMd5(String target) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] md5 = digest.digest(target.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte aMd5 : md5) {
            if ((0xFF & aMd5) < 0x10) {
                result.append('0').append(Integer.toHexString(0xFF & aMd5));
            } else {
                result.append(Integer.toHexString(0xFF & aMd5));
            }
        }
        return result.toString();
    }
}
