package gavinli.translator.worddetail;

import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.util.ExplainNotFoundException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailPresenter implements WordDetailContract.Presenter {
    private WordDetailContract.View mView;
    private WordDetailContract.Model mModel;

    public WordDetailPresenter(WordDetailContract.View view, WordDetailContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadWordExplain(String word) {
        Observable
                .create((Observable.OnSubscribe<List<CharSequence>>) subscriber -> {
                    try {
                        List<CharSequence> spanneds = mModel.getExplain(word.replace(" ", "-"));
                        subscriber.onNext(spanneds);
                    } catch (ExplainNotFoundException | IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(spanneds ->
                    mView.showWordExplain(spanneds)
                , e -> {
                    if(e instanceof IOException) {
                        mView.showNetworkError();
                    } else if(e instanceof ExplainNotFoundException) {
                        mView.showExplainNotFoundError();
                    }
                });
    }
}
