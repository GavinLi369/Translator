package gavinli.translator.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by gavin on 9/19/17.
 */

public class RoundImageView extends AppCompatImageView {
    private static final int CIRCLE_STOKE_WIDTH = 2;
    private Path circlePath = new Path();
    private Paint mPaint = new Paint();

    {
        // Canvas.clipPath() isn't supported with hardware acceleration
        // in Android 4.2
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        circlePath.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CW);
        canvas.clipPath(circlePath);
        Drawable image =  getDrawable();
        if(image instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            float scale = (float) getWidth() / bitmap.getWidth();
            canvas.scale(scale, scale);
            canvas.drawBitmap(bitmap, 0, 0, mPaint);
        }
        canvas.restore();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        mPaint.setStrokeWidth(CIRCLE_STOKE_WIDTH * scale);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                getWidth() / 2 - (CIRCLE_STOKE_WIDTH - 1) * scale, mPaint);
    }
}
