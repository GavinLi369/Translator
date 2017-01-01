package gavinli.translator.worddetail;

import android.content.Context;
import android.text.Spanned;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.util.CambirdgeApi;
import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        return CambirdgeApi.getExplain(mContext, word, null);
    }
}
