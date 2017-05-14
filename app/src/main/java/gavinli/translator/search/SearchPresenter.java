package gavinli.translator.search;

import android.os.AsyncTask;
import android.text.Spanned;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gavinli.translator.util.ExplainNotFoundException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchPresenter implements SearchContract.Presenter {
    private SearchContract.View mView;
    private SearchContract.Model mModel;
    private String mCurrentWord = "";

    private Timer mTimer;

    public SearchPresenter(SearchContract.View view, SearchContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadExplain(String word) {
        if(mTimer != null) {
            mTimer.cancel();
        }

        Observable.create((Observable.OnSubscribe<List<Spanned>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getExplain(word.replace(" ", "-")));
            } catch (IOException | ExplainNotFoundException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(spanneds -> {
                    mView.hideBackground();
                    mView.showExplain(spanneds);
                    mCurrentWord = spanneds.get(0).toString();
                }, e -> {
                    if(e instanceof IOException) {
                        mView.showNetworkError();
                    } else if (e instanceof ExplainNotFoundException) {
                        mView.showNotFoundWordError();
                        mCurrentWord = "";
                    }
                });
    }

    @Override
    public void loadChineseExplain(String word) {
        Observable.create((Observable.OnSubscribe<List<Spanned>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getChineseExplain(word.replace(" ", "-")));
            } catch (IOException | ExplainNotFoundException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(spanneds -> {
                    mView.showChineseExplain(spanneds);
                    mCurrentWord = spanneds.get(0).toString();
                }, e -> {
                    if(e instanceof IOException) {
                        mView.showNetworkError();
                    } else if (e instanceof ExplainNotFoundException) {
                        mView.showChineseExplainNotFoundError();
                    }
                });
    }

    @Override
    public void loadAutoComplete(String key, int num) {
        if(mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performLoadCompleted(key, num);
            }
        }, 1000);
    }

    @Override
    public void cancelAutoCompleteIfCompleting() {
        if(mTimer != null) {
            mTimer.cancel();
        }
    }

    private void performLoadCompleted(String key, int num) {Observable.create((Observable.OnSubscribe<List<String>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getComplete(key, num));
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(words ->
                                mView.showSuggestion(words)
                        , e -> {
                            if(e instanceof IOException) {
                                mView.showNetworkError();
                            }
                        });
    }

    @Override
    public void saveWord() {
        if(!mCurrentWord.equals("")) new SaveWordTask(mModel, mView).execute(mCurrentWord);
    }

    static class SaveWordTask extends AsyncTask<String, Void, String> {
        private SoftReference<SearchContract.Model> mModelSoftReference;
        private SoftReference<SearchContract.View> mViewSoftReference;

        public SaveWordTask(SearchContract.Model model, SearchContract.View view) {
            mModelSoftReference = new SoftReference<>(model);
            mViewSoftReference = new SoftReference<>(view);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(!mModelSoftReference.get().wordExisted(strings[0])) {
                mModelSoftReference.get().saveWord(strings[0]);
                return "单词已保存至单词本";
            } else {
                return "单词已存在";
            }
        }

        @Override
        protected void onPostExecute(String info) {
            mViewSoftReference.get().showWordInfo(info);
        }
    }

    @Override
    public String getCurrentWord() {
        return mCurrentWord;
    }
}
