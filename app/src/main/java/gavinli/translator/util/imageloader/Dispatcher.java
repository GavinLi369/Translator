package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;

/**
 * Created by gavin on 17-8-16.
 */

public class Dispatcher {
    private final String mUrl;
    private final MemoryCache mMemoryCache;
    private final DiskCache mDiskCache;
    private final ExecutorService mExecutorService;
    private final String mKey;
    private int mLessThanWidth;
    private int mLessThanHeight;

    public Dispatcher(String url, MemoryCache memoryCache, DiskCache diskCache,
                      ExecutorService executorService) {
        mUrl = url;
        mMemoryCache = memoryCache;
        mDiskCache = diskCache;
        mExecutorService = executorService;
        mKey = getKeyByUrl(mUrl);
        mLessThanWidth = ImageLoader.DEFAULT_SIZE;
        mLessThanHeight = ImageLoader.DEFAULT_SIZE;
    }

    public void into(ImageView imageView) {
        imageView.setTag(mUrl);
        WeakReference<ImageView> imageViewReference = new WeakReference<>(imageView);
        mExecutorService.execute(() -> {
            try {
                Bitmap image = get();
                ImageLoader.HANDLER.post(() -> {
                    ImageView view = imageViewReference.get();
                    if(view != null && view.getTag().equals(mUrl)) {
                        imageView.setImageBitmap(image);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Bitmap get() throws IOException {
        //从内存获取
        Bitmap image = mMemoryCache.get(mKey);
        if(image == null) {
            //从磁盘获取
            image = mDiskCache.get(mKey);
            if(image == null) {
                //从网络获取
                image = NetworkUtil.getBitmap(mUrl, mLessThanWidth, mLessThanHeight);
                if(image == null) return null;
                mDiskCache.put(mKey, image);
            }
            mMemoryCache.put(mKey, image);
        }
        return image;
    }

    /**
     * 限制图片大小
     * @param width 图片最大宽度
     * @param height 图片最大高度
     * @return this
     */
    public Dispatcher lessThan(int width, int height) {
        mLessThanWidth = width;
        mLessThanHeight = height;
        return this;
    }

    private String getKeyByUrl(String url) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] md5 = digest.digest(url.getBytes());
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
