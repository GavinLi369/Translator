package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
 * Created by gavin on 17-8-16.
 */

public class Dispatcher {
    private ImageLoader mImageLoader;
    private final MemoryCache mMemoryCache;
    private final Executor mExecutor;
    private final String mKey;

    /**
     * 线程切换
     */
    private DispatcherHandler mDispatcherHandler;

    /**
     * 图片加载完成
     *
     * @see DispatcherHandler
     */
    public static final int REQUEST_COMPLETED = 0;

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

    public Dispatcher(ImageLoader imageLoader, String url, MemoryCache memoryCache, DiskCache diskCache,
                      Executor executor, Map<ImageView, ImageRequestor> requestorMap) {
        mImageLoader = imageLoader;
        mMemoryCache = memoryCache;
        mExecutor = executor;
        mKey = canculateMd5(url);
        mLoaderTask = new LoaderTask(imageLoader, mKey, url, null,
                ImageLoader.DEFAULT_IMAGE_SIZE, ImageLoader.DEFAULT_IMAGE_SIZE);
        mRequestor = new ImageRequestor(this, diskCache, mLoaderTask);
        mRequestorMap = requestorMap;
    }

    /**
     * 限制图片大小
     *
     * @param width 图片最大宽度
     *
     * @param height 图片最大高度
     */
    public Dispatcher lessThan(int width, int height) {
        mLoaderTask.setMaxWidth(width);
        mLoaderTask.setMaxHeight(height);
        return this;
    }

    /**
     * 设置占位图片
     *
     * @param placeholder 占位图片资源id
     */
    public Dispatcher placeholder(@DrawableRes int placeholder) {
        mPlaceholder = mImageLoader.getContext().getResources().getDrawable(placeholder);
        return this;
    }

    /**
     * 设置占位图片
     *
     * @param placeholder 占位图片
     */
    public Dispatcher placeholder(Drawable placeholder) {
        mPlaceholder = placeholder;
        return this;
    }

    /**
     * 异步图片加载
     *
     * @param imageView 目标ImageView
     */
    public void into(ImageView imageView) {
        Bitmap image = mMemoryCache.get(mKey);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            mDispatcherHandler = new DispatcherHandler(this);
            // 处理异步图片加载错位
            ImageRequestor oldRequestor = mRequestorMap.get(imageView);
            if (oldRequestor != null) {
                oldRequestor.cancel();
            }
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

    private void performComplete(ImageRequestor requestor) {
        if (requestor.isCanceled()) return;

        LoaderTask loaderTask = requestor.getLoaderTask();
        // 从RequestorMap中移除ImageView，防止内存泄露。
        mRequestorMap.remove(loaderTask.getImageView());
        ImageLoader.HANDLER.sendMessage(
                ImageLoader.HANDLER.obtainMessage(REQUEST_COMPLETED, requestor));
    }

    /**
     * ImageRequestor图片加载完成回调
     *
     * @param requestor 图片加载器
     */
    void dipatchComplete(ImageRequestor requestor) {
        mDispatcherHandler.sendMessage(
                mDispatcherHandler.obtainMessage(REQUEST_COMPLETED, requestor));
    }

    /**
     * 由{@link ImageRequestor}调用{@link #dipatchComplete(ImageRequestor)}
     * 发送加载完成Message，完成线程从线程池切换到Dispatcher线程。
     */
    static class DispatcherHandler extends Handler {
        private Dispatcher mDispatcher;

        public DispatcherHandler(Dispatcher dispatcher) {
            mDispatcher = dispatcher;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_COMPLETED:
                    mDispatcher.performComplete((ImageRequestor) msg.obj);
                    break;
            }
        }
    }

    /**
     * 获取目标字符串的MD5表示
     *
     * @param target 目标字符串
     */
    private String canculateMd5(String target) {
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
