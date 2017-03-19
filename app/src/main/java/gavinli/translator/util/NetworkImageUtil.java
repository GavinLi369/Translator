package gavinli.translator.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GavinLi
 * on 17-3-19.
 */

public abstract class NetworkImageUtil {
    protected List<String> mImageUrls = new ArrayList<>();

    public List<String> getImageUrls() throws IOException {
        if(mImageUrls.size() == 0) {
            buildImageUrl();
        }
        return mImageUrls;
    }

    protected abstract void buildImageUrl() throws IOException;
}
