package gavinli.translator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-19.
 */

public class GoogleImageUtil extends NetworkImageUtil {
    private static final String GOOGLE_IMAGE_SEARCH_URL = "https://www.google.com.hk/search?tbm=isch&q=";
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Mobile Safari/537.36";

    private String mKey;

    private List<Bitmap> mImages = new ArrayList<>();

    public GoogleImageUtil(String key) {
        mKey = key;
    }

    @Override
    protected void buildImageUrl() throws IOException {
        Request request = new Request.Builder()
                .url(GOOGLE_IMAGE_SEARCH_URL + mKey)
//                .header("User-Agent", DESKTOP_USER_AGENT)
                .build();
        Response response = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
                .newCall(request)
                .execute();
        if(!response.isSuccessful())
            throw new IOException("网络连接错误(" + response.code() + ")");
        String html = response.body().string();
//        Pattern pattern = Pattern.compile("\"ou\":\"(.+?)\"");
        Pattern pattern = Pattern.compile("src=\"(http.+?)\"");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1));
        }
    }

    public List<Bitmap> getImages() throws IOException {
        if(mImages.isEmpty()) {
            Request request = new Request.Builder()
                    .url(GOOGLE_IMAGE_SEARCH_URL + mKey)
//                    .header("User-Agent", DESKTOP_USER_AGENT)
                    .build();
            Response response = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                    .newCall(request)
                    .execute();
            if(!response.isSuccessful())
                throw new IOException("网络连接错误(" + response.code() + ")");
            Pattern pattern = Pattern.compile("\",\"data.+?base64,(.+?)\"");
            Matcher matcher = pattern.matcher(response.body().string());
            while(matcher.find()) {
                String base64 = matcher.group(1);
                byte[] out = Base64.decode(base64.getBytes(), Base64.DEFAULT);
                mImages.add(BitmapFactory.decodeByteArray(out, 0, out.length));
            }
        }
        return mImages;
    }
}
