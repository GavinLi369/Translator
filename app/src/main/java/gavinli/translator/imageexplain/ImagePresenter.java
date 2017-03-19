package gavinli.translator.imageexplain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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

    private int offset = 0;

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        mView.setPresenter(this);
        mModel = model;
    }

    @Override
    public void loadMoreImages() {
        Observable<Bitmap> observable = Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            try {
                List<String> urls = mModel.getImageUrls(10, offset);
                offset += 10;
                for(String urlString : urls) {
                    InputStream in = null;
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(urlString);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        in = connection.getInputStream();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (in, null, options);
                        subscriber.onNext(bitmap);
                    } finally {
                        if(in != null) in.close();
                        if(connection != null) connection.disconnect();
                    }
                }

//                List<Bitmap> images = mModel.getImages(10, offset);
//                offset += 10;
//                for(Bitmap image : images) {
//                    subscriber.onNext(image);
//                }
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
                mView.showMoreImage(image);
            }
        });
    }
}
