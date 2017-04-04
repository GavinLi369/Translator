package gavinli.translator.worddetail;

import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailContract {
    interface Model {
        List<Spanned> getExplain(String word)
                throws IOException, IndexOutOfBoundsException ;
    }

    interface View extends BaseView<Presenter> {
        void showWordExplain(List<Spanned> spanneds);

        void showNetworkError();
    }

    interface Presenter extends BasePresenter {
        void loadWordExplain(String word);
    }
}
