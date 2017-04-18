package gavinli.translator.clipboard;

import android.content.Context;
import android.graphics.Color;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 17-1-1.
 */

public class FloatWindow extends RelativeLayout {
    private TextView mTextView;
    private ProgressBar mProgressBar;

    private FloatWindowListener mListener;

    public FloatWindow(Context context) {
        super(context);
        init();
    }

    public FloatWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

        LinearLayout topLayout = new LinearLayout(getContext());
        LayoutParams topLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 70);
        topLayout.setLayoutParams(topLayoutParams);
        topLayout.setBackgroundResource(R.color.colorPrimary);
        addView(topLayout);

        TextView title = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(20, 12, 0, 0);
        title.setLayoutParams(titleParams);
        title.setTextColor(Color.WHITE);
        title.setText(R.string.app_name);
        title.setTextSize(20);
        topLayout.addView(title);

        ImageButton chinese = new ImageButton(getContext());
        LinearLayout.LayoutParams chineseParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        chineseParams.setMargins(100, 3, 0, 0);
        chinese.setLayoutParams(chineseParams);
        chinese.setBackgroundResource(R.color.colorChineseBg);
        chinese.setImageResource(R.drawable.ic_chinese);
        chinese.setOnClickListener(view -> mListener.onChangeExplain());
        topLayout.addView(chinese);

        ImageButton star = new ImageButton(getContext());
        LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        starParams.setMargins(30, 0, 0, 0);
        star.setLayoutParams(starParams);
        star.setBackgroundResource(R.color.colorStarBg);
        star.setImageResource(R.drawable.ic_star);
        star.setOnClickListener(view -> mListener.onStar());
        topLayout.addView(star);


        ImageButton close = new ImageButton(getContext());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        closeParams.setMargins(26, 1, 0, 0);
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
        mTextView.setPadding(32, 0, 32, 0);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ScrollView scrollView = new ScrollView(getContext());
        LayoutParams scrollParams = new LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        scrollParams.setMargins(0, 70, 0, 0);
        scrollView.setLayoutParams(scrollParams);
        scrollView.addView(mTextView);
        addView(scrollView);
    }

    public void setExplain(List<Spanned> spanneds) {
        removeView(mProgressBar);
        for(Spanned spanned : spanneds) {
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
        progressBarParams.addRule(CENTER_IN_PARENT);
        mProgressBar.setLayoutParams(progressBarParams);
        addView(mProgressBar);
    }

    public void setFloatWindowListener(FloatWindowListener listener) {
        mListener = listener;
    }

    public interface FloatWindowListener {
        void onChangeExplain();

        void onStar();

        void onClose();
    }
}
