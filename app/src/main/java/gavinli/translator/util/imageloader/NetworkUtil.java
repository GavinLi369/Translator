package gavinli.translator.util.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gavin on 17-8-16.
 */

public class NetworkUtil {
    private NetworkUtil() {}

    public static Bitmap getBitmap(String url, int reqWidth, int reqHeight) throws IOException {
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        try(InputStream in = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] temp = new byte[1024];
            int length;
            while((length = in.read(temp)) > -1) {
                out.write(temp, 0, length);
            }
            out.flush();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            byte[] bitmapData = out.toByteArray();
            BitmapFactory.decodeByteArray(bitmapData,
                    0, bitmapData.length, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = cancaulateSampleSize(options.outWidth, options.outHeight,
                    reqWidth, reqHeight);
            return BitmapFactory.decodeByteArray(bitmapData,
                    0, bitmapData.length, options);
        } finally {
            connection.disconnect();
        }
    }

    private static int cancaulateSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int sampleSize = 1;
        while(width > reqWidth || height > reqHeight) {
            sampleSize *= 2;
            width /= 2;
            height /= 2;
        }
        return sampleSize;
    }
}
