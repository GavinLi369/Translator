package gavinli.translator.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-19.
 */

public class GoogleImageUtil {
    private static final String GOOGLE_IMAGE_SEARCH_URL = "https://www.google.com.hk/search?tbm=isch&q=";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Mobile Safari/537.36";

    private String mKey;

    private List<String> mImageUrls = new ArrayList<>();

    public GoogleImageUtil(String key) {
        mKey = key;
    }

    public List<String> getImageUrls() throws IOException {
        if(mImageUrls.size() == 0) {
            buildImageUrl();
        }
        return mImageUrls;
    }

    public void buildImageUrl() throws IOException {
        Request request = new Request.Builder()
                .url(GOOGLE_IMAGE_SEARCH_URL + mKey)
                .header("User-Agent", MOBILE_USER_AGENT)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if(!response.isSuccessful())
            throw new IOException("网络连接错误(" + response.code() + ")");
        String html = response.body().string();
        Pattern pattern = Pattern.compile("\"ou\":\"(.+?)\"");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1));
        }
    }
}
