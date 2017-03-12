package gavinli.translator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class BaiduImageUtil {
    private static final String BAIDU_IMAGE_SEARCH_URL = "http://image.baidu.com/search/index?tn=baiduimage&ps=1&ct=201326592&lm=-1&cl=2&nc=1&ie=utf-8&word=";
    private List<String> mImageUrls = new ArrayList<>();

    private String mKey;

    public BaiduImageUtil(String key) {
        mKey = key;
    }

    public List<Bitmap> getImageByKey(int num, int offset) throws IOException {
        if(mImageUrls.size() == 0) {
            Request request = new Request.Builder()
                    .url(BAIDU_IMAGE_SEARCH_URL + mKey)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            buildImageUrl(response.body().string());
        }

        List<Bitmap> images = new ArrayList<>();
        for (int i = offset; i < offset + num; i++) {
//            Request request = new Request.Builder()
//                    .url(mImageUrls.get(i))
//                    .build();
//            Response response = new OkHttpClient().newCall(request).execute();
//            Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
            URL url = new URL(mImageUrls.get(i));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
            images.add(bitmap);
        }
        return images;
    }

    private void buildImageUrl(String html) {
        Pattern pattern = Pattern.compile("\"thumbURL\":\"(.+?)\"");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1));
        }
    }
}
