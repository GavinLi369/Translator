package gavinli.translator.util.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import gavinli.translator.App;
import gavinli.translator.datebase.AccountData;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by gavin on 9/19/17.
 */

public class AccountServer {
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
            Socket socket;
            try {
                socket = new Socket(App.SERVER_HOST, App.SIGN_UP_PORT);
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }
            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(jsonObject.toString() + '\n');
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String resultStr = reader.readLine();
                JSONObject result = new JSONObject(resultStr);
                subscriber.onNext(result.getBoolean("result"));
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<AccountData> performLogIn(AccountData accountData) {
        return Observable.create((Observable.OnSubscribe<AccountData>) subscriber -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", accountData.id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Socket socket;
            try {
                socket = new Socket(App.SERVER_HOST, App.LOGIN_PORT);
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }
            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(jsonObject.toString() + '\n');
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                JSONObject result = new JSONObject(reader.readLine());
                boolean success = result.getBoolean("result");
                if(!success) {
                    subscriber.onNext(null);
                    return;
                }
                JSONObject resultData = result.getJSONObject("data");
                String account = resultData.getString("id");
                String name = resultData.getString("name");
                String password = resultData.getString("password");
                String faceBase64 = resultData.getString("face");
                byte[] faceData = Base64.decode(faceBase64, Base64.DEFAULT);
                Bitmap face = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);
                AccountData data = new AccountData(account, name, password, face);
                subscriber.onNext(data);
            } catch (IOException | JSONException e) {
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
            Socket socket;
            try {
                socket = new Socket(App.SERVER_HOST, App.UPDATE_INFO_PORT);
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }
            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(jsonObject.toString() + '\n');
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String resultStr = reader.readLine();
                JSONObject result = new JSONObject(resultStr);
                subscriber.onNext(result.getBoolean("result"));
            } catch (IOException | JSONException e) {
                subscriber.onError(e);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io());
    }
}
