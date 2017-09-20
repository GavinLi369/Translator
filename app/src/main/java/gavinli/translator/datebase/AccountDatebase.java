package gavinli.translator.datebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import gavinli.translator.datebase.TranslatorDbHelper.AccountEntry;

/**
 * Created by gavin on 9/17/17.
 */

public class AccountDatebase {
    private TranslatorDbHelper mDbHelper;

    public AccountDatebase(Context context) {
        mDbHelper = new TranslatorDbHelper(context);
    }

    public @Nullable AccountData getAccountData() {
        AccountData accountData = null;
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] columns = {
                AccountEntry._ID,
                AccountEntry.COLUMN_NAME,
                AccountEntry.COLUMN_PASSWORD,
                AccountEntry.COLUNM_FACE,
        };
        Cursor cursor = database.query(AccountEntry.TABLE_NAME, columns,
                "", null, null, null, null);
        if(cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUMN_PASSWORD));
            String face = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry.COLUNM_FACE));
            byte[] faceData = Base64.decode(face, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);
            accountData = new AccountData(id, name, password, bitmap);
        }
        cursor.close();
        return accountData;
    }

    public void insertAccountData(AccountData accountData) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AccountEntry._ID, accountData.id);
        values.put(AccountEntry.COLUMN_NAME, accountData.name);
        values.put(AccountEntry.COLUMN_PASSWORD, accountData.password);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        accountData.face.compress(Bitmap.CompressFormat.JPEG, 100, out);
        String faceBase64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        values.put(AccountEntry.COLUNM_FACE, faceBase64);
        database.insert(AccountEntry.TABLE_NAME, null, values);
    }

    public void deleteAccountData() {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] columns = {AccountEntry._ID};
        Cursor cursor = database.query(AccountEntry.TABLE_NAME, columns,
                "", null, null, null, null);
        if(cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(AccountEntry._ID));
            database.delete(AccountEntry.TABLE_NAME,
                    AccountEntry._ID + " = ?", new String[] {id});
        }
        cursor.close();
    }

    public void updateAccountData(AccountData accountData) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(AccountEntry.COLUMN_NAME, accountData.name);
        values.put(AccountEntry.COLUMN_PASSWORD, accountData.password);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        accountData.face.compress(Bitmap.CompressFormat.JPEG, 100, out);
        String faceBase64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        values.put(AccountEntry.COLUNM_FACE, faceBase64);
        database.update(AccountEntry.TABLE_NAME, values,
                AccountEntry._ID + " = ?", new String[]{accountData.id});
    }
}
