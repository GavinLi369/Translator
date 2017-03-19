package gavinli.translator.util;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class BaiduImageUtil extends NetworkImageUtil {
    private static final String BAIDU_IMAGE_SEARCH_URL = "http://image.baidu.com/search/index?tn=baiduimage&ps=1&ct=201326592&lm=-1&cl=2&nc=1&ie=utf-8&word=";

    private String mKey;

    public BaiduImageUtil(String key) {
        mKey = key;
    }

    @Override
    protected void buildImageUrl() throws IOException {
        Request request = new Request.Builder()
                .url(BAIDU_IMAGE_SEARCH_URL + mKey)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if(!response.isSuccessful())
            throw new IOException("网络连接错误(" + response.code() + ")");
        Pattern pattern = Pattern.compile("\"thumbURL\":\"(.+?)\"");
        Matcher matcher = pattern.matcher(response.body().string());
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1).replaceAll("\\\\", ""));
        }
    }
}
