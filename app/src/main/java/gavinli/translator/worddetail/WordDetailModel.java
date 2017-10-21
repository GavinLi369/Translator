package gavinli.translator.worddetail;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import gavinli.translator.util.ExplainLoader;
import gavinli.translator.util.ExplainNotFoundException;

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
    public List<CharSequence> getExplain(String word)
            throws IOException, ExplainNotFoundException {
        return ExplainLoader
                .with(mContext)
                .search(word)
                .load().getSource();
    }
}
