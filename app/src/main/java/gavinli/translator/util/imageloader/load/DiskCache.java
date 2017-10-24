package gavinli.translator.util.imageloader.load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import gavinli.translator.BuildConfig;

/**
 * Bitmap磁盘缓存，使用{@link DiskLruCache}实现。
 *
 * Created by gavin on 17-8-17.
 */

public class DiskCache {
    private DiskLruCache mDiskLruCache;

    /**
     * 缓存目录名
     */
    private static final String DISK_CACHE_DIR = "imageloader-cache";

    /**
     * 默认缓存大小(MB)
     */
    private static final int DEFAULT_MAX_SIZE = 20;

    public DiskCache(Context context)  {
        final int maxSize = calculateDiskCacheSize();
        final File directory = checkOrCreateDirectory(context, DISK_CACHE_DIR);
        try {
            mDiskLruCache = DiskLruCache.open(directory,
                    BuildConfig.VERSION_CODE, 1, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据key值，返回磁盘缓存
     *
     * @param key 缓存对应键值
     *
     * @return 磁盘缓存图片Bitmap对象，如果没有则返回null。
     *
     * @throws IOException 磁盘IO流出错
     */
    public Bitmap get(String key) throws IOException {
        if (mDiskLruCache == null) {
            return null;
        }
        try (DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key)) {
            if(snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            } else {
                return null;
            }
        }
    }

    /**
     * 将Bitmap对象置入磁盘缓存
     *
     * @param key 缓存对应键值
     *
     * @param bitmap 图片Bitmap对象
     *
     * @throws IOException 磁盘IO流出错
     */
    public void put(String key, Bitmap bitmap) throws IOException {
        if (mDiskLruCache == null) {
            return;
        }
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor == null) return;
        try (OutputStream out = editor.newOutputStream(0)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            editor.commit();
        }
    }

    /**
     * 检查缓存目录， 如果目录不存在则创建。
     *
     * @param dir 缓存目录路径
     *
     * @return 缓存目录
     */
    private File checkOrCreateDirectory(Context context, String dir) {
        File file = new File(context.getCacheDir(), dir);
        if(!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 计算缓存大小
     *
     * @return 缓存大小(B)
     */
    private int calculateDiskCacheSize() {
        return DEFAULT_MAX_SIZE * 1024 * 1024;
    }
}
