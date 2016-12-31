package gavinli.translator.worddetail;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailPresenter implements WordDetailContract.Presenter {
    private WordDetailContract.View mView;
    private WordDetailContract.Model mModel;
    private Context mContext;

    public WordDetailPresenter(WordDetailContract.View view, WordDetailContract.Model model,
                               Context context) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
        mContext = context;
    }

    @Override
    public void loadWordExplain(String word) {
        Observable<List<Spanned>> observable = Observable.create(new Observable.OnSubscribe<List<Spanned>>() {
            @Override
            public void call(Subscriber<? super List<Spanned>> subscriber) {
                try {
                    List<Spanned> spanneds = mModel.getExplain(word.replace(" ", "-"), url -> onSpeaked(url));
                    subscriber.onNext(spanneds);
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Subscriber<List<Spanned>>() {
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
            public void onNext(List<Spanned> spanneds) {
                mView.showWordExplain(spanneds);
            }
        });
    }

    private void onSpeaked(String url) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(url));
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }
}
