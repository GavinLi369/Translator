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
import gavinli.translator.datebase.AccountData;
import gavinli.translator.datebase.AccountDatebase;
import gavinli.translator.util.network.AccountServer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by gavin on 9/17/17.
 */

public class AccountActivity extends Activity {
    private static final int GALLERY_REQUSET_CODE = 138;
    private static final int CROP_REQUEST_CODE = 139;
    private static final int LOG_IN_REQUSET_CODE = 140;

    private ImageView mFaceImage;
    private TextView mAccountText;
    private TextView mNameText;
    private Button mLogOutButton;

    private AccountDatebase mAccountDatebase;
    private AccountData mAccountData;

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

        mAccountDatebase = new AccountDatebase(this);
        mAccountData = mAccountDatebase.getAccountData();
        if(mAccountData != null) {
            mAccountText.setText(mAccountData.id);
            mNameText.setText(mAccountData.name);
            mFaceImage.setImageBitmap(mAccountData.face);
        } else {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivityForResult(intent, LOG_IN_REQUSET_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOG_IN_REQUSET_CODE:
                if(resultCode == RESULT_OK) {
                    AccountDatebase accountDatebase = new AccountDatebase(this);
                    AccountData accountData = accountDatebase.getAccountData();
                    if(accountData == null) return;
                    mAccountText.setText(accountData.id);
                    mNameText.setText(accountData.name);
                    mFaceImage.setImageBitmap(accountData.face);
                } else {
                    finish();
                }
                break;
            case GALLERY_REQUSET_CODE:
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
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    // Android 7.0 "file://" uri权限适配
                    fileUri = FileProvider.getUriForFile(this,
                            "gavinli.translator", faceFile);
                } else {
                    fileUri = Uri.fromFile(faceFile);
                }
                routeToCrop(fileUri);
                break;
            case CROP_REQUEST_CODE:
                // 此处crop正常返回resultCode也不为RESULT_OK
                if(data == null) return;
                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    Bitmap face = bundle.getParcelable("data");
                    mFaceImage.setImageBitmap(face);
                    if(mAccountData == null) {
                        mAccountData = mAccountDatebase.getAccountData();
                    }
                    String id = mAccountData.id;
                    String name = mAccountData.name;
                    String password = mAccountData.password;
                    mAccountData = new AccountData(id, name, password, face);
                    updateInfo(mAccountData);
                    mAccountDatebase.updateAccountData(mAccountData);
                }
                break;
            default:
                break;
        }
    }

    private void routeToGallery() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUSET_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
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

    private void updateInfo(AccountData data) {
        Observable<Boolean> observable = AccountServer.performUpdateInfo(mAccountData);
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(success -> {

        });
    }

    private void logOut() {
        AccountDatebase accountDatebase = new AccountDatebase(this);
        accountDatebase.deleteAccountData();
        finish();
    }

    private File saveBitmap(Bitmap bitmap) throws IOException {
        File file = new File(getCacheDir(), "face-cache");
        if (!file.exists()) file.createNewFile();
        try (OutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        return file;
    }
}
