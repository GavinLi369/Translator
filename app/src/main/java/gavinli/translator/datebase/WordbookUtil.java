package gavinli.translator.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by GavinLi
 * on 17-3-9.
 */

public class WordbookUtil {
    private WordbookDbHelper mDbHelper;

    public WordbookUtil(Context context) {
        mDbHelper = new WordbookDbHelper(context);
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

    public void saveWord(String word) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WordbookEntry.COLUMN_NAME_WORD, word);
        db.insert(WordbookEntry.TABLE_NAME, null, values);
    }
}
