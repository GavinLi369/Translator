package gavinli.translator.search;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class WordSuggestion implements SearchSuggestion {
    private String mWord;

    public WordSuggestion(String word) {
        mWord = word;
    }

    public WordSuggestion(Parcel parcel) {
        mWord = parcel.readString();
    }

    @Override
    public String getBody() {
        return mWord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mWord);
    }

    public static final Creator<WordSuggestion> CREATOR = new Creator<WordSuggestion>() {
        @Override
        public WordSuggestion createFromParcel(Parcel parcel) {
            return new WordSuggestion(parcel);
        }

        @Override
        public WordSuggestion[] newArray(int i) {
            return new WordSuggestion[i];
        }
    };
}
