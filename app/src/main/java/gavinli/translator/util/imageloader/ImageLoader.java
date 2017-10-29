package gavinli.translator.util.imageloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import gavinli.translator.util.imageloader.executor.LoaderExecutor;
import gavinli.translator.util.imageloader.load.DiskCache;
import gavinli.translator.util.imageloader.load.MemoryCache;

/**
 * Created by gavin on 17-8-16.
 */

public class ImageLoader {
    private static volatile ImageLoader mSingleton;
    private final Context mContext;

    private final MemoryCache mMemoryCache;
    private final DiskCache mDiskCache;
    private final Executor mExecutor;
    private final Dispatcher mDispatcher;

    /**
     * 避免瀑布流异步加载图片乱序
     */
    private Map<ImageView, ImageRequestor> mRequestorMap;

    /**
     * {@link ImageLoader}在调用{@link #pause()}后处于暂停状态，
     * 在此期间加载任务不会执行，在调用{@link #resume()}后，
     * 没有被取消的任务会继续执行，而已经取消的任务会直接结束。
     */
    private static volatile boolean sPaused;

    /**
     * 使用{@link Object#wait()}执行暂停操作
     *
     * @see ImageRequestor
     */
    private static final Object sPausedObject = new Object();

    /**
     * 主线程Handler
     */
    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Dispatcher.REQUEST_COMPLETED:
                    ImageRequestor requestor = (ImageRequestor) msg.obj;
                    LoaderTask loaderTask = requestor.getLoaderTask();
                    loaderTask.complete(requestor.getResult());
                    break;
            }
        }
    };

    private ImageLoader(Context context) {
        //防止单例持有Activity的Context导致内存泄露
        mContext = context.getApplicationContext();
        mMemoryCache = new MemoryCache(context);
        mDiskCache = new DiskCache(context);
        mExecutor = new LoaderExecutor();
        mRequestorMap = new WeakHashMap<>();
        mDispatcher = new Dispatcher(mMemoryCache, mRequestorMap);
        sPaused = false;
    }

    public static ImageLoader with(Context context) {
        if(mSingleton == null) {
            synchronized (ImageLoader.class) {
                if(mSingleton == null) {
                    mSingleton = new ImageLoader(context);
                }
            }
        }
        return mSingleton;
    }

    public LoaderTaskBuilder load(String url) {
        return new LoaderTaskBuilder(this, url, mMemoryCache, mDiskCache,
                mExecutor, mRequestorMap, mDispatcher);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 暂停图片加载，如果暂停期间加载任务取消，在恢复后任务会直接结束。
     *
     * @see ImageRequestor
     */
    public static void pause() {
        sPaused = true;
    }

    /**
     * 继续图片加载
     *
     * @see ImageRequestor
     */
    public static void resume() {
        sPaused = false;
        synchronized (sPausedObject) {
            sPausedObject.notifyAll();
        }
    }

    /**
     * @return 是否处于暂停状态
     */
    boolean isPaused() {
        return sPaused;
    }

    Object getPausedObject() {
        return sPausedObject;
    }
}
