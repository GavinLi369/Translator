package gavinli.translator.search;

import android.os.AsyncTask;
import android.text.Spanned;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchPresenter implements SearchContract.Presenter {
    private SearchContract.View mView;
    private SearchContract.Model mModel;
    private Subscription mAutoComplete;
    private String mCurrentWord = "";

    public SearchPresenter(SearchContract.View view, SearchContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadExplain(String word) {
        if (mAutoComplete != null) {
            mAutoComplete.unsubscribe();
            mAutoComplete = null;
        }
        Observable<List<Spanned>> observable = Observable.create((Observable.OnSubscribe<List<Spanned>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getExplain(word.replace(" ", "-")));
            } catch (IOException | IndexOutOfBoundsException e) {
                e.printStackTrace();
                subscriber.onError(e);
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
                } else if (e instanceof IndexOutOfBoundsException) {
                    mView.showNotFoundWordError();
                    mCurrentWord = "";
                }
            }

            @Override
            public void onNext(List<Spanned> spanneds) {
                mView.hideBackground();
                mView.showExplain(spanneds);
                mCurrentWord = spanneds.get(0).toString();
            }
        });
    }

    @Override
    public void loadChineseExplain(String word) {
        Observable<List<Spanned>> observable = Observable.create((Observable.OnSubscribe<List<Spanned>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getChineseExplain(word.replace(" ", "-")));
            } catch (IOException | IndexOutOfBoundsException e) {
                e.printStackTrace();
                subscriber.onError(e);
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
                } else if (e instanceof IndexOutOfBoundsException) {
                    mView.showChineseExplainNotFoundError();
                }
            }

            @Override
            public void onNext(List<Spanned> spanneds) {
                mView.showChineseExplain(spanneds);
                mCurrentWord = spanneds.get(0).toString();
            }
        });
    }

    @Override
    public void loadAutoComplete(String key, int num) {
        if (mAutoComplete != null) {
            mAutoComplete.unsubscribe();
            mAutoComplete = null;
        }
        Observable<List<String>> observable = Observable.create((Observable.OnSubscribe<List<String>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getComplete(key, num));
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        mAutoComplete = observable.subscribe(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {
                mAutoComplete = null;
            }

            @Override
            public void onError(Throwable e) {
                if(e instanceof IOException) {
                    mView.showNetworkError();
                }
                mAutoComplete = null;
            }

            @Override
            public void onNext(List<String> words) {
                mView.showSuggestion(words);
            }
        });
    }

    @Override
    public void saveWord() {
        Logger.d("clicked");
        if(!mCurrentWord.equals("")) new SaveWordTask().execute(mCurrentWord);
    }

    class SaveWordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if(!mModel.wordExisted(strings[0])) {
                mModel.saveWord(strings[0]);
                return "单词已保存至单词本";
            } else {
                return "单词已存在";
            }
        }

        @Override
        protected void onPostExecute(String info) {
            mView.showWordInfo(info);
        }
    }

    @Override
    public String getCurrentWord() {
        return mCurrentWord;
    }
}
