package gavinli.translator.util.permisson;

/**
 * Created by gavin on 9/4/17.
 */

public interface PermissonListener {
    void onGranted();
    void onDenied(String[] deniedPermisson);
}
