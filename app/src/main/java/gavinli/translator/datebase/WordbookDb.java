package gavinli.translator.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GavinLi
 * on 17-3-9.
 */

public class WordbookDb {
    private TranslatorDbHelper mDbHelper;

    public WordbookDb(Context context) {
        mDbHelper = new TranslatorDbHelper(context);
    }

    public List<String> getWords() {
        List<String> words = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                TranslatorDbHelper.WordbookEntry._ID,
                TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD
        };
        Cursor cursor = db.query(TranslatorDbHelper.WordbookEntry.TABLE_NAME,
                projection, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                words.add(cursor.getString(cursor.getColumnIndexOrThrow(
                        TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return words;
    }

    public boolean wordExisted(String word) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                TranslatorDbHelper.WordbookEntry._ID,
                TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD
        };
        String selection = TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        Cursor cursor = db.query(TranslatorDbHelper.WordbookEntry.TABLE_NAME,
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

    public void saveWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD, word);
        db.insert(TranslatorDbHelper.WordbookEntry.TABLE_NAME, null, values);
    }

    public void removeWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = TranslatorDbHelper.WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        db.delete(TranslatorDbHelper.WordbookEntry.TABLE_NAME, selection, selectionArg);
    }
}
