package gavinli.translator.data.source.datebase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class TranslatorDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WordbookEntry.TABLE_NAME + " ("
            + WordbookEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
            + WordbookEntry.COLUMN_NAME_WORD + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + WordbookEntry.TABLE_NAME;

    private static final String CREATE_ACCOUNT_TABLE =
            "CREATE TABLE " + AccountEntry.TABLE_NAME + " (" +
                    AccountEntry._ID + " CHAR(20)" + " PRIMARY KEY" + COMMA_SEP +
                    AccountEntry.COLUMN_NAME + " CHAR(20)" + COMMA_SEP +
                    AccountEntry.COLUMN_PASSWORD + " CHAR(20)" + COMMA_SEP +
                    AccountEntry.COLUNM_FACE + TEXT_TYPE + " )";
    private static final String DELETE_ACCOUNT_TABLE =
            "DROP TABLE IF EXISTS " + AccountEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "translator.db";

    public TranslatorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        sqLiteDatabase.execSQL(CREATE_ACCOUNT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        sqLiteDatabase.execSQL(DELETE_ACCOUNT_TABLE);
        onCreate(sqLiteDatabase);
    }

    public static class WordbookEntry implements BaseColumns {
        public static final String TABLE_NAME = "wordbook";
        public static final String COLUMN_NAME_WORD = "word";
    }

    public static class AccountEntry implements BaseColumns {
        public static final String TABLE_NAME = "account";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUNM_FACE = "face";
    }
}
