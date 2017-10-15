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

    private volatile Bitmap mResult;


    private volatile boolean mCanceled = false;

    public ImageRequestor(Dispatcher dispatcher, DiskCache diskCache, LoaderTask loaderTask) {
        mDispatcher = dispatcher;
        mDiskCache = diskCache;
        mLoaderTask = loaderTask;
        mNetworkLoader = new NetworkLoader();
    }

    @Override
    public void run() {
        try {
            mResult = request();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDispatcher.dipatchComplete(this);
    }

    public Bitmap request() throws IOException {
        //从磁盘获取
        Bitmap image = mDiskCache.get(mLoaderTask.getKey());
        if (image == null) {
            //从网络获取
            image = mNetworkLoader.fetchBitmap(mLoaderTask.getUrl(),
                    mLoaderTask.getMaxWidth(), mLoaderTask.getMaxHeight());
            if (image == null) throw new IOException("网络图片解析出错");
            mDiskCache.put(mLoaderTask.getKey(), image);
        }
        return image;
    }

    /**
     * 取消图片加载
     */
    public void cancel() {
        mCanceled = true;
        mNetworkLoader.cancel();
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public LoaderTask getLoaderTask() {
        return mLoaderTask;
    }

    public Bitmap getResult() {
        return mResult;
    }
}
