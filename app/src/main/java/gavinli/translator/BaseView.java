package gavinli.translator;

/**
 * Created by GavinLi
 * on 16-11-16.
 */

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
