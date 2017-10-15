package gavinli.translator.util.imageloader.load;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by gavin on 17-8-16.
 */

public class MemoryCache {
    private LruCache<String, Bitmap> mLruCache;

    public MemoryCache(int maxSize) {
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
}
