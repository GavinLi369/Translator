package gavinli.translator.imageexplain;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private StaggeredGridLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String key = getIntent().getExtras().getString(SearchFragment.INTENT_KEY);
        toolbar.setTitle(key);

        RecyclerView imageRecyclerView = (RecyclerView) findViewById(R.id.rv_imagelist);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageRecyclerAdapter(this);
        imageRecyclerView.setAdapter(mAdapter);
        imageRecyclerView.addOnScrollListener(new ScrollRefreshListener());
        new ImagePresenter(this, new ImageModel(key));
        mPresenter.loadMoreImages();
        mProgressBar = (ProgressBar) findViewById(R.id.bar_loading);
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
            int[] into = mLayoutManager.findLastVisibleItemPositions(null);
            mLastVisibleItem = Math.max(into[0], into[1]);
        }
    }

    @Override
    public void showMoreImages(List<String> urls) {
//        mAdapter.addImageUrls(urls);
//        mAdapter.notifyItemRangeInserted(0, urls.size());
    }

    @Override
    public void showMoreImage(Bitmap bitmap) {
        if(mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
        mAdapter.addImage(bitmap);
        mAdapter.notifyItemInserted(mAdapter.getItemCount() + 1);
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(this, "网络连接出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(ImageContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
