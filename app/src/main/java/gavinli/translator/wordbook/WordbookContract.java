package gavinli.translator.wordbook;

import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookContract {
    interface Presenter extends BasePresenter {
        void loadWords();

        void removeWord(String word);
    }

    interface View extends BaseView<Presenter> {
        void showWords(List<String> words);

        void showBackground();

        void hideBackground();
    }

    interface Model {
        List<String> getWords();

        void removeWord(String word);
    }
}
