package cz.utb.thesisapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private MutableLiveData<HashMap<String, String>> users = new MutableLiveData<>();
    private MutableLiveData<Boolean> mBounded = new MutableLiveData<>();
    private MutableLiveData<Boolean> requireRefresh = new MutableLiveData<>();
    private MutableLiveData<Boolean> kryoConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> kryoUseDatabase = new MutableLiveData<>();

    private MutableLiveData<Boolean> webConnected = new MutableLiveData<>();

    private MutableLiveData<Boolean> firebaseConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> firebaseRemoteListener = new MutableLiveData<>();

    public HomeViewModel() {
        mBounded.setValue(false);
        requireRefresh.setValue(false);
        kryoConnected.setValue(false);
        kryoUseDatabase.setValue(true);
        webConnected.setValue(false);
        firebaseConnected.setValue(false);
        firebaseRemoteListener.setValue(false);
    }


    public MutableLiveData<Boolean> getKryoConnected() {
        return kryoConnected;
    }

    public void setKryoConnected(Boolean kryoConnected) {
        if (kryoConnected) {
            setFirebaseConnected(false);
            setWebConnected(false);
        }
        this.kryoConnected.setValue(kryoConnected);
    }

    public MutableLiveData<Boolean> getWebConnected() {
        return webConnected;
    }

    public void setWebConnected(Boolean webConnected) {
        if (webConnected) {
            setKryoConnected(false);
            setFirebaseConnected(false);
        }
        this.webConnected.setValue(webConnected);
    }

    public MutableLiveData<Boolean> getFirebaseConnected() {
        return firebaseConnected;
    }

    public void setFirebaseConnected(Boolean firebaseConnected) {
        if (firebaseConnected) {
            setWebConnected(false);
            setKryoConnected(false);
        }else {
            setFirebaseRemoteListener(false);
        }
        this.firebaseConnected.setValue(firebaseConnected);
    }

    public MutableLiveData<HashMap<String, String>> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users.setValue(users);
    }

    public MutableLiveData<Boolean> getRequireRefresh() {
        return requireRefresh;
    }

    public void setRequireRefresh(Boolean requireRefresh) {
        this.requireRefresh.setValue(requireRefresh);
    }

    public MutableLiveData<Boolean> getKryoUseDatabase() {
        return kryoUseDatabase;
    }

    public void setKryoUseDatabase(Boolean kryoUseDatabase) {
        this.kryoUseDatabase.setValue(kryoUseDatabase);
    }

    public void setmBounded(Boolean _mBounded) {
        mBounded.setValue(_mBounded);
    }

    public MutableLiveData<Boolean> getmBounded() {
        return mBounded;
    }



    public void setFirebaseRemoteListener(boolean firebaseRemoteListener) {

        this.firebaseRemoteListener.setValue(firebaseRemoteListener);
    }

    public MutableLiveData<Boolean> getFirebaseRemoteListener() {
        return firebaseRemoteListener;
    }
}



