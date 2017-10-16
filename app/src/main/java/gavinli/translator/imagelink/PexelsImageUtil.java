package gavinli.translator.imagelink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavinli.translator.imagelink.NetworkImage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 7/8/17.
 */

public class PexelsImageUtil {
    private static final String SEARCH_URL = "https://www.pexels.com/search/";
    private static final Pattern PATTERN = Pattern.compile("width=\"(.+?)\" height=\"(.+?)\".+?src=\"(http.+?)\"");

    private String mKey;
    private int mPage;

    public PexelsImageUtil(String key) {
        mKey = key;
        mPage = 1;
    }

    /**
     * 根据给定关键字获取网页源文件，每调用一次page加1。
     *
     * @return 15个图片链接
     */
    public List<NetworkImage> getImageLinks() throws IOException {
        Request request = new Request.Builder()
                .url(SEARCH_URL + mKey + "?page=" + mPage++)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        return findImageLinksFromHtml(response.body().string());
    }

    /**
     * 使用正则匹配网页中的图片资源
     *
     * @param html 源网页
     *
     * @return 该网页中包含的所有图片资源
     */
    private List<NetworkImage> findImageLinksFromHtml(String html) {
        List<NetworkImage> links = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(html);
        while(matcher.find()) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            String url = matcher.group(3);
            NetworkImage networkImage = new NetworkImage(width, height, url);
            links.add(networkImage);
        }
        return links;
    }
}
