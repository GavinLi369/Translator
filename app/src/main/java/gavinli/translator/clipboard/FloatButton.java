package gavinli.translator.clipboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-29.
 */

public class FloatButton extends RelativeLayout {
    public FloatButton(Context context) {
        super(context);
        init();
    }

    public FloatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        ImageView mBackground = new ImageView(getContext());
        mBackground.setImageResource(R.drawable.ic_floatview_bg);
        mBackground.setLayoutParams(params);
        addView(mBackground);
        ImageView floatview = new ImageView(getContext());
        floatview.setImageResource(R.drawable.ic_floatview);
        floatview.setLayoutParams(params);
        addView(floatview);

        ObjectAnimator floatviewScaleXAnimator = ObjectAnimator.ofFloat(floatview, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator floatviewScaleYAnimator = ObjectAnimator.ofFloat(floatview, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator backgroundScaleXAnimator = ObjectAnimator.ofFloat(mBackground, "scaleX", 1f, 1.7f);
        ObjectAnimator backgroundScaleYAnimator = ObjectAnimator.ofFloat(mBackground, "scaleY", 1f, 1.7f);
        ObjectAnimator backgroundAlphaAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(floatviewScaleXAnimator).with(floatviewScaleYAnimator)
                .with(backgroundScaleXAnimator).with(backgroundScaleYAnimator)
                .with(backgroundAlphaAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.setStartDelay(400);
                animation.start();
            }
        });
        animatorSet.setDuration(900);
        animatorSet.start();
    }
}
