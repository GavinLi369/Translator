package gavinli.translator.util;

import android.content.Context;

/**
 * Created by gavin on 9/20/17.
 */

public class UiTool {
    public static int dpToPx(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }
}
