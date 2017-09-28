package gavinli.translator.util.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gavinli.translator.App;
import gavinli.translator.datebase.AccountData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by gavin on 9/19/17.
 */

public class AccountServer {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Observable<Boolean> performSignUp(AccountData accountData) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            Map<String, String> infoes = new HashMap<>();
            infoes.put("id", accountData.id);
            infoes.put("name", accountData.name);
            infoes.put("password", accountData.password);
            ByteArrayOutputStream faceOut = new ByteArrayOutputStream();
            accountData.face.compress(Bitmap.CompressFormat.JPEG, 100, faceOut);
            String faceBase64 = Base64.encodeToString(faceOut.toByteArray(), Base64.DEFAULT);
            infoes.put("face", faceBase64);
            JSONObject jsonObject = new JSONObject(infoes);
            Request request = new Request.Builder()
                    .url(App.HOST + "/account")
                    .post(RequestBody.create(JSON, jsonObject.toString()))
                    .build();
            try {
                Response response = new OkHttpClient().newCall(request).execute();
                if (response.code() == StatusCode.CREATED) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<AccountData> performLogIn(AccountData accountData) {
        return Observable.create((Observable.OnSubscribe<AccountData>) subscriber -> {
            Request request = new Request.Builder()
                    .url(App.HOST + "/account/" + accountData.id)
                    .build();
            String json;
            try {
                Response response = new OkHttpClient().newCall(request).execute();
                if (response.code() == StatusCode.NOT_FOUND) {
                    subscriber.onNext(null);
                    return;
                }
                json = response.body().string();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }
            try {
                JSONObject result = new JSONObject(json);
                String account = result.getString("id");
                String name = result.getString("name");
                String password = result.getString("password");
                String faceBase64 = result.getString("face");
                byte[] faceData = Base64.decode(faceBase64, Base64.DEFAULT);
                Bitmap face = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);
                AccountData data = new AccountData(account, name, password, face);
                subscriber.onNext(data);
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Boolean> performUpdateInfo(AccountData accountData) {
        return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            Map<String, String> infoes = new HashMap<>();
            infoes.put("id", accountData.id);
            infoes.put("name", accountData.name);
            infoes.put("password", accountData.password);
            ByteArrayOutputStream faceOut = new ByteArrayOutputStream();
            accountData.face.compress(Bitmap.CompressFormat.JPEG, 100, faceOut);
            String faceBase64 = Base64.encodeToString(faceOut.toByteArray(), Base64.DEFAULT);
            infoes.put("face", faceBase64);
            JSONObject jsonObject = new JSONObject(infoes);
            Request request = new Request.Builder()
                    .url(App.HOST + "/account/" + accountData.id)
                    .post(RequestBody.create(JSON, jsonObject.toString()))
                    .build();
            try {
                Response response = new OkHttpClient().newCall(request).execute();
                if (response.code() == StatusCode.CREATED) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io());
    }
}
