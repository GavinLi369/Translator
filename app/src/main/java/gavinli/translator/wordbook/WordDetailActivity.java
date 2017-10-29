package gavinli.translator.wordbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import gavinli.translator.R;
import gavinli.translator.data.Explain;
import gavinli.translator.data.source.remote.ExplainLoader;
import gavinli.translator.data.ExplainNotFoundException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailActivity extends AppCompatActivity {
    public static final String INTENT_WORD_KEY = "word";

    private TextView mWordDefine;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worddetail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        mWordDefine = (TextView) findViewById(R.id.tv_define);
        mWordDefine.setMovementMethod(LinkMovementMethod.getInstance());
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if(intent != null) {
            String word = intent.getStringExtra(INTENT_WORD_KEY);
            if(word != null && !word.isEmpty()) {
                Observable
                        .create((Observable.OnSubscribe<Explain>) subscriber -> {
                            try {
                                String key = word.replace(" ", "-");
                                Explain explain = ExplainLoader
                                        .with(this)
                                        .search(key)
                                        .load();
                                subscriber.onNext(explain);
                            } catch (ExplainNotFoundException | IOException e) {
                                e.printStackTrace();
                                subscriber.onError(e);
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::showWordExplain
                                , e -> {
                                    if(e instanceof IOException) {
                                        showNetworkError();
                                    } else if(e instanceof ExplainNotFoundException) {
                                        showExplainNotFoundError();
                                    }
                                });
            }
        }
    }

    private void showWordExplain(Explain explain) {
        mProgressBar.setVisibility(View.GONE);
        for(CharSequence spanned : explain.getSource()) {
            mWordDefine.append(spanned);
            mWordDefine.append("\n\n");
        }
    }

    private void showNetworkError() {
        mProgressBar.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content),
                "网络连接失败", Snackbar.LENGTH_SHORT).show();
    }

    private void showExplainNotFoundError() {
        mProgressBar.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content),
                "该单词暂无解释", Snackbar.LENGTH_SHORT).show();
    }
}
