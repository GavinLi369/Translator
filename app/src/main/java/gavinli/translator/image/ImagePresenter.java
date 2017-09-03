package gavinli.translator.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImagePresenter implements ImageContract.Presenter {
    private ImageContract.View mView;
    private ImageContract.Model mModel;

    private final ThreadPoolExecutor mExecutor;
    private final LoadImageHandler mLoadImageHandler;
    private AtomicInteger mOffset;

    private static final int SHOW_IMAGE = 0;
    private static final int SHOW_PLACEHOLDERS = 1;
    private static final int SHOW_NOT_MORE_IMAGE = 2;
    private static final int SHOW_NETWORK_ERROR = 3;

    public ImagePresenter(ImageContract.View view, ImageContract.Model model) {
        mView = view;
        mModel = model;

        mOffset = new AtomicInteger(0);
        mExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        mLoadImageHandler = new LoadImageHandler(mView);

        mView.setPresenter(this);
    }

    @Override
    public void loadImages(int num) {
        new Thread(() -> {
            Message networkErrorMessage = new Message();
            networkErrorMessage.what = SHOW_NETWORK_ERROR;

            int restLinksNum;
            try {
                restLinksNum = mModel.initImageLinks(num, mOffset.get());
            } catch (IOException e) {
                e.printStackTrace();
                mView.showNetworkError();
                return;
            }
            if(restLinksNum == 0) {
                Message showNotMoreIamgeMessage = new Message();
                showNotMoreIamgeMessage.what = SHOW_NOT_MORE_IMAGE;
                mLoadImageHandler.sendMessage(showNotMoreIamgeMessage);
            } else {
                Message showPlaceholdersMessage = new Message();
                showPlaceholdersMessage.what = SHOW_PLACEHOLDERS;
                showPlaceholdersMessage.arg1 = restLinksNum;
                mLoadImageHandler.sendMessage(showPlaceholdersMessage);

                for(int i = 0; i < restLinksNum; i++) {
                    mExecutor.execute(() -> {
                        try {
                            final int offset = mOffset.getAndIncrement();
                            final Message showImageMessage = Message.obtain();
                            showImageMessage.what = SHOW_IMAGE;
                            showImageMessage.obj = mModel.getImage(offset);
                            showImageMessage.arg1 = offset;
                            mLoadImageHandler.sendMessage(showImageMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            mLoadImageHandler.sendMessage(networkErrorMessage);
                        }
                    });
                }
            }
        }).start();
    }

    static class LoadImageHandler extends Handler {
        private WeakReference<ImageContract.View> mViewWeakReference;

        public LoadImageHandler(ImageContract.View view) {
            mViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_IMAGE: {
                    int offset = msg.arg1;
                    Bitmap image = (Bitmap) msg.obj;
                    mViewWeakReference.get().showImage(image, offset);
                    break;
                }
                case SHOW_PLACEHOLDERS: {
                    int num = msg.arg1;
                    mViewWeakReference.get().showPlaceholders(num);
                    break;
                }
                case SHOW_NOT_MORE_IMAGE: {
                    mViewWeakReference.get().showNotMoreImages();
                    break;
                }
                case SHOW_NETWORK_ERROR: {
                    mViewWeakReference.get().showNetworkError();
                    break;
                }
            }
        }
    }
}
