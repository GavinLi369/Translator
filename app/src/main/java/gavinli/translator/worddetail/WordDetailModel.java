package gavinli.translator.worddetail;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.util.CambirdgeApi;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailModel implements WordDetailContract.Model {
    private Context mContext;

    public WordDetailModel(Context context) {
        mContext = context;
    }

    @Override
    public List<Spanned> getExplain(String word)
            throws IOException, IndexOutOfBoundsException {
        return CambirdgeApi.getExplain(mContext, word, null,
                PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.key_dictionary), "null"));
    }
}
