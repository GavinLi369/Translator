package gavinli.translator.clipboard;

import gavinli.translator.data.Explain;

/**
 * Created by gavin on 10/25/17.
 */

public interface FloatingExplainListener {
    /**
     * star按钮点击事件
     *
     * @param explain 当前正在显示的翻译，如果没有则为null
     */
    void onStar(Explain explain);

    /**
     * close按钮点击事件
     */
    void onClose();
}