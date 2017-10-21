package gavinli.translator.wordbook;

import java.util.List;

import gavinli.translator.BasePresenter;
import gavinli.translator.BaseView;
import gavinli.translator.data.Explain;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public class WordbookContract {
    interface Presenter extends BasePresenter {
        void loadWords();

        void removeWord(Explain word);

        void restoreWord();
    }

    interface View extends BaseView<Presenter> {
        void showWords(List<Explain> words);

        void showBackground();

        void hideBackground();
    }

    interface Model {
        List<Explain> getWords();

        void removeWord(Explain explain);

        void saveWord(Explain explain);
    }
}
