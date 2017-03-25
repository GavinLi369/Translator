package gavinli.translator.imageexplain;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
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
    private Context mContext;

    private int mOffset = 0;
    private static final int PLACE_HOLD_WIDTH = 230;
    private static final int PLACE_HOLD_HEIGHT = 170;

    private final int[] mPlaceHolder = {
        R.color.colorPlaceHold1,
        R.color.colorPlaceHold2,
        R.color.colorPlaceHold3,
        R.color.colorPlaceHold4,
        R.color.colorPlaceHold5,
    };

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        if(mView instanceof Activity) mContext = (Context) mView;
        mModel = model;
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
            //noinspection deprecation
            bitmap.eraseColor(mContext.getResources()
                    .getColor(mPlaceHolder[(int) (Math.random() * 5)]));
            bitmaps.add(bitmap);
        }
        mView.showPlaceHolds(bitmaps);

        performLoadImages();
    }

    private void performLoadImages() {
        Observable<Bitmap> observable = Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            try {
                List<String> urls = mModel.getImageUrls(10, mOffset);
                for(String urlString : urls) {
                    HttpURLConnection connection = null;
                    InputStream in = null;
                    try {
                        URL url = new URL(urlString);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        in = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        double scale = 0.75;
                        Bitmap resized = Bitmap.createScaledBitmap(bitmap,
                                (int) (bitmap.getWidth() * scale),
                                (int) (bitmap.getHeight() * scale),
                                false);
                        subscriber.onNext(resized);
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    } finally {
                        if(in != null) in.close();
                        if(connection != null) connection.disconnect();
                    }
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
            public void onNext(Bitmap image) {
                mView.showMoreImage(image, mOffset++);
            }
        });
    }
}
