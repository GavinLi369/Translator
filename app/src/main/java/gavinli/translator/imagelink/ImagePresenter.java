package gavinli.translator.imagelink;

import java.io.IOException;

import gavinli.translator.data.NetworkImage;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImagePresenter implements ImageContract.Presenter {
    @SuppressWarnings("FieldCanBeLocal")
    private ImageContract.View mView;
    private ImageContract.Model mModel;

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        mModel = model;

        mView.setPresenter(this);
    }

    @Override
    public Observable<NetworkImage> loadImages(int num) {
        return Observable.create((Observable.OnSubscribe<NetworkImage>) subscriber -> {
                for(int i = 0; i < num; i++) {
                    try {
                        NetworkImage imageEntry = mModel.getImageLink();
                        if (imageEntry == null) break;
                        subscriber.onNext(imageEntry);
                    } catch (IOException e) {
                        subscriber.onError(e);
                        return;
                    }
                }
                subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
