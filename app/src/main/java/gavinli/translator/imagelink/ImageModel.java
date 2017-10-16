package gavinli.translator.imagelink;

import android.os.Looper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageModel implements ImageContract.Model {
    private PexelsImageUtil mImageUtil;
    private Queue<NetworkImage> mImageLinks;


    public ImageModel(String key) {
        mImageUtil = new PexelsImageUtil(key);
        mImageLinks = new LinkedList<>();
    }

    @Override
    public NetworkImage getImageLink() throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("不能在主线程操作网络");
        if(mImageLinks.isEmpty()) {
            mImageLinks.addAll(mImageUtil.getImageLinks());
        }
        return mImageLinks.poll();
    }
}
