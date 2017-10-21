package gavinli.translator.data;

import java.util.List;

/**
 * Created by gavin on 10/21/17.
 */

public class Explain {
    /**
     * 该翻译所对应的单词或短语
     */
    private String mKey;

    /**
     * 翻译概览
     */
    private String mSummary;

    /**
     * 翻译信息，一般使用{@link android.text.Spanned}。
     */
    private List<CharSequence> mSource;

    public void setKey(String key) {
        mKey = key;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public void setSource(List<CharSequence> source) {
        mSource = source;
    }

    public String getKey() {
        return mKey;
    }

    public String getSummary() {
        return mSummary;
    }

    public List<CharSequence> getSource() {
        return mSource;
    }
}
