package gavinli.translator.wordbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import gavinli.translator.datebase.WordbookDbHelper;
import gavinli.translator.datebase.WordbookEntry;

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
    public ArrayList<String> getWords() {
        ArrayList<String> words = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                WordbookEntry._ID,
                WordbookEntry.COLUMN_NAME_WORD
        };
        Cursor cursor = db.query(WordbookEntry.TABLE_NAME,
                projection, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                words.add(cursor.getString(cursor.getColumnIndexOrThrow(
                        WordbookEntry.COLUMN_NAME_WORD)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return words;
    }

    @Override
    public void removeWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = WordbookEntry.COLUMN_NAME_WORD + " = ?";
        String[] selectionArg = { word };
        db.delete(WordbookEntry.TABLE_NAME, selection, selectionArg);
    }
}
