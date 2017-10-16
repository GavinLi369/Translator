package gavinli.translator.imagelink;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.search.SearchFragment;
import gavinli.translator.util.imageloader.ImageLoader;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageActivity extends AppCompatActivity implements ImageContract.View {
    private ImageContract.Presenter mPresenter;

    private ImageAdapter mAdapter;
    private FlexboxLayoutManager mLayoutManager;
    private AnimatorSet mCurrentAnimator;

    private static final int LOAD_NUM = 10;

    private boolean mNoMore = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String key = getIntent().getExtras().getString(SearchFragment.INTENT_KEY);
        toolbar.setTitle(key);
        setSupportActionBar(toolbar);
        //开启Back按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);

        buildRecyclerView();

        new ImagePresenter(this, new ImageModel(key));
    }

    private void buildRecyclerView() {
        RecyclerView imageRecyclerView = (RecyclerView) findViewById(R.id.rv_imagelist);
        mLayoutManager = new FlexboxLayoutManager(this);
        mLayoutManager.setFlexDirection(FlexDirection.ROW);
        mLayoutManager.setFlexWrap(FlexWrap.WRAP);
        mLayoutManager.setAlignItems(AlignItems.STRETCH);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this);
        mAdapter.setOnItemClickLinstener(this::onItemClick);
        imageRecyclerView.setAdapter(mAdapter);
        imageRecyclerView.addOnScrollListener(new ScrollRefreshListener());
    }

    private void onItemClick(View view, int postion) {
        new Thread(() -> {
            try {
                Bitmap image = ImageLoader.with(this)
                        .load(mAdapter.getImageLinks().get(postion))
                        .get();
                this.runOnUiThread(() -> zoomImageFromThumb(view, image));
            } catch (IOException e) {
                this.runOnUiThread(this::showNetworkError);
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void zoomImageFromThumb(View view, Bitmap bitmap) {
        if(mCurrentAnimator != null && mCurrentAnimator.isRunning())
            mCurrentAnimator.cancel();
        ImageView expandedView = (ImageView) findViewById(R.id.img_expanded);
        ImageView maskView = (ImageView) findViewById(R.id.img_mask);
        expandedView.setOnClickListener(v -> {
            expandedView.setVisibility(View.GONE);
            maskView.setVisibility(View.GONE);
        });
        expandedView.setImageBitmap(bitmap);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        view.getGlobalVisibleRect(startBounds);
        findViewById(R.id.rv_imagelist).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float scale = (float) startBounds.width() / finalBounds.width();
        float startHeight = scale * finalBounds.height();
        float deltaHeight = (startHeight - startBounds.height()) / 2;
        startBounds.top -= deltaHeight;
        startBounds.bottom += deltaHeight;

        expandedView.setVisibility(View.VISIBLE);

        expandedView.setPivotX(0);
        expandedView.setPivotY(0);

        mCurrentAnimator = new AnimatorSet();
        mCurrentAnimator
                .play(ObjectAnimator.ofFloat(expandedView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_X,
                        scale, 1))
                .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_Y,
                        scale, 1))
                .with(ObjectAnimator.ofFloat(expandedView, View.ALPHA,
                        0, 1));
        mCurrentAnimator.setDuration(500);
        mCurrentAnimator.setInterpolator(new DecelerateInterpolator());
        maskView.setVisibility(View.VISIBLE);
        mCurrentAnimator.start();
    }

    class ScrollRefreshListener extends RecyclerView.OnScrollListener {
        private int mLastVisibleItem;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if(!mNoMore && newState == RecyclerView.SCROLL_STATE_IDLE &&
                    mLastVisibleItem + 1 == mAdapter.getItemCount()) {
                Observable<String> observable = mPresenter.loadImages(LOAD_NUM);
                observable.collect((Func0<List<String>>) ArrayList::new, List::add)
                    .subscribe(links -> {
                    if (!links.isEmpty()) {
                        for (String link : links) {
                            mAdapter.addImageLinks(link);
                        }
                        mAdapter.notifyItemRangeChanged(mAdapter.getItemCount(), links.size());
                    } else {
                        mNoMore = true;
                        mAdapter.showNotMoreImages();
                    }
                }, Throwable::printStackTrace);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mLastVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition();
        }
    }

    private void showNetworkError() {
        Toast.makeText(this, "网络连接出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(ImageContract.Presenter presenter) {
        mPresenter = presenter;
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_dialog_message));
        progressDialog.show();
        Observable<String> observable = mPresenter.loadImages(LOAD_NUM);
        observable.subscribe(link -> {
            progressDialog.cancel();
            if(link != null) {
                mAdapter.addImageLinks(link);
                mAdapter.notifyItemInserted(mAdapter.getItemCount());
            } else {
                mAdapter.showNotMoreImages();
            }
        }, throwable -> {
            showNetworkError();
            progressDialog.cancel();
        });
    }
}
