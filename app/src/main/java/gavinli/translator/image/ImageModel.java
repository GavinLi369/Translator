package gavinli.translator.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.bumptech.glide.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gavinli.translator.util.PiexelsImageUtil;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageModel implements ImageContract.Model {
    private static final String CACHE_DIR = "image";
    private static final int CACHE_SIZE = 20 * 1024 * 1024;
    private final int BITMAP_SIZE;

    private Context mContext;

    private PiexelsImageUtil mImageUtil;
    private List<String> mImageUrls = null;

    public ImageModel(Context context, String key) {
        mContext = context;
        mImageUtil = new PiexelsImageUtil(key);
        BITMAP_SIZE = (int) (mContext.getResources().getDisplayMetrics().widthPixels / 1.5);
    }

    @Override
    public List<Bitmap> getImages(int num, int offset) throws IOException {
        if(mImageUrls == null) mImageUrls = mImageUtil.getImageLinks();
        List<Bitmap> images = new ArrayList<>();
        final DiskLruCache diskLruCache = DiskLruCache.open(checkOrCreateCacheDir(),
                1, 1, CACHE_SIZE);
        final ExecutorService threadPool = Executors.newFixedThreadPool(num);
        final CountDownLatch latch = new CountDownLatch(num);
        for(String url : mImageUrls.subList(offset, offset + num)) {
            //从缓存获取
            String key = caculateMd5(url);
            DiskLruCache.Value value = diskLruCache.get(key);
            if(value != null) {
                try {
                    FileInputStream inputStream = new FileInputStream(value.getFile(0));
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    images.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            } else {
                threadPool.execute(() -> {
                    try {
                        //从网络获取
                        Bitmap image = loadImageFromNetwork(url);
                        if(image == null) return;
                        //缓存文件
                        synchronized (diskLruCache) {
                            DiskLruCache.Editor editor = diskLruCache.edit(key);
                            cacheBitmapToDisk(image, editor);
                        }
                        synchronized (images) {
                            images.add(image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (latch) {
                            latch.countDown();
                        }
                    }
                });
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            diskLruCache.close();
        }
        return images;
    }

    private Bitmap loadImageFromNetwork(String url) throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("不能在主线程操作网络");
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.connect();
        if(connection.getResponseCode() != 200) {
            throw new IOException("网络连接出错");
        }
        InputStream in = connection.getInputStream();
        //网络流只能读取一次
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while((len = in.read(buffer)) > -1) {
            out.write(buffer, 0, len);
        }
        Bitmap bitmap = loadBitmapFromByteArray(out.toByteArray());
        out.close();
        in.close();
        connection.disconnect();
        return bitmap;
    }

    private Bitmap loadBitmapFromByteArray(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0, data.length, options);
        options.inSampleSize = caculateInSampleSize(options.outWidth, BITMAP_SIZE);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private void cacheBitmapToDisk(@NonNull Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException {
        File file = editor.getFile(0);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
        editor.commit();
        editor.abortUnlessCommitted();
    }

    private int caculateInSampleSize(int width, int reqWidth) {
        int inSampleSize = 1;
        while(width / inSampleSize > reqWidth) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private File checkOrCreateCacheDir() throws IOException {
        File explainCacheDir = new File(mContext.getCacheDir() +
                File.separator + CACHE_DIR);
        if(!explainCacheDir.exists()) {
            if(!explainCacheDir.mkdirs()) {
                throw new IOException("缓存文件夹创建失败");
            }
        }
        return explainCacheDir;
    }

    private String caculateMd5(String target) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
