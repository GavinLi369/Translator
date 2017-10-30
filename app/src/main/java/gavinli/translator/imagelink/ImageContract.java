package gavinli.translator.imagelink;

import java.io.IOException;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.data.NetworkImage;
import rx.Observable;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageContract {
    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {
        Observable<NetworkImage> loadImages(int num);
    }

    interface Model {
        NetworkImage getImageLink() throws IOException;
    }
}
