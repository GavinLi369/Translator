package gavinli.translator.clipboard;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gavinli.translator.MainActivity;
import gavinli.translator.R;
import gavinli.translator.datebase.WordbookDb;
import gavinli.translator.util.CambirdgeApi;
import gavinli.translator.util.ExplainLoader;
import gavinli.translator.util.ExplainNotFoundException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by GavinLi
 * on 16-11-27.
 */

public class ClipboardMonitor extends Service
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final int FLOAT_WINDOW_TIME = 4000;

    private ClipboardManager mClipboardManager;
    private WindowManager mWindowManager;
    private View mFloatButton;
    private FloatWindow mFloatWindow;
    private LinearLayout mContainLayout;

    private String mPreviousText = "";
    private int mScreenWidth;
    private int mScreenHeight;

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
        stopForeground(true);
        mClipboardManager.removePrimaryClipChangedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(mWindowManager != null && mContainLayout != null) {
            mWindowManager.removeView(mContainLayout);
            showFloatWindow(mPreviousText);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentIntent(PendingIntent.getActivity(
                        this, 0,
                        new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle("Tap to Translate")
                .setContentText("Tap to translate is running");
        if(SDK_INT >= 19) notificationBuilder.setSmallIcon(R.drawable.ic_launcher_alpha);
        else notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        startForeground(1001, notificationBuilder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrimaryClipChanged() {
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_clipboard), false)) {
            CharSequence charSequence = mClipboardManager.getPrimaryClip().getItemAt(0).getText();
            if (charSequence == null) return;
            String text = charSequence.toString();
            //必须是英语单词
            if (!text.matches("[a-zA-Z]+\\s*") ||
                    (text.equals(mPreviousText) && mFloatButton != null)) return;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if(Settings.canDrawOverlays(this)) {
                    showFloatButton(text.trim());
                    TimerTask hideFloatWindowTask = new TimerTask() {
                        @Override
                        public void run() {
                            hideFloatButton();
                        }
                    };
                    new Timer().schedule(hideFloatWindowTask, FLOAT_WINDOW_TIME);
                } else {
                    String message = "Translator: " +
                            "Please permit drawing over other apps in Settings";
                    Toast.makeText(this, message,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showFloatButton(text.trim());
                TimerTask hideFloatWindowTask = new TimerTask() {
                    @Override
                    public void run() {
                        hideFloatButton();
                    }
                };
                new Timer().schedule(hideFloatWindowTask, FLOAT_WINDOW_TIME);
            }
            mPreviousText = text;
        }
    }

    private void showFloatButton(String word) {
        if(mFloatButton != null) {
            hideFloatButton();
        }
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mFloatButton = new FloatButton(this);
        //点击悬浮球显示解释
        mFloatButton.setOnClickListener(view -> showFloatWindow(word));
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.TOP;
        params.width = (int) (mScreenWidth / 4.5);
        params.height = (int) (mScreenHeight / 4.5);
        params.x = mScreenWidth + 100;
        params.y = mScreenHeight / 5;
        mWindowManager.addView(mFloatButton, params);
    }

    private void showFloatWindow(String word) {
        hideFloatButton();

        //切换横竖屏时保证屏幕宽高的正确
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        mContainLayout = new LinearLayout(this);
        mContainLayout.setBackgroundResource(R.color.colorFloatWindowContain);
        //单击解释区域外部，则关闭悬浮框
        mContainLayout.setOnTouchListener((v, event) -> {
            Rect rect = new Rect();
            mFloatWindow.getGlobalVisibleRect(rect);
            if(!rect.contains((int) event.getX(), (int) event.getY())) {
                mWindowManager.removeView(mContainLayout);
                mContainLayout = null;
                return true;
            } else {
                return false;
            }
        });

        ContextThemeWrapper wrapper = new ContextThemeWrapper(ClipboardMonitor.this, R.style.Theme_AppCompat);
        mFloatWindow = new FloatWindow(wrapper);
        mFloatWindow.setFloatWindowListener(new FloatWindowListenerImpl(word.toLowerCase()));

        int height = mScreenHeight * 2 / 3;
        int top = mScreenHeight / 6;

        int width = mScreenWidth * 5 / 6;
        int left = mScreenWidth / 12;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(left, top, 0, 0);
        mFloatWindow.setLayoutParams(layoutParams);
        mContainLayout.addView(mFloatWindow);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.TOP;
        params.width = mScreenWidth;
        params.height = mScreenHeight;
        params.x = 0;
        params.y = 0;
        mWindowManager.addView(mContainLayout, params);
        showExplain(word, "");
    }

    private void showExplain(String word, String dictionary) {
        Observable
                .create((Observable.OnSubscribe<List<Spanned>>) subscriber -> {
                    try {
                        List<Spanned> explain;
                        if(dictionary.isEmpty()) {
                            explain = ExplainLoader.with(ClipboardMonitor.this)
                                    .search(word).load();
                        } else {
                            explain = ExplainLoader.with(ClipboardMonitor.this)
                                    .search(word).dictionary(dictionary).load();
                        }
                        subscriber.onNext(explain);
                    } catch (IOException | ExplainNotFoundException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(explain -> mFloatWindow.setExplain(explain)
                , throwable -> {
                    if(throwable instanceof IOException) {
                        Snackbar.make(mFloatWindow, "网络连接失败",
                                Snackbar.LENGTH_SHORT).show();
                    } else if(throwable instanceof ExplainNotFoundException) {
                        Snackbar.make(mFloatWindow, "未找到翻译",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideFloatButton() {
        if(mFloatButton != null) {
            mWindowManager.removeView(mFloatButton);
            mFloatButton = null;
        }
    }

    private class FloatWindowListenerImpl implements FloatWindow.FloatWindowListener {
        private String mWord;

        public FloatWindowListenerImpl(String word) {
            mWord = word;
        }

        @Override
        public void onChangeExplain() {
            mFloatWindow.showLoading();
            showExplain(mWord, CambirdgeApi.DICTIONARY_CHINESE_URL);
        }

        @Override
        public void onStar() {
            WordbookDb wordbookDb = new WordbookDb(ClipboardMonitor.this);
            if(wordbookDb.wordExisted(mWord)) {
                Snackbar.make(mFloatWindow, "单词已存在",
                        Snackbar.LENGTH_SHORT).show();
            } else {
                wordbookDb.saveWord(mWord);
                Snackbar.make(mFloatWindow, "单词已保存至单词本",
                        Snackbar.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onClose() {
            mWindowManager.removeView(mContainLayout);
            mContainLayout = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
