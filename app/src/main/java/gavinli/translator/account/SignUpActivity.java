package gavinli.translator.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gavinli.translator.R;
import gavinli.translator.data.Account;
import gavinli.translator.data.source.datebase.AccountDb;
import gavinli.translator.data.source.remote.AccountServer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by gavin on 9/17/17.
 */

public class SignUpActivity extends Activity {
    private EditText mAccountEdit;
    private EditText mNameEdit;
    private EditText mPasswordEdit;
    private Button mSigninButton;
    private TextView mSwitchButton;

    private Bitmap mFace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFace = BitmapFactory.decodeResource(getResources(), R.drawable.img_default_face);

        mAccountEdit = findViewById(R.id.edit_account);
        mNameEdit = findViewById(R.id.edit_name);
        mPasswordEdit = findViewById(R.id.edit_password);
        mSigninButton = findViewById(R.id.btn_signin);
        mSigninButton.setOnClickListener(view -> signUp());
//        mFaceButton = findViewById(R.id.btn_face);
//        mFaceButton.setOnClickListener(view -> routeToGallery());
        mSwitchButton = findViewById(R.id.tv_switch);
        mSwitchButton.setOnClickListener(view -> finish());
    }

    private void signUp() {
        String account = mAccountEdit.getText().toString();
        String name = mNameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        if(account.length() < 3 || account.length() > 20) {
            mAccountEdit.setError(getString(R.string.account_illegal));
            mAccountEdit.requestFocus();
            return;
        } else if(name.length() < 3 || name.length() > 20) {
            mNameEdit.setError(getString(R.string.name_illegal));
            mNameEdit.requestFocus();
            return;
        } else if(password.length() < 3 || password.length() > 20) {
            mPasswordEdit.setError(getString(R.string.password_illegal));
            mPasswordEdit.requestFocus();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getString(R.string.progress_message));
        dialog.show();

        Account accountData = new Account(account, name, password, mFace);
        Observable<Boolean> observable = AccountServer.performSignUp(accountData);
        observable.map(success -> {
            if(!success) return false;
            AccountDb datebase = new AccountDb(SignUpActivity.this);
            datebase.insertAccountData(accountData);
            return true;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(success -> {
            if(success) {
                dialog.cancel();
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                dialog.cancel();
                mAccountEdit.setError(getString(R.string.account_has_used));
                mAccountEdit.requestFocus();
            }
        }, throwable -> {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        });
    }
}
