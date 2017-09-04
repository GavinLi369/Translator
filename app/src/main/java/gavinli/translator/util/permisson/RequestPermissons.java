package gavinli.translator.util.permisson;

import android.app.Activity;
import android.app.FragmentManager;

/**
 * Created by gavin on 9/4/17.
 */

public class RequestPermissons {
    private static final String TAG = "Permissons";

    private PermissonFragment mFragment;

    public RequestPermissons(Activity activity) {
        mFragment = getPermissonFragment(activity);
    }

    private PermissonFragment getPermissonFragment(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        PermissonFragment fragment = (PermissonFragment) fragmentManager
                .findFragmentByTag(TAG);
        if(fragment == null) {
            fragment = new PermissonFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    public void request(String[] permissons, PermissonGrantedListener grantedListener) {
        request(permissons, grantedListener, null);
    }

    public void request(String[] permissons, PermissonGrantedListener grantedListener,
                        PermissonDeniedListener deniedListener) {
        mFragment.setListener(new PermissonListener() {
            @Override
            public void onGranted() {
                grantedListener.onGranted();
            }

            @Override
            public void onDenied(String[] deniedPermisson) {
                if(deniedListener != null) {
                    deniedListener.onDenied(deniedPermisson);
                }
            }
        });
        mFragment.request(permissons);
    }

    public interface PermissonGrantedListener {
        void onGranted();
    }

    public interface PermissonDeniedListener {
        void onDenied(String[] denied);
    }
}
