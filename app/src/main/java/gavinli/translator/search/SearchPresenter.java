package gavinli.translator.search;

import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import gavinli.translator.data.Explain;
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

    /**
     * 当前正在展示的单词
     */
    private Explain mCurrentExplain;

    /**
     * 单词提示定时器，在用户输入单词500ms后再尝试加载提示信息，
     * 防止提示信息加载过于频繁，导致无谓的流量及性能消耗。
     */
    private final ScheduledExecutorService mExecutor;

    /**
     * 单词提示延迟时间(ms)
     */
    private static final int COMPLETE_DELAY = 500;

    /**
     * 单词提示{@link java.util.concurrent.Future}，
     * 用于取消上一个未完成的提示任务。
     */
    private ScheduledFuture mAutoCompletionFuture;

    public SearchPresenter(SearchContract.View view, SearchContract.Model model) {
        mView = view;
        mModel = model;
        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mView.setPresenter(this);
    }

    @Override
    public void loadExplain(String word) {
        cancelAutoCompleteIfCompleting();

        Observable.create((Observable.OnSubscribe<Explain>) subscriber -> {
            try {
                subscriber.onNext(mModel.getExplain(word.replace(" ", "-")));
            } catch (IOException | ExplainNotFoundException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(explain -> {
                    mCurrentExplain = explain;
                    mView.hideBackground();
                    mView.showExplain(explain);
                }, e -> {
                    if(e instanceof IOException) {
                        mView.showNetworkError();
                    } else if (e instanceof ExplainNotFoundException) {
                        mView.showNotFoundWordError();
                        mCurrentExplain = null;
                    }
                });
    }

    @Override
    public void loadAutoComplete(String key, int num) {
        cancelAutoCompleteIfCompleting();

        mAutoCompletionFuture = mExecutor.schedule(() -> {
            performLoadCompleted(key, num);
        }, COMPLETE_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelAutoCompleteIfCompleting() {
        if (mAutoCompletionFuture != null) {
            mAutoCompletionFuture.cancel(true);
        }
    }

    private void performLoadCompleted(String key, int num) {
        Observable.create((Observable.OnSubscribe<List<String>>) subscriber -> {
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
        if(mCurrentExplain != null) new SaveWordTask(mModel, mView).execute(mCurrentExplain);
    }

    static class SaveWordTask extends AsyncTask<Explain, Void, String> {
        private SoftReference<SearchContract.Model> mModelSoftReference;
        private SoftReference<SearchContract.View> mViewSoftReference;

        public SaveWordTask(SearchContract.Model model, SearchContract.View view) {
            mModelSoftReference = new SoftReference<>(model);
            mViewSoftReference = new SoftReference<>(view);
        }

        @Override
        protected String doInBackground(Explain... explains) {
            if(mModelSoftReference.get().saveWord(explains[0])) {
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
        return mCurrentExplain.getKey();
    }
}
