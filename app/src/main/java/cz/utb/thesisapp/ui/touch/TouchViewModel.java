package cz.utb.thesisapp.ui.touch;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.services.kryonet.Network;

public class TouchViewModel extends ViewModel {

    private static final String TAG = "TouchViewModel";
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<GlobalValues.Touch>> touch = new MutableLiveData<>();

    public TouchViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue(" ");
//        touchUp.setValue(false);
    }
    public MutableLiveData<ArrayList<GlobalValues.Touch>> getTouch() {
        return touch;
    }

    public void setTouch(ArrayList<GlobalValues.Touch> touchStart) {
        this.touch.setValue(touchStart);
    }

    public LiveData<String> getText() {
        return mText;
    }
}