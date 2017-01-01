package gavinli.translator.search;

import android.text.Spanned;

import java.io.IOException;
import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.util.HtmlDecoder;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchContract {
    interface Model {
        List<Spanned> getExplain(String word,
                                 HtmlDecoder.OnStaredLisenter lisenter)
                throws IOException, IndexOutOfBoundsException;

        List<String> getComplete(String key, int num) throws IOException;

        boolean wordExisted(String word);

        void saveWord(String word);
    }

    interface View extends BaseView<Presenter> {
        void showExplain(List<Spanned> explains);

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
    }
}
