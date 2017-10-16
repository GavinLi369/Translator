package gavinli.translator.util.imageloader;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import gavinli.translator.BuildConfig;
import gavinli.translator.util.imageloader.load.DiskCache;
import gavinli.translator.util.imageloader.load.MemoryCache;

/**
 * Created by gavin on 17-8-16.
 */

public class ImageLoader {
    private static volatile ImageLoader mSingleton;
    private final Context mContext;

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
    private static final String DISK_CACHE_DIR = "imageloader-cache";

    public static final int DEFAULT_IMAGE_SIZE = Integer.MAX_VALUE;

    private final MemoryCache mMemoryCache;
    private DiskCache mDiskCache;
    private final Executor mExecutor;

    /**
     * 避免瀑布流异步加载图片乱序
     */
    private Map<ImageView, ImageRequestor> mRequestorMap;

    private ImageLoader(Context context) {
        //防止单例持有Activity的Context导致内存泄露
        mContext = context.getApplicationContext();
        mMemoryCache = new MemoryCache(calculateMemoryCacheSize(mContext));
        final File directory = checkOrCreateDirectory(mContext, DISK_CACHE_DIR);
        try {
            mDiskCache = new DiskCache(directory,
                    BuildConfig.VERSION_CODE, calculateDiskCacheSize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mExecutor = new LoaderExecutor();
        mRequestorMap = new WeakHashMap<>();
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

    public Dispatcher load(String url) {
        return new Dispatcher(this, url, mMemoryCache, mDiskCache, mExecutor, mRequestorMap);
    }

    public Context getContext() {
        return mContext;
    }

    private File checkOrCreateDirectory(Context context, String dir) {
        File file = new File(context.getCacheDir(), dir);
        if(!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private int calculateMemoryCacheSize(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memorySize = activityManager.getMemoryClass() * 1024 * 1024;
        return memorySize / 8;
    }

    private int calculateDiskCacheSize() {
        return 20 * 1024 * 1024;
    }
}
