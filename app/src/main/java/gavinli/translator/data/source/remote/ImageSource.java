package gavinli.translator.data.source.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.App;
import gavinli.translator.data.NetworkImage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 7/8/17.
 */

public class ImageSource {
    private String mKey;
    private int mPage;

    public ImageSource(String key) {
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
                .url(App.HOST + "/image/" +  mKey + "?page=" + mPage++)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        try {
            return convertToList(response.body().string());
        } catch (JSONException e) {
            throw new IOException("Json解析出错");
        }
    }

    private List<NetworkImage> convertToList(String json) throws JSONException {
        List<NetworkImage> networkImages = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String url = jsonObject.getString("url");
            int width = Integer.parseInt(jsonObject.getString("width"));
            int height = Integer.parseInt(jsonObject.getString("height"));
            NetworkImage networkImage = new NetworkImage(width, height, url);
            networkImages.add(networkImage);
        }
        return networkImages;
    }
}
