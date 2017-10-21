package gavinli.translator.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import gavinli.translator.R;
import gavinli.translator.data.Account;
import gavinli.translator.data.source.datebase.AccountDb;
import gavinli.translator.data.source.remote.AccountServer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by gavin on 9/17/17.
 */

public class AccountActivity extends Activity {
    private static final int GALLERY_REQUSET_CODE = 138;
    private static final int GALLERY_REQUSET_CODE_KITKAT = 139;
    private static final int CROP_REQUEST_CODE = 140;
    private static final int LOG_IN_REQUSET_CODE = 141;

    private ImageView mFaceImage;
    private TextView mAccountText;
    private TextView mNameText;
    private Button mLogOutButton;

    private AccountDb mAccountDb;
    private Account mAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAccountText = findViewById(R.id.tv_id);
        mNameText = findViewById(R.id.tv_name);
        mFaceImage = findViewById(R.id.img_face);
        mLogOutButton = findViewById(R.id.btn_log_out);
        mFaceImage.setOnClickListener(view -> routeToGallery());
        mLogOutButton.setOnClickListener(view -> logOut());

        mAccountDb = new AccountDb(this);
        mAccount = mAccountDb.getAccountData();
        if(mAccount != null) {
            mAccountText.setText(mAccount.id);
            mNameText.setText(mAccount.name);
            mFaceImage.setImageBitmap(mAccount.face);
        } else {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivityForResult(intent, LOG_IN_REQUSET_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOG_IN_REQUSET_CODE:
                if(resultCode == RESULT_OK) {
                    AccountDb accountDb = new AccountDb(this);
                    Account account = accountDb.getAccountData();
                    if(account == null) return;
                    mAccountText.setText(account.id);
                    mNameText.setText(account.name);
                    mFaceImage.setImageBitmap(account.face);
                } else {
                    finish();
                }
                break;
            case GALLERY_REQUSET_CODE:
                handleGalleryResult(resultCode, data);
                break;
            case GALLERY_REQUSET_CODE_KITKAT:
                handleGalleryKitKatResult(resultCode, data);
                break;
            case CROP_REQUEST_CODE:
                // 此处crop正常返回resultCode也不为RESULT_OK
                if(data == null) return;
                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    Bitmap face = bundle.getParcelable("data");
                    mFaceImage.setImageBitmap(face);
                    if(mAccount == null) {
                        mAccount = mAccountDb.getAccountData();
                    }
                    String id = mAccount.id;
                    String name = mAccount.name;
                    String password = mAccount.password;
                    mAccount = new Account(id, name, password, face);
                    updateInfo(mAccount);
                    mAccountDb.updateAccountData(mAccount);
                }
                break;
            default:
                break;
        }
    }

    private void handleGalleryResult(int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        String path = data.getData().getPath();
        Bitmap image = BitmapFactory.decodeFile(path);
        File faceFile;
        try {
            faceFile = saveBitmap(image);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Uri fileUri = Uri.fromFile(faceFile);
        routeToCrop(fileUri);
    }

    // Result uri is "content://" after Android 4.4
    private void handleGalleryKitKatResult(int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        Uri contentUri = data.getData();
        if (contentUri == null) return;
        File faceFile;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(contentUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            faceFile = saveBitmap(image);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Uri fileUri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0 "file://" uri权限适配
            fileUri = FileProvider.getUriForFile(this,
                    "gavinli.translator", faceFile);
        } else {
            fileUri = Uri.fromFile(faceFile);
        }
        routeToCrop(fileUri);
    }

    private void routeToGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startActivityForResult(intent, GALLERY_REQUSET_CODE_KITKAT);
        } else {
            startActivityForResult(intent, GALLERY_REQUSET_CODE);
        }
    }


    private void routeToCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    private void updateInfo(Account data) {
        Observable<Boolean> observable = AccountServer.performUpdateInfo(data);
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(success -> {});
    }

    private void logOut() {
        AccountDb accountDb = new AccountDb(this);
        accountDb.deleteAccountData();
        finish();
    }

    private File saveBitmap(Bitmap bitmap) throws IOException {
        File file = new File(getExternalCacheDir(), "face-cache");
        if (!file.exists()) file.createNewFile();
        try (OutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        return file;
    }
}
