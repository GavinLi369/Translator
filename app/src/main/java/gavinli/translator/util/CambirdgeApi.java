package gavinli.translator.util;

import android.content.Context;
import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-1-1.
 */

public class CambirdgeApi {
    private static final String DICTIONARY_URL = "http://dictionary.cambridge.org/search/english/direct/?q=";
    private static final String AUTO_COMPLETE_URL = "http://dictionary.cambridge.org/autocomplete/english/?q=";

    public static List<Spanned> getExplain(Context context, String word, HtmlDecoder.OnStaredLisenter lisenter)
                    throws IOException{
        Request request = new Request.Builder()
                .url(DICTIONARY_URL + word)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        HtmlDecoder htmlDecoder = new HtmlDecoder(response.body().string(), context);
        if(lisenter != null) {
            htmlDecoder.setOnStaredListener(lisenter);
        }
        return htmlDecoder.decode();
    }

    public static List<String> getComplete(String key, int num) throws IOException {
        try {
            List<String> results = new ArrayList<>();
            Request request = new Request.Builder()
                    .url(AUTO_COMPLETE_URL + key)
                    .build();

            Response response = new OkHttpClient().newCall(request).execute();
            JSONArray words = new JSONObject(response.body().string()).getJSONArray("results");
            for (int i = 0; i < words.length() && i < num; i++) {
                results.add(words.getJSONObject(i).getString("searchtext"));
            }
            return results;
        } catch (JSONException e) {
            //不应该出现JSONException;
            throw new RuntimeException(e);
        }
    }
}
