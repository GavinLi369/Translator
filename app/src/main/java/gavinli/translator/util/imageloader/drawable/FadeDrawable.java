package gavinli.translator.util.imageloader.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * 使ImageView以渐出的效果显示图片
 *
 * Created by gavin on 10/15/17.
 */

public class FadeDrawable extends BitmapDrawable {
    /**
     * 动画起始时间
     */
    private long mStartTime = 0;

    /**
     * 动画时长，单位ms
     */
    private static final float DURATION = 200f;

    /**
     * alpha最大值
     */
    private static final int ALPHA_VALUE = 0xFF;

    /**
     * 正在显示渐出动画
     */
    private boolean isAnimating = false;

    public static void setBitmap(Context context, ImageView imageView, Bitmap bitmap) {
        FadeDrawable fadeDrawable = new FadeDrawable(context, bitmap);
        imageView.setImageDrawable(fadeDrawable);
    }

    public FadeDrawable(Context context, Bitmap bitmap) {
        super(context.getResources(), bitmap);
        isAnimating = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isAnimating) {
            super.draw(canvas);
        } else {
            if (mStartTime == 0) {
                // 初始化startTime
                mStartTime = System.nanoTime();
            }
            float offset = (System.nanoTime() - mStartTime) / (DURATION * 1000000);
            if (offset >= 1) {
                isAnimating = false;
                super.draw(canvas);
            } else {
                super.setAlpha((int) (ALPHA_VALUE * offset));
                super.draw(canvas);
                // 借助setAlpha刷新自己
                super.setAlpha(ALPHA_VALUE);
            }
        }
    }
}
