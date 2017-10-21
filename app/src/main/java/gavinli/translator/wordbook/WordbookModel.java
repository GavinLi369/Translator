package gavinli.translator.wordbook;

import android.content.Context;

import java.util.List;

import gavinli.translator.data.source.datebase.WordbookDb;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookModel implements WordbookContract.Model {
    private WordbookDb mWordbookDb;

    public WordbookModel(Context context) {
        mWordbookDb = new WordbookDb(context);
    }

    @Override
    public List<String> getWords() {
        return mWordbookDb.getWords();
    }

    @Override
    public void removeWord(String word) {
        mWordbookDb.removeWord(word);
    }

    @Override
    public void saveWord(String word) {
        mWordbookDb.saveWord(word);
    }
}
