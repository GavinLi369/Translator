package gavinli.translator.clipboard;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import gavinli.translator.R;
import gavinli.translator.data.Explain;

/**
 * Created by GavinLi
 * on 17-1-1.
 */

public class FloatingExplainView extends CoordinatorLayout {
    private TextView mTextView;
    private ProgressBar mProgressBar;

    private FloatingExplainListener mListener;

    /**
     * 当前正在显示的翻译
     */
    private Explain mExplain;

    public FloatingExplainView(Context context) {
        super(context);
        init();
    }

    public FloatingExplainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

        //保证宽度不随屏幕旋转变换
        int screenWidth = Math.min(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);

        RelativeLayout topLayout = new RelativeLayout(getContext());
        int topHeight = (int) (screenWidth / 7.7);
        LayoutParams topLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, topHeight);
        topLayout.setLayoutParams(topLayoutParams);
        topLayout.setBackgroundResource(R.color.colorPrimary);
        addView(topLayout);

        TextView title = new TextView(getContext());
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        int titleTop = screenWidth / 45;
        int titleLeft = screenWidth / 27;
        titleParams.setMargins(titleLeft, titleTop, 0, 0);
        title.setLayoutParams(titleParams);
        title.setTextColor(Color.WHITE);
        title.setText(R.string.app_name);
        title.setTextSize(20);
        topLayout.addView(title);

        ImageButton star = new ImageButton(getContext());
        RelativeLayout.LayoutParams starParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        starParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        int starRight = (int) (screenWidth / 6.75);
        starParams.setMargins(0, 0, starRight, 0);
        star.setLayoutParams(starParams);
        star.setBackgroundResource(R.color.colorStarBg);
        star.setImageResource(R.drawable.ic_star);
        star.setOnClickListener(view -> mListener.onStar(mExplain));
        topLayout.addView(star);


        ImageButton close = new ImageButton(getContext());
        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        int closeTop = screenWidth / 540;
        int closeRight = screenWidth / 27;
        closeParams.setMargins(0, closeTop, closeRight, 0);
        close.setLayoutParams(closeParams);
        close.setBackgroundResource(R.color.colorCloseBg);
        close.setImageResource(R.drawable.ic_close);
        close.setOnClickListener(view -> mListener.onClose());
        topLayout.addView(close);

        showLoading();

        mTextView = new TextView(getContext());
        LayoutParams textViewParams = new LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mTextView.setLayoutParams(textViewParams);
        int textLeft = (int) (screenWidth / 16.875);
        mTextView.setPadding(textLeft, 0, textLeft, 0);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ScrollView scrollView = new ScrollView(getContext());
        LayoutParams scrollParams = new LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        scrollParams.setMargins(0, topHeight, 0, 0);
        scrollView.setLayoutParams(scrollParams);
        scrollView.addView(mTextView);
        addView(scrollView);
    }

    public void setExplain(Explain explain) {
        mExplain = explain;
        removeView(mProgressBar);
        for(CharSequence spanned : mExplain.getSource()) {
            mTextView.append(spanned);
            mTextView.append("\n\n");
        }
    }

    public void showLoading() {
        if(mTextView != null) mTextView.setText("");
        mProgressBar = new ProgressBar(getContext());
        LayoutParams progressBarParams = new LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParams.gravity = Gravity.CENTER;
        mProgressBar.setLayoutParams(progressBarParams);
        addView(mProgressBar);
    }

    public void setFloatingExplainListener(FloatingExplainListener listener) {
        mListener = listener;
    }
}
