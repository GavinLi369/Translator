package gavinli.translator.worddetail;

import android.content.Context;
import android.text.Spanned;

import java.io.IOException;
import java.util.ArrayList;

import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailModel implements WordDetailContract.Model {
    private static final String DICTIONARY_URL = "http://dictionary.cambridge.org/search/english/direct/?q=";

    private Context mContext;

    public WordDetailModel(Context context) {
        mContext = context;
    }

    @Override
    public ArrayList<Spanned> getExplain(String word, HtmlDecoder.OnSpeakedLisenter onSpeakedLisenter)
            throws IOException, IndexOutOfBoundsException {
        Request request = new Request.Builder()
                .url(DICTIONARY_URL + word)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        HtmlDecoder htmlDecoder = new HtmlDecoder(response.body().string(), mContext);
        htmlDecoder.setOnSpeakedLisenter(onSpeakedLisenter);
        return htmlDecoder.decode();
    }
}
