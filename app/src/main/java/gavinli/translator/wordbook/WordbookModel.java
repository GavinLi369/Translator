package gavinli.translator.wordbook;

import android.content.Context;

import java.util.List;

import gavinli.translator.data.Explain;
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
    public List<Explain> getWords() {
        return mWordbookDb.getWords();
    }

    @Override
    public void removeWord(Explain explain) {
        mWordbookDb.removeWord(explain.getKey());
    }

    @Override
    public void saveWord(Explain explain) {
        mWordbookDb.saveWord(explain.getKey(), explain.getSummary());
    }
}
