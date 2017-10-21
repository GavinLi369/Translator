package gavinli.translator.worddetail;

import java.io.IOException;
import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.util.ExplainNotFoundException;

/**
 * Created by GavinLi
 * on 16-12-30.
 */

public class WordDetailContract {
    interface Model {
        List<CharSequence> getExplain(String word)
                throws IOException, ExplainNotFoundException;
    }

    interface View extends BaseView<Presenter> {
        void showWordExplain(List<CharSequence> spanneds);

        void showNetworkError();

        void showExplainNotFoundError();
    }

    interface Presenter extends BasePresenter {
        void loadWordExplain(String word);
    }
}
