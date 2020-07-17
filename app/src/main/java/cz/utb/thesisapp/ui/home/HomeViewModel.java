package cz.utb.thesisapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private MutableLiveData<Boolean> mBounded = new MutableLiveData<>();
    private MutableLiveData<Boolean> requireRefresh = new MutableLiveData<>();
    private MutableLiveData<Boolean> kryoConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> webConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> kryoUseDatabase = new MutableLiveData<>();
    private MutableLiveData<HashMap<String, String>> users = new MutableLiveData<>();

    public HomeViewModel() {
        mBounded.setValue(false);
        requireRefresh.setValue(false);
        kryoConnected.setValue(false);
        kryoUseDatabase.setValue(true);
        webConnected.setValue(false);
    }


    public MutableLiveData<Boolean> getKryoConnected() {
        return kryoConnected;
    }

    public void setKryoConnected(Boolean kryoConnected) {
        this.kryoConnected.setValue(kryoConnected);
    }
    public MutableLiveData<Boolean> getWebConnected() {
        return webConnected;
    }

    public void setWebConnected(Boolean webConnected) {
        this.webConnected.setValue(webConnected);
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


}