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
 * on 7/7/17.
 */

public class FreeImagesUtil {
    private static final String SEARCH_URL = "http://cn.freeimages.com/search/";

    private String mKey;

    public FreeImagesUtil(String key) {
        mKey = key;
    }

    public List<String> getImageLinks() throws IOException {
        Request request = new Request.Builder()
                .url(SEARCH_URL + mKey)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        return findImageLinksFromHtml(response.body().string());
    }

    private List<String> findImageLinksFromHtml(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img src=\"(.+?jpg)\"");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()) {
            links.add(matcher.group(1));
        }
        return links;
    }
}
