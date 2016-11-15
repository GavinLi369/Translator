package gavinli.translator.search;

import android.text.Spanned;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchContract {
    interface Model {
        ArrayList<Spanned> getExplain(String word) throws IOException, IndexOutOfBoundsException;

        ArrayList<String> getComplete(String key) throws IOException, JSONException;
    }

    interface View {
        void showExplain(ArrayList<Spanned> explains);

        void showSuggestion(ArrayList<String> suggestions);

        void showNetworkError();

        void showNotFoundWordError();

        void setPresent(Presenter presenter);
    }

    interface Presenter {
        void loadExplain(String word);

        void loadAutoComplete(String key);
    }
}
