package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by gavin on 17-8-17.
 */

public class DiskCache {
    private DiskLruCache mDiskLruCache;

    public DiskCache(File directory,int appVersion, int maxSize) throws IOException {
        mDiskLruCache = DiskLruCache.open(directory, appVersion, 1, maxSize);
    }

    public Bitmap get(String key) throws IOException {
        try (DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key)) {
            if(snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            } else {
                return null;
            }
        }
    }

    public void put(String key, Bitmap bitmap) throws IOException {
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor == null) return;
        try (OutputStream out = editor.newOutputStream(0)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            editor.commit();
        }
    }
}
