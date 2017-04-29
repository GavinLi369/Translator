package gavinli.translator.util;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-1-1.
 */

public class CambirdgeApi {
    public static final String DICTIONARY_ENGLISH_URL = "http://dictionary.cambridge.org/search/english/direct/?q=";
    public static final String DICTIONARY_CHINESE_URL = "http://dictionary.cambridge.org/search/english-chinese-simplified/direct/?q=";
    private static final String EN_AUTO_COMPLETE_URL = "http://dictionary.cambridge.org/autocomplete/english/?q=";
    private static final String CH_AUTO_COMPLETE_URL = "http://dictionary.cambridge.org/autocomplete/english-chinese-simplified/?q=";

    public static String getExplainSource(String word, String url)
            throws IOException{
        Request request = new Request.Builder()
                .url(url + word)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();

        return response.body().string();
    }

    public static List<String> getComplete(Context context, String key, int num)
            throws IOException {
        String dictionary = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.key_dictionary), null);
        if(dictionary == null) throw new RuntimeException("设置不应为空");
        String url = null;
        if(dictionary.equals(context.getResources().getStringArray(R.array.explain_language_values)[0])) {
            url = EN_AUTO_COMPLETE_URL;
        } else if(dictionary.equals(context.getResources().getStringArray(R.array.explain_language_values)[1])){
            url = CH_AUTO_COMPLETE_URL;
        }
        try {
            List<String> results = new ArrayList<>();
            Request request = new Request.Builder()
                    .url(url + key)
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
