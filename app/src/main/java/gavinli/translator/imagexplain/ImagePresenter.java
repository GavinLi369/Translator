package gavinli.translator.imagexplain;


import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImagePresenter implements ImageContract.Presenter {
    private ImageContract.View mView;
    private ImageContract.Model mModel;

    private int mOffset;

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        mView.setPresenter(this);
        mModel = model;
    }

    @Override
    public void loadMoreImages() {
        Observable<Bitmap> observable = Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            try {
                for(int i = 0; i < 10; i++) {
                    subscriber.onNext(mModel.getImages(1, mOffset++).get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if(e instanceof IOException) {
                    mView.showNetworkError();
                }
            }

            @Override
            public void onNext(Bitmap bitmap) {
                mView.showMoreImage(bitmap);
            }
        });
    }
}
