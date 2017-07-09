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
        void showPlaceholders(int num);

        void showImage(Bitmap bitmap, int postion);

        void showNotMoreImages();

        void showNetworkError();
    }

    interface Presenter extends BasePresenter {
        void loadImages(int num);
    }

    interface Model {
        int initImageLinks(int num, int offset) throws IOException;

        Bitmap getImage(int offset) throws IOException;
    }
}
