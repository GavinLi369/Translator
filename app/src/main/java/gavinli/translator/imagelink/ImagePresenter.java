package gavinli.translator.imagelink;

import java.io.IOException;

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
    public Observable<String> loadImages(int num) {
        return Observable.create((Observable.OnSubscribe<String>) subscriber -> {
                for(int i = 0; i < num; i++) {
                    try {
                        String link = mModel.getImageLink();
                        subscriber.onNext(link);
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
