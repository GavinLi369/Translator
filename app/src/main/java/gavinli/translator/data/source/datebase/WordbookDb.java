package gavinli.translator.data.source.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.data.Explain;

/**
 * Created by GavinLi
 * on 17-3-9.
 */

public class WordbookDb {
    private TranslatorDbHelper mDbHelper;

    public WordbookDb(Context context) {
        mDbHelper = new TranslatorDbHelper(context);
    }

    public List<Explain> getWords() {
        List<Explain> words = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                WordbookEntry._ID,
                WordbookEntry.COLUMN_NAME_WORD,
                WordbookEntry.COLUMN_NAME_SUMMARY,
        };
        Cursor cursor = db.query(WordbookEntry.TABLE_NAME,
                projection, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                Explain explain = new Explain();
                explain.setKey(cursor.getString(cursor.getColumnIndexOrThrow(
                        WordbookEntry.COLUMN_NAME_WORD)));
                explain.setSummary(cursor.getString(cursor.getColumnIndexOrThrow(
                        WordbookEntry.COLUMN_NAME_SUMMARY)));
                words.add(explain);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return words;
    }

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

    public void saveWord(String word, String summary) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WordbookEntry.COLUMN_NAME_WORD, word);
        values.put(WordbookEntry.COLUMN_NAME_SUMMARY, summary);
        db.insert(WordbookEntry.TABLE_NAME, null, values);
    }

    public void removeWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        db.delete(WordbookEntry.TABLE_NAME, selection, selectionArg);
    }

    public static class WordbookEntry implements BaseColumns {
        public static final String TABLE_NAME = "wordbook";
        public static final String COLUMN_NAME_WORD = "word";
        public static final String COLUMN_NAME_SUMMARY = "summary";
    }
}
