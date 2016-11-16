package gavinli.translator.search;

import android.text.Spanned;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.search.util.HtmlDecoder;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchContract {
    interface Model {
        ArrayList<Spanned> getExplain(String word, HtmlDecoder.OnStaredLisenter lisenter) throws IOException, IndexOutOfBoundsException;

        ArrayList<String> getComplete(String key) throws IOException, JSONException;

        boolean wordExisted(String word);

        void saveWord(String word);
    }

    interface View extends BaseView<Presenter> {
        void showExplain(ArrayList<Spanned> explains);

        void showSuggestion(ArrayList<String> suggestions);

        void showNetworkError();

        void showNotFoundWordError();

        void showWordInfo(String info);
    }

    interface Presenter extends BasePresenter {
        void loadExplain(String word);

        void loadAutoComplete(String key);
    }
}
