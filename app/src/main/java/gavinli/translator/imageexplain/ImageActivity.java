package gavinli.translator.imageexplain;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

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
    private ProgressBar mProgressBar;
    private AnimatorSet mCurrentAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String key = getIntent().getExtras().getString(SearchFragment.INTENT_KEY);
        toolbar.setTitle(key);

        RecyclerView imageRecyclerView = (RecyclerView) findViewById(R.id.rv_imagelist);
        mLayoutManager = new FlexboxLayoutManager();
        mLayoutManager.setFlexDirection(FlexDirection.ROW);
        mLayoutManager.setFlexWrap(FlexWrap.WRAP);
        mLayoutManager.setAlignItems(AlignItems.STRETCH);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageRecyclerAdapter(this);
        mAdapter.setOnItemClickLinstener((view, postion) ->
            zoomImageFromThumb(view, mAdapter.getImages().get(postion)));
        imageRecyclerView.setAdapter(mAdapter);
        imageRecyclerView.addOnScrollListener(new ScrollRefreshListener());
        mProgressBar = (ProgressBar) findViewById(R.id.bar_loading);

        new ImagePresenter(this, new ImageModel(key));
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
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                    mLastVisibleItem + 1 == mAdapter.getItemCount()) {
                mPresenter.loadMoreImages();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            mLastVisibleItem = mLayoutManager.getFlexItemCount();
        }
    }

    @Override
    public void showMoreImage(Bitmap bitmap, int postion) {
        if(mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
        mAdapter.setImage(bitmap, postion);
        mAdapter.notifyItemChanged(postion);
    }

    @Override
    public void showPlaceHolds(List<Bitmap> bitmaps) {
        if(mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
        mAdapter.addImages(bitmaps);
        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(),
                mAdapter.getItemCount() + bitmaps.size());
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(this, "网络连接出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(ImageContract.Presenter presenter) {
        mPresenter = presenter;
        mPresenter.loadMoreImages();
    }
}
