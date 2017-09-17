package gavinli.translator.imagelink;

import android.os.Looper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import gavinli.translator.util.PexelsImageUtil;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageModel implements ImageContract.Model {
    private PexelsImageUtil mImageUtil;
    private Queue<String> mImageLinks;


    public ImageModel(String key) {
        mImageUtil = new PexelsImageUtil(key);
        mImageLinks = new LinkedList<>();
    }

    @Override
    public String getImageLink() throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("不能在主线程操作网络");
        if(mImageLinks.isEmpty()) {
            mImageLinks.addAll(mImageUtil.getImageLinks());
        }
        return mImageLinks.poll();
    }
}
