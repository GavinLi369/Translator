package gavinli.translator.image;

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
        void showImage(Bitmap bitmap, int postion);

        void showPlaceHolds(List<Bitmap> bitmaps);

        void removeRangePlaceHolds(int start, int end);

        void showNetworkError();
    }

    interface Presenter extends BasePresenter {
        void loadMoreImages();
    }

    interface Model {
        List<Bitmap> getImages(int num, int offset) throws IOException;
    }
}
