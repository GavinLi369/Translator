package gavinli.translator.clipboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-29.
 */

public class FloatingIndicator extends View {
    private Bitmap mLogo;
    private Paint mPaint;

    /**
     * 动画开始时间
     */
    private long mStartTime;

    /**
     * 指示器显现动画时长(ms)
     */
    private static final long APPEAR_TIME = 250;

    /**
     * 背景扩散动画时长(ms)
     */
    private static final long BG_BIGGER_TIME = 800;

    /**
     * Logo放大动画时长(ms)
     */
    private static final long BIGGER_TIME = 200;

    /**
     * Logo缩小动画时长(ms)
     */
    private static final long SMALLER_TIME = 100;

    /**
     * Logo等待动画时长(ms)
     */
    private static final long DELAY_TIME = 500;

    /**
     * Logo的中心坐标，由于指示器右边有一部分会超出屏幕，
     * 所以，中心坐标位于指示器实际的中心偏右。
     */
    private int mCenterX;
    private int mCenterY;

    public FloatingIndicator(Context context) {
        super(context);
        init();
    }

    public FloatingIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mLogo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mCenterX = mLogo.getWidth();
        mCenterY = mLogo.getHeight();
    }

    /**
     * 指示器的动画分为五个阶段
     *
     *      第一阶段：指示器从0放大到原始大小 {@link #APPEAR_TIME}
     *
     *      第二阶段：指示器从原始大小放大到1.2倍大小 {@link #BIGGER_TIME}
     *
     *      第三阶段：指示器从1.2倍大小缩小到原始大小 {@link #SMALLER_TIME}
     *
     *      第四阶段：指示器大小不变，背景从原始大小放大到2倍大小，
     *          同时aplha值从0xff / 2逐渐变为0x00 {@link #BG_BIGGER_TIME}
     *
     *      第五阶段：指示器及背景均无变化，等待下一轮动画 {@link #DELAY_TIME}
     *
     * 在五个阶段结束后，动画会从第二个阶段再次开始。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (mStartTime == 0) {
            mStartTime = System.nanoTime();
        }
        final long currentTime = System.nanoTime();
        if ((currentTime - mStartTime) / (1000 * 1000) < APPEAR_TIME) {
            //指示器显现动画
            canvas.save();
            float scale = (float) (currentTime - mStartTime) / (1000 * 1000) / APPEAR_TIME;
            int left = mCenterX - mLogo.getWidth() / 2;
            int top = mCenterY - mLogo.getHeight() / 2;
            canvas.scale(scale, scale, mCenterX, mCenterY);
            canvas.drawBitmap(mLogo, left, top, mPaint);
            canvas.restore();
            invalidate();
            return;
        }

        final long offset = (currentTime - mStartTime - APPEAR_TIME) / (1000 * 1000)
                % (BIGGER_TIME + SMALLER_TIME + BG_BIGGER_TIME + DELAY_TIME);

        if (offset > BIGGER_TIME + SMALLER_TIME && offset < BIGGER_TIME + SMALLER_TIME + BG_BIGGER_TIME) {
            // 绘制背景
            canvas.save();
            float scale = 1 + (float) (offset - BIGGER_TIME - SMALLER_TIME) / BG_BIGGER_TIME;
            canvas.scale(scale, scale, mCenterX, mCenterY);
            int alpha = (int) ((1 - (float) (offset - BIGGER_TIME - SMALLER_TIME) / BG_BIGGER_TIME)
                    * 0xff / 2);
            mPaint.setAlpha(alpha);
            canvas.drawCircle(mCenterX, mCenterY,
                    mLogo.getWidth() / 2, mPaint);
            // 恢复Aplha值
            mPaint.setAlpha(0xff);
            canvas.restore();
        }

        // 绘制Logo
        canvas.save();
        if (offset < BIGGER_TIME) {
            // 放大阶段
            float scale = 1 + (float) offset / BIGGER_TIME / 5;
            canvas.scale(scale, scale, mCenterX, mCenterY);
        } else if (offset < BIGGER_TIME + SMALLER_TIME) {
            // 缩小阶段
            float scale = 1.2f - (float) (offset - BIGGER_TIME) / SMALLER_TIME / 5;
            canvas.scale(scale, scale, mCenterX, mCenterY);
        }
        int left = mCenterX - mLogo.getWidth() / 2;
        int top = mCenterY - mLogo.getHeight() / 2;
        canvas.drawBitmap(mLogo, left, top, mPaint);
        canvas.restore();
        invalidate();
    }

    /**
     * 指示器的宽设置为Logo的1.3倍，高为Logo的2倍。
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (mLogo.getWidth() * 1.3), mLogo.getHeight() * 2);
    }
}
