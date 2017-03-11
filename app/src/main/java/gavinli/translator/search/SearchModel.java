package gavinli.translator.search;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.datebase.WordbookUtil;
import gavinli.translator.util.CambirdgeApi;
import gavinli.translator.util.HtmlDecoder;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchModel implements SearchContract.Model {
    private Context mContext;
    private WordbookUtil mWordbookUtil;

    public SearchModel(Context context) {
        mContext = context;
        mWordbookUtil = new WordbookUtil(context);
    }

    @Override
    public List<Spanned> getExplain(String word)
            throws IOException, IndexOutOfBoundsException {
        return CambirdgeApi.getExplain(mContext, word,
                PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.key_dictionary), "null"));
    }

    @Override
    public List<String> getComplete(String key, int num) throws IOException {
        return CambirdgeApi.getComplete(key, num);
    }

    @Override
    public boolean wordExisted(String word) {
        return mWordbookUtil.wordExisted(word);
    }

    @Override
    public void saveWord(String word) {
        mWordbookUtil.saveWord(word);
    }
}
