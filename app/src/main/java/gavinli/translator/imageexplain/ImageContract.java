package gavinli.translator.imageexplain;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageContract {
    interface View extends BaseView<Presenter> {
        void showMoreImages(List<String> urls);

        void showMoreImage(Bitmap bitmap);

        void showNetworkError();
    }

    interface Presenter extends BasePresenter {
        void loadMoreImages();
    }

    interface Model {
        List<String> getImageUrls(int num, int offset) throws IOException;
        List<Bitmap> getImages(int num, int offset) throws IOException;
    }
}
