package gavinli.translator.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gavinli.translator.R;
import gavinli.translator.datebase.AccountData;
import gavinli.translator.datebase.AccountDatebase;
import gavinli.translator.util.network.AccountServer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by gavin on 9/17/17.
 */

public class LogInActivity extends Activity {
    private static final int REQUEST_SIGN_UP_CODE = 138;

    private EditText mAccountEdit;
    private EditText mPasswordEdit;
    private Button mLoginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView switchText = findViewById(R.id.tv_switch);
        switchText.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGN_UP_CODE);
        });

        mAccountEdit = findViewById(R.id.edit_account);
        mPasswordEdit = findViewById(R.id.edit_password);
        mLoginButton = findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(view -> login());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SIGN_UP_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void login() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getString(R.string.progress_message));
        dialog.show();
        String account = mAccountEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        AccountData accountData = new AccountData(account, null, password, null);
        Observable<AccountData> observable = AccountServer.performLogIn(accountData);
        observable.map(data -> {
            if(data != null) {
                AccountDatebase datebase = new AccountDatebase(LogInActivity.this);
                datebase.insertAccountData(data);
            }
            return data;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(data -> {
            dialog.cancel();
            if(data == null) {
                mAccountEdit.setError(getString(R.string.wrong_account));
                mAccountEdit.requestFocus();
            } else if (!password.equals(data.password)) {
                mPasswordEdit.setError(getString(R.string.wrong_password));
                mPasswordEdit.requestFocus();
            } else {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }, throwable -> {
            dialog.cancel();
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        });
    }
}
