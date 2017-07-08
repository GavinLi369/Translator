package gavinli.translator.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImagePresenter implements ImageContract.Presenter {
    private ImageContract.View mView;
    private ImageContract.Model mModel;
    private Context mContext;

    private int mOffset = 0;
    private final int PLACE_HOLD_WIDTH;
    private final int PLACE_HOLD_HEIGHT;

    private final int[] mPlaceHolder = {
        R.color.colorPlaceHold1,
        R.color.colorPlaceHold2,
        R.color.colorPlaceHold3,
        R.color.colorPlaceHold4,
        R.color.colorPlaceHold5,
    };

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        mModel = model;
        if(mView instanceof Activity) mContext = (Context) mView;
        PLACE_HOLD_WIDTH = (int) (mContext.getResources().getDisplayMetrics().widthPixels / 2.5);
        PLACE_HOLD_HEIGHT = (int) (PLACE_HOLD_WIDTH / 1.3);
        mView.setPresenter(this);
    }

    @Override
    public void loadMoreImages() {
        if(mOffset > 20) return;
        List<Bitmap> bitmaps = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            Bitmap bitmap = Bitmap.createBitmap(
                    PLACE_HOLD_WIDTH,
                    PLACE_HOLD_HEIGHT,
                    Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(mContext.getResources()
                    .getColor(mPlaceHolder[(int) (Math.random() * 5)]));
            bitmaps.add(bitmap);
        }
        mView.showPlaceHolds(bitmaps);

        performLoadImages();
    }

    private void performLoadImages() {
        Observable.create((Observable.OnSubscribe<List<Bitmap>>) subscriber -> {
            try {
                List<Bitmap> images = mModel.getImages(10, mOffset);
                subscriber.onNext(images);
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }

        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(images -> {
                    for(int i = 0; i < images.size(); i++) {
                        mView.showMoreImage(images.get(i), mOffset++);
                    }
                    if(images.size() % 10 != 0) {
                        mView.removeRangePlaceHolds(mOffset,
                                mOffset + (10 - images.size()));
                    }
                }, e -> {
                    if(e instanceof IOException) {
                        mView.showNetworkError();
                    }
                });
    }
}
