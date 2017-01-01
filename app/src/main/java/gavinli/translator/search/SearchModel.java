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
import java.util.List;

import gavinli.translator.datebase.WordbookDbHelper;
import gavinli.translator.datebase.WordbookEntry;
import gavinli.translator.util.CambirdgeApi;
import gavinli.translator.util.HtmlDecoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchModel implements SearchContract.Model {
    private Context mContext;
    private WordbookDbHelper mDbHelper;

    public SearchModel(Context context) {
        mContext = context;
        mDbHelper = new WordbookDbHelper(context);
    }

    @Override
    public List<Spanned> getExplain(String word, HtmlDecoder.OnStaredLisenter onStaredLisenter)
            throws IOException, IndexOutOfBoundsException {
        return CambirdgeApi.getExplain(mContext, word, onStaredLisenter);
    }

    @Override
    public List<String> getComplete(String key, int num) throws IOException {
        return CambirdgeApi.getComplete(key, num);
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
