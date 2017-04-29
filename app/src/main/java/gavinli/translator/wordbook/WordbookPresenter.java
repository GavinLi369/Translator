package gavinli.translator.wordbook;

import android.os.AsyncTask;

import java.util.List;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookPresenter implements WordbookContract.Presenter {
    private WordbookContract.View mView;
    private WordbookContract.Model mModel;

    public WordbookPresenter(WordbookContract.View view, WordbookContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadWords() {
        new LoadWordsTask().execute();
    }

    class LoadWordsTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            return mModel.getWords();
        }

        @Override
        protected void onPostExecute(List<String> words) {
            if(words.size() == 0) {
                mView.showBackground();
            } else {
                mView.hideBackground();
                mView.showWords(words);
            }
        }
    }

    @Override
    public void removeWord(String word) {
        new Thread(() -> mModel.removeWord(word)).start();
    }
}
