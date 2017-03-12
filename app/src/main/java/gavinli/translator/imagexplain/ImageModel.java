package gavinli.translator.imagexplain;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;

import gavinli.translator.util.BaiduImageUtil;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageModel implements ImageContract.Model {
    private BaiduImageUtil mBaiduImageUtil;

    public ImageModel(String key) {
        mBaiduImageUtil = new BaiduImageUtil(key);
    }

    @Override
    public List<Bitmap> getImages(int num, int offset) throws IOException {
        return mBaiduImageUtil.getImageByKey(num, offset);
    }
}
