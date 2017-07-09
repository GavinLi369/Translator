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
 * on 7/8/17.
 */

public class PexelsImageUtil {
    private static final String SEARCH_URL = "https://www.pexels.com/search/";

    private String mKey;
    private int mPage;

    public PexelsImageUtil(String key) {
        mKey = key;
        mPage = 1;
    }

    /**
     * @return 15个图片链接
     */
    public List<String> getImageLinks() throws IOException {
        Request request = new Request.Builder()
                .url(SEARCH_URL + mKey + "?page=" + mPage++)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        return findImageLinksFromHtml(response.body().string());
    }

    private List<String> findImageLinksFromHtml(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("src=\"(http.+?)\"");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()) {
            links.add(matcher.group(1));
        }
        return links;
    }
}
