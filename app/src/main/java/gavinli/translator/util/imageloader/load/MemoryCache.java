package gavinli.translator.util.imageloader.load;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by gavin on 17-8-16.
 */

public class MemoryCache {
    private LruCache<String, Bitmap> mLruCache;

    public MemoryCache(Context context) {
        int maxSize = calculateMemoryCacheSize(context);
        mLruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public Bitmap get(String key) {
        return mLruCache.get(key);
    }

    public void put(String key, Bitmap bitmap) {
        mLruCache.put(key, bitmap);
    }

    /**
     * 根据应用可用内存计算内存缓存大小
     *
     * @return 应用可用内存的1/8
     */
    private int calculateMemoryCacheSize(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memorySize = activityManager.getMemoryClass() * 1024 * 1024;
        return memorySize / 8;
    }
}
