package gavinli.translator.wordbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.datebase.WordbookDbHelper;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookModel implements WordbookContract.Model {
    private WordbookDbHelper mDbHelper;

    public WordbookModel(Context context) {
        mDbHelper = new WordbookDbHelper(context);
    }

    @Override
    public List<String> getWords() {
        List<String> words = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                WordbookDbHelper.WordbookEntry._ID,
                WordbookDbHelper.WordbookEntry.COLUMN_NAME_WORD
        };
        Cursor cursor = db.query(WordbookDbHelper.WordbookEntry.TABLE_NAME,
                projection, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                words.add(cursor.getString(cursor.getColumnIndexOrThrow(
                        WordbookDbHelper.WordbookEntry.COLUMN_NAME_WORD)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return words;
    }

    @Override
    public void removeWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = WordbookDbHelper.WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        db.delete(WordbookDbHelper.WordbookEntry.TABLE_NAME, selection, selectionArg);
    }
}
