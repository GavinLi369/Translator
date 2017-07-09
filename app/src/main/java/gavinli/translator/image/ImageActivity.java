package gavinli.translator.image;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import java.util.ArrayList;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.search.SearchFragment;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class ImageActivity extends AppCompatActivity implements ImageContract.View {
    private ImageContract.Presenter mPresenter;

    private ImageRecyclerAdapter mAdapter;
    private FlexboxLayoutManager mLayoutManager;
    private AnimatorSet mCurrentAnimator;

    private int PLACE_HOLD_WIDTH;
    private int PLACE_HOLD_HEIGHT;

    private final int[] mPlaceHolder = {
            R.color.colorPlaceHold1,
            R.color.colorPlaceHold2,
            R.color.colorPlaceHold3,
            R.color.colorPlaceHold4,
            R.color.colorPlaceHold5,
    };

    private boolean mIsLoading = true;
    private static final int LOAD_NUM = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String key = getIntent().getExtras().getString(SearchFragment.INTENT_KEY);
        toolbar.setTitle(key);
        setSupportActionBar(toolbar);
        //开启Back按钮
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);

        RecyclerView imageRecyclerView = (RecyclerView) findViewById(R.id.rv_imagelist);
        mLayoutManager = new FlexboxLayoutManager(this);
        mLayoutManager.setFlexDirection(FlexDirection.ROW);
        mLayoutManager.setFlexWrap(FlexWrap.WRAP);
        mLayoutManager.setAlignItems(AlignItems.STRETCH);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageRecyclerAdapter();
        mAdapter.setOnItemClickLinstener((view, postion) ->
            zoomImageFromThumb(view, mAdapter.getImages().get(postion)));
        imageRecyclerView.setAdapter(mAdapter);
        imageRecyclerView.addOnScrollListener(new ScrollRefreshListener());

        PLACE_HOLD_WIDTH = (int) (getResources().getDisplayMetrics().widthPixels / 2.5);
        PLACE_HOLD_HEIGHT = (int) (PLACE_HOLD_WIDTH / 1.3);

        new ImagePresenter(this, new ImageModel(this, key));
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
            if(!mIsLoading && newState == RecyclerView.SCROLL_STATE_IDLE &&
                    mLastVisibleItem + 1 == mAdapter.getItemCount()) {
                mIsLoading = true;
                mAdapter.showLoadingFooter();
                mPresenter.loadImages(LOAD_NUM);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mLastVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition();
        }
    }

    @Override
    public void showImage(Bitmap bitmap, int postion) {
        mAdapter.setImage(bitmap, postion);
        mAdapter.notifyItemChanged(postion);
    }

    @Override
    public void showNotMoreImages() {
        mAdapter.showNotMoreImages();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(this, "网络连接出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPlaceholders(int num) {
        mAdapter.removeLoadingFooter();
        mIsLoading = false;

        List<Bitmap> bitmaps = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            Bitmap bitmap = Bitmap.createBitmap(
                    PLACE_HOLD_WIDTH,
                    PLACE_HOLD_HEIGHT,
                    Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(getResources()
                    .getColor(mPlaceHolder[(int) (Math.random() * 5)]));
            bitmaps.add(bitmap);
        }
        mAdapter.addImages(bitmaps);
        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(),
                bitmaps.size());
    }

    @Override
    public void setPresenter(ImageContract.Presenter presenter) {
        mPresenter = presenter;
        mAdapter.showLoadingFooter();
        mPresenter.loadImages(LOAD_NUM);
    }
}
