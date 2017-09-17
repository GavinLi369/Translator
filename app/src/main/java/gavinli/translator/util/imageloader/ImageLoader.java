package gavinli.translator.util.imageloader;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import gavinli.translator.BuildConfig;

/**
 * Created by gavin on 17-8-16.
 */

public class ImageLoader {
    private static volatile ImageLoader mSingleton;
    private final Context mContext;

    static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final String DISK_CACHE_DIR = "imageloader-cache";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_THREAD_NUM = CPU_COUNT + 1;
    private static final int MAX_THREAD_NUM = 2 * CPU_COUNT + 1;
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int QUEUE_CAPACITY = 30;

    public static final int DEFAULT_SIZE = Integer.MAX_VALUE;

    private final MemoryCache mMemoryCache;
    private final DiskCache mDiskCache;
    private final ExecutorService mExecutorService;

    private ImageLoader(Context context) {
        //防止单例持有Activity的Context导致内存泄露
        mContext = context.getApplicationContext();
        mMemoryCache = new MemoryCache(calculateMemoryCacheSize(mContext));
        final File directory = checkOrCreateDirectory(mContext, DISK_CACHE_DIR);
        mDiskCache = new DiskCache(directory,
                BuildConfig.VERSION_CODE,calculateDiskCacheSize());
        mExecutorService = new ThreadPoolExecutor(CORE_THREAD_NUM,
                MAX_THREAD_NUM, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.DiscardOldestPolicy());
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
        return new Dispatcher(url, mMemoryCache, mDiskCache, mExecutorService);
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
