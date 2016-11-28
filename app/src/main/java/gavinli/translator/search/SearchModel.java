package gavinli.translator.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import gavinli.translator.datebase.WordbookDbHelper;
import gavinli.translator.datebase.WordbookEntry;
import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchModel implements SearchContract.Model {
    private static final String DICTIONARY_URL = "http://dictionary.cambridge.org/search/english/direct/?q=";
    private static final String AUTO_COMPLETE_URL = "http://dictionary.cambridge.org/autocomplete/english/?q=";

    private Context mContext;
    private WordbookDbHelper mDbHelper;

    public SearchModel(Context context) {
        mContext = context;
        mDbHelper = new WordbookDbHelper(context);
    }

    @Override
    public ArrayList<Spanned> getExplain(String word, HtmlDecoder.OnStaredLisenter onStaredLisenter,
                                         HtmlDecoder.OnSpeakedLisenter onSpeakedLisenter)
            throws IOException, IndexOutOfBoundsException {
        Request request = new Request.Builder()
                .url(DICTIONARY_URL + word)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        HtmlDecoder htmlDecoder = new HtmlDecoder(response.body().string(), mContext);
        htmlDecoder.setOnStaredListener(onStaredLisenter);
        htmlDecoder.setOnSpeakedLisenter(onSpeakedLisenter);
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

    @Override
    public boolean wordExisted(String word) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                WordbookEntry._ID,
                WordbookEntry.COLUMN_NAME_WORD
        };
        String selection = WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        Cursor cursor = db.query(WordbookEntry.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null);
        boolean existed = cursor.moveToFirst();
        cursor.close();
        return existed;
    }

    @Override
    public void saveWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WordbookEntry.COLUMN_NAME_WORD, word);
        db.insert(WordbookEntry.TABLE_NAME, null, values);
    }
}
