package gavinli.translator.util;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GavinLi
 * on 17-3-19.
 */
//TODO 使用多线程
public abstract class NetworkImageUtil {
    protected List<String> mImageUrls = new ArrayList<>();

    public List<String> getImageUrls() throws IOException {
        if(mImageUrls.size() == 0) {
            buildImageUrl();
        }
        return mImageUrls;
    }

    public abstract List<Bitmap> getImages(int num, int offset) throws IOException;

    protected abstract void buildImageUrl() throws IOException;
}
