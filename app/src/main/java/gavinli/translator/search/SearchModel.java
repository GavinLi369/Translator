package gavinli.translator.search;

import android.content.Context;
import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.data.source.datebase.WordbookDb;
import gavinli.translator.data.source.remote.CambirdgeSource;
import gavinli.translator.util.ExplainLoader;
import gavinli.translator.util.ExplainNotFoundException;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchModel implements SearchContract.Model {
    private Context mContext;
    private WordbookDb mWordbookDb;

    public SearchModel(Context context) {
        mContext = context;
        mWordbookDb = new WordbookDb(context);
    }

    @Override
    public List<Spanned> getExplain(String word)
            throws IOException, ExplainNotFoundException {
        return ExplainLoader
                .with(mContext)
                .search(word)
                .load();
    }

    @Override
    public List<String> getComplete(String key, int num) throws IOException {
        return CambirdgeSource.getComplete(mContext, key, num);
    }

    @Override
    public boolean wordExisted(String word) {
        return mWordbookDb.wordExisted(word);
    }

    @Override
    public void saveWord(String word) {
        mWordbookDb.saveWord(word);
    }
}
