package gavinli.translator.worddetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailActivity extends AppCompatActivity implements WordDetailContract.View {
    public static final String INTENT_WORD_KEY = "word";

    private TextView mWordDefine;
    private ProgressBar mProgressBar;
    private WordDetailContract.Presenter mPresenter;

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

        new WordDetailPresenter(this, new WordDetailModel(this), this);
        Intent intent = getIntent();
        if(intent != null) {
            String word = intent.getStringExtra(INTENT_WORD_KEY);
            if(word != null && !word.isEmpty()) {
                mPresenter.loadWordExplain(word);
            }
        }
    }

    @Override
    public void showWordExplain(List<Spanned> spanneds) {
        mProgressBar.setVisibility(View.GONE);
        for(Spanned spanned : spanneds) {
            mWordDefine.append(spanned);
            mWordDefine.append("\n\n");
        }
    }

    @Override
    public void showNetworkError() {
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(WordDetailContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
