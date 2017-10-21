package gavinli.translator.search;

import java.io.IOException;
import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.data.Explain;
import gavinli.translator.util.ExplainNotFoundException;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchContract {
    interface Model {
        Explain getExplain(String word)
                throws IOException, ExplainNotFoundException;

        List<String> getComplete(String key, int num) throws IOException;

        boolean saveWord(Explain explain);
    }

    interface View extends BaseView<Presenter> {
        void showExplain(Explain explain);

        void showSuggestion(List<String> suggestions);

        void showNetworkError();

        void showNotFoundWordError();

        void showWordInfo(String info);

        void showBackground();

        void hideBackground();
    }

    interface Presenter extends BasePresenter {
        void loadExplain(String word);

        void loadAutoComplete(String key, int num);

        void cancelAutoCompleteIfCompleting();

        void saveWord();

        String getCurrentWord();
    }
}
