package gavinli.translator.util.imageloader;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.util.Map;

import gavinli.translator.util.imageloader.load.MemoryCache;

/**
 * Created by gavin on 17-8-16.
 */

public class Dispatcher {
    private final MemoryCache mMemoryCache;

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
     * 避免瀑布流异步加载图片乱序
     */
    private Map<ImageView, ImageRequestor> mRequestorMap;

    public Dispatcher(MemoryCache memoryCache, Map<ImageView, ImageRequestor> requestorMap) {
        mMemoryCache = memoryCache;
        mRequestorMap = requestorMap;
        mDispatcherHandler = new Dispatcher.DispatcherHandler(this);
    }

    private void performComplete(ImageRequestor requestor) {
        // 内存缓存
        mMemoryCache.put(requestor.getKey(), requestor.getResult());

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
    void dispatchComplete(ImageRequestor requestor) {
        mDispatcherHandler.sendMessage(
                mDispatcherHandler.obtainMessage(REQUEST_COMPLETED, requestor));
    }

    /**
     * 由{@link ImageRequestor}调用{@link #dispatchComplete(ImageRequestor)}
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
}
