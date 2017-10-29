package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;

import java.io.IOException;

import gavinli.translator.util.imageloader.load.DiskCache;
import gavinli.translator.util.imageloader.load.NetworkLoader;

/**
 * Created by gavin on 10/15/17.
 */

public class ImageRequestor implements Runnable {
    private Dispatcher mDispatcher;
    private DiskCache mDiskCache;
    private LoaderTask mLoaderTask;
    private NetworkLoader mNetworkLoader;

    private String mKey;
    private volatile Bitmap mResult;


    private volatile boolean mCanceled = false;

    public ImageRequestor(Dispatcher dispatcher, DiskCache diskCache, LoaderTask loaderTask) {
        mDispatcher = dispatcher;
        mDiskCache = diskCache;
        mNetworkLoader = new NetworkLoader();
        mLoaderTask = loaderTask;
        mKey = loaderTask.getKey();
    }

    @Override
    public void run() {
        try {
            mResult = request();
            mDispatcher.dispatchComplete(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步请求图片数据
     *
     * @return Bitmap图片
     *
     * @throws IOException 网络连接出错，图片解析出错或磁盘I/O出错
     */
    public Bitmap request() throws IOException {
        //从磁盘获取
        Bitmap image = mDiskCache.get(mLoaderTask.getKey());
        if (image == null) {
            //从网络获取
            image = mNetworkLoader.fetchBitmap(mLoaderTask);
            if (image == null) throw new IOException("网络图片解析出错");
            mDiskCache.put(mLoaderTask.getKey(), image);
        }
        // 返回转换后的图片
        return transform(image);
    }

    /**
     * 取消图片加载
     */
    public void cancel() {
        mCanceled = true;
        mNetworkLoader.cancel();
    }

    /**
     * 将图片进行转换，这里只是将图片缩放为指定大小
     *
     * @param target 原始图片
     *
     * @return 转换后的图片
     */
    private Bitmap transform(Bitmap target) {
        if (mLoaderTask.shouldResize()) {
            int reqWidth = mLoaderTask.getTargetWidth();
            int reqHeight = mLoaderTask.getTargetHeight();
            return Bitmap.createScaledBitmap(target, reqWidth, reqHeight, false);
        } else {
            return target;
        }
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public LoaderTask getLoaderTask() {
        return mLoaderTask;
    }

    public String getKey() {
        return mKey;
    }

    public Bitmap getResult() {
        return mResult;
    }
}
