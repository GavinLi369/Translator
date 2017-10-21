package gavinli.translator.wordbook;

import android.os.AsyncTask;

import java.lang.ref.SoftReference;
import java.util.List;

import gavinli.translator.data.Explain;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookPresenter implements WordbookContract.Presenter {
    private WordbookContract.View mView;
    private WordbookContract.Model mModel;

    /**
     * 最后一个被删除的单词，用于UNDO操作。
     */
    private Explain mPreExplain;

    public WordbookPresenter(WordbookContract.View view, WordbookContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadWords() {
        new LoadWordsTask(mModel, mView).execute();
    }

    static class LoadWordsTask extends AsyncTask<Void, Void, List<Explain>> {
        private SoftReference<WordbookContract.Model> mModelSoftReference;
        private SoftReference<WordbookContract.View> mViewSoftReference;

        public LoadWordsTask(WordbookContract.Model model, WordbookContract.View view) {
            mModelSoftReference = new SoftReference<>(model);
            mViewSoftReference = new SoftReference<>(view);
        }

        @Override
        protected List<Explain> doInBackground(Void... voids) {
            return mModelSoftReference.get().getWords();
        }

        @Override
        protected void onPostExecute(List<Explain> words) {
            if(words.size() == 0) {
                mViewSoftReference.get().showBackground();
            } else {
                mViewSoftReference.get().hideBackground();
                mViewSoftReference.get().showWords(words);
            }
        }
    }

    @Override
    public void removeWord(Explain word) {
        mPreExplain = word;
        new Thread(() -> mModel.removeWord(word)).start();
    }

    @Override
    public void restoreWord() {
        if (mPreExplain != null) {
            mModel.saveWord(mPreExplain);
        }
    }
}
