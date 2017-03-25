package gavinli.translator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-23.
 */

public class BingImageUtil extends NetworkImageUtil {
    private static final String BING_IMAGE_URL = "http://cn.bing.com/images/search?FORM=RESTAB&q=";

    private String mKey;

    public BingImageUtil(String key) {
        mKey = key;
    }

    @Override
    protected void buildImageUrl() throws IOException {
        Request request = new Request.Builder()
                .url(BING_IMAGE_URL + mKey)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if(!response.isSuccessful())
            throw new IOException("网络连接失败");
//        Pattern pattern = Pattern.compile("href=\"(http[s]*://[^\":]+?\\.jpg[^\"]*?)\"");
        Pattern pattern = Pattern.compile("src=\"(http.+?)\"");
        Matcher matcher = pattern.matcher(response.body().string());
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1));
        }
    }

    @Override
    public List<Bitmap> getImages(int num, int offset) throws IOException {
        if(mImageUrls.isEmpty()) buildImageUrl();
        List<Bitmap> images = new ArrayList<>();
        for(int i = offset; i < num + offset; i++) {
            InputStream in = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(mImageUrls.get(i));
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();
//                if (connection.getResponseCode() != 200)
//                    throw new IOException("网络连接失败(" + connection.getResponseCode() + ")");
                in = connection.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                images.add(BitmapFactory.decodeStream(in, null, options));
            } catch (SSLHandshakeException e) {
                e.printStackTrace();
            }finally {
                if(in != null) in.close();
                if(connection != null) connection.disconnect();
            }
        }
        return images;
    }
}
