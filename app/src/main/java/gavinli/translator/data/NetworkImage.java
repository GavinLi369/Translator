package gavinli.translator.data;

/**
 * Created by gavin on 10/16/17.
 */

public class NetworkImage {
    /**
     * 网络图片宽度
     */
    private int mWidth;

    /**
     * 网络图片高度
     */
    private int mHeight;

    /**
     * 网络图片url
     */
    private String mUrl;

    public NetworkImage(int width, int height, String url) {
        mWidth = width;
        mHeight = height;
        mUrl = url;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getUrl() {
        return mUrl;
    }
}
