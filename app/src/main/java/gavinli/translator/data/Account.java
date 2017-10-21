package gavinli.translator.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gavin on 9/17/17.
 */

public class Account implements Parcelable {
    public final String id;
    public final String name;
    public final String password;
    public final Bitmap face;

    public Account(String id, String name, String password, Bitmap face) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.face = face;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(password);
        parcel.writeParcelable(face, i);
    }

    public static Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel parcel) {
            String id = parcel.readString();
            String name = parcel.readString();
            String password = parcel.readString();
            Bitmap bitmap = parcel.readParcelable(Bitmap.class.getClassLoader());
            return new Account(id, name, password, bitmap);
        }

        @Override
        public Account[] newArray(int i) {
            return new Account[i];
        }
    };
}
