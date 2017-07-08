package gavinli.translator.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import gavinli.translator.R;


/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImagePresenter implements ImageContract.Presenter {
    private ImageContract.View mView;
    private ImageContract.Model mModel;
    private Context mContext;

    private final ThreadPoolExecutor mExecutor;
    private final LoadImageHandler mLoadImageHandler;

    private AtomicInteger mOffset;
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

        mOffset = new AtomicInteger(0);
        mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        mLoadImageHandler = new LoadImageHandler(mView);

        mView.setPresenter(this);
    }

    @Override
    public void loadMoreImages() {
        final int num = 10;
        if(mOffset.get() > 20) return;
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

        performLoadImages(num);
    }

    private void performLoadImages(int num) {
        for(int i = 0; i < num; i++) {
            mExecutor.execute(() -> {
                try {
                    final int offset = mOffset.getAndIncrement();
                    List<Bitmap> images = mModel.getImages(1, offset);
                    Message message = new Message();
                    message.obj = images.get(0);
                    message.arg1 = offset;
                    mLoadImageHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    static class LoadImageHandler extends Handler {
        private WeakReference<ImageContract.View> mViewWeakReference;

        public LoadImageHandler(ImageContract.View view) {
            mViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            int offset = msg.arg1;
            Bitmap image = (Bitmap) msg.obj;
            mViewWeakReference.get().showImage(image, offset);
        }
    }
}
