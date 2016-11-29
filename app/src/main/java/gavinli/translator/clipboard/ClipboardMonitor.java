package gavinli.translator.clipboard;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import gavinli.translator.MainActivity;
import gavinli.translator.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by GavinLi
 * on 16-11-27.
 */

public class ClipboardMonitor extends Service
        implements ClipboardManager.OnPrimaryClipChangedListener {
    public static final String INTENT_WORD = "word";
    private static final int FLOAT_WINDOW_TIME = 4000;

    private ClipboardManager mClipboardManager;
    private WindowManager mWindowManager;
    private View mFloatWindow;
    private String mPreviousText = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(this);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClipboardManager.removePrimaryClipChangedListener(this);
    }

    @Override
    public void onPrimaryClipChanged() {
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_clipboard), false)) {
            CharSequence charSequence = mClipboardManager.getPrimaryClip().getItemAt(0).getText();
            //TODO 内容有可能为空，待解决
            if (charSequence == null) return;
            String text = charSequence.toString();
            //必须是英语单词
            if (!text.matches("[a-zA-Z]+\\s*") ||
                    (text.equals(mPreviousText) && mFloatWindow != null)) return;
            showFloatWindow(text.trim());
            TimerTask hideFloatWindowTask = new TimerTask() {
                @Override
                public void run() {
                    hideFloatWindow();
                }
            };
            new Timer().schedule(hideFloatWindowTask, FLOAT_WINDOW_TIME);
            mPreviousText = text;
        }
    }

    @SuppressLint("InflateParams")
    private void showFloatWindow(String word) {
        if(mFloatWindow != null) {
            hideFloatWindow();
        }
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        mFloatWindow = new FloatView(this);
        mFloatWindow.setOnClickListener(view -> {
            hideFloatWindow();
            //TODO 悬浮框显示解释
            Intent intent = new Intent(ClipboardMonitor.this, MainActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(INTENT_WORD, word);
            startActivity(intent);
        });
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.TOP;
        params.width = 120;
        params.height = 120;
        params.x = screenWidth + 100;
        params.y = screenHeight / 5;
        mWindowManager.addView(mFloatWindow, params);
    }

    private void hideFloatWindow() {
        if(mFloatWindow != null) {
            mWindowManager.removeView(mFloatWindow);
            mFloatWindow = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
