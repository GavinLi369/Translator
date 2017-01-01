package gavinli.translator.clipboard;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gavinli.translator.R;
import gavinli.translator.util.CambirdgeApi;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-27.
 */

public class ClipboardMonitor extends Service
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private static int GRAY_SERVICE_ID = 1001;
    public static final String INTENT_WORD = "word";
    private static final int FLOAT_WINDOW_TIME = 4000;

    private ClipboardManager mClipboardManager;
    private WindowManager mWindowManager;
    private View mFloatButton;
    private FloatWindow mFloatWindow;
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        //利用Android漏洞进程保活
        Intent innerInent = new Intent(this, InnerClass.class);
        startService(innerInent);
        startForeground(GRAY_SERVICE_ID, new Notification());
        return super.onStartCommand(intent, flags, startId);
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
                    (text.equals(mPreviousText) && mFloatButton != null)) return;
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
        if(mFloatButton != null) {
            hideFloatWindow();
        }
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        mFloatButton = new FloatButton(this);
        mFloatButton.setOnClickListener(view -> {
            hideFloatWindow();

            LinearLayout containLayout = new LinearLayout(this);
            containLayout.setBackgroundResource(R.color.colorFloatWindowContain);
            //单击解释区域外部，则关闭悬浮框
            containLayout.setOnTouchListener((v, event) -> {
                Rect rect = new Rect();
                mFloatWindow.getGlobalVisibleRect(rect);
                if(!rect.contains((int) event.getX(), (int) event.getY())) {
                    mWindowManager.removeView(containLayout);
                    return true;
                } else {
                    return false;
                }
            });

            mFloatWindow = new FloatWindow(ClipboardMonitor.this);
            mFloatWindow.setOnCloseListener(() -> mWindowManager.removeView(containLayout));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(450, 400);
            layoutParams.setMargins((screenWidth - 450) / 2, 100, 0, 0);
            mFloatWindow.setLayoutParams(layoutParams);
            containLayout.addView(mFloatWindow);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.gravity = Gravity.START | Gravity.TOP;
            params.width = screenWidth;
            params.height = screenHeight;
            params.x = 0;
            params.y = 0;
            mWindowManager.addView(containLayout, params);
            Observable<List<Spanned>> observable = Observable.create(new Observable.OnSubscribe<List<Spanned>>() {
                @Override
                public void call(Subscriber<? super List<Spanned>> subscriber) {
                    Logger.d("call");
                    try {
                        subscriber.onNext(CambirdgeApi.getExplain(ClipboardMonitor.this, word, null));
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
            observable.subscribe(new Observer<List<Spanned>>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    Logger.d("onError");
                    if(e instanceof IOException) {
                        Toast.makeText(ClipboardMonitor.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNext(List<Spanned> spanneds) {
                    Logger.d("OnNext");
                    mFloatWindow.setExplain(spanneds);
                }
            });
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
        mWindowManager.addView(mFloatButton, params);
    }

    private void hideFloatWindow() {
        if(mFloatButton != null) {
            mWindowManager.removeView(mFloatButton);
            mFloatButton = null;
        }
    }

    public static class InnerClass extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
