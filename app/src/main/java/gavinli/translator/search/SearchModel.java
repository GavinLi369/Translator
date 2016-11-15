package gavinli.translator.search;

import android.content.Context;
import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchModel implements SearchContract.Model {
    private static final String DICTIONARY_URL = "http://dictionary.cambridge.org/dictionary/english/";
    private static final String AUTO_COMPLETE_URL = "http://dictionary.cambridge.org/autocomplete/english/?q=";

    private Context mContext;

    public SearchModel(Context context) {
        mContext = context;
    }

    @Override
    public ArrayList<Spanned> getExplain(String word) throws IOException, IndexOutOfBoundsException {
        Request request = new Request.Builder()
                .url(DICTIONARY_URL + word)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        HtmlDecoder htmlDecoder = new HtmlDecoder(response.body().string(), mContext);
        return htmlDecoder.decode();
    }

    @Override
    public ArrayList<String> getComplete(String key) throws IOException, JSONException {
        ArrayList<String> results = new ArrayList<>();
        Request request = new Request.Builder()
                .url(AUTO_COMPLETE_URL + key)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        JSONArray words = new JSONObject(response.body().string()).getJSONArray("results");
        for(int i = 0; i < words.length(); i++) {
            results.add(words.getJSONObject(i).getString("searchtext"));
        }
        return results;
    }
}
