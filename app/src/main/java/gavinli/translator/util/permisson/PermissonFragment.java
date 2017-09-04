package gavinli.translator.util.permisson;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavin on 9/4/17.
 */

public class PermissonFragment extends Fragment {
    private static final int REQUEST_CODE = 20;

    private PermissonListener mPermissonListener;

    public void setListener(PermissonListener permissonListener) {
        mPermissonListener = permissonListener;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void request(String[] permissons) {
        List<String> needRequestPermisson = new ArrayList<>();
        for(String permisson : permissons) {
            if(ContextCompat.checkSelfPermission(getActivity(), permisson)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequestPermisson.add(permisson);
            }
        }
        if(!needRequestPermisson.isEmpty()) {
            requestPermissions(needRequestPermisson.toArray(new String[needRequestPermisson.size()]),
                    REQUEST_CODE);
        } else if(mPermissonListener != null) {
            mPermissonListener.onGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE) {
            List<String> deniedList = new ArrayList<>();
            int size = permissions.length;
            for(int i = 0; i < size; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    deniedList.add(permissions[i]);
                }
            }
            if(mPermissonListener != null) {
                if (deniedList.isEmpty()) {
                    mPermissonListener.onGranted();
                } else {
                    mPermissonListener.onDenied(deniedList.toArray(new String[deniedList.size()]));
                }
            }
        }
    }
}
