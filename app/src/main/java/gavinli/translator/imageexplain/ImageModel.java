package gavinli.translator.imageexplain;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;

import gavinli.translator.util.BaiduImageUtil;
import gavinli.translator.util.GoogleImageUtil;
import gavinli.translator.util.NetworkImageUtil;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageModel implements ImageContract.Model {
    private NetworkImageUtil mNetworkImageUtil;
    private List<String> mImageUrls = null;

    public ImageModel(String key) {
        mNetworkImageUtil = new GoogleImageUtil(key);
    }

    @Override
    public List<String> getImageUrls(int num, int offset) throws IOException {
        if(mImageUrls == null) mImageUrls = mNetworkImageUtil.getImageUrls();
        return mImageUrls.subList(offset, offset + num);
    }

    public List<Bitmap> getImages(int num, int offset) throws IOException {
        if(mNetworkImageUtil instanceof GoogleImageUtil)
            return ((GoogleImageUtil) mNetworkImageUtil).getImages()
                    .subList(offset, offset + num);
        return null;
    }
}
