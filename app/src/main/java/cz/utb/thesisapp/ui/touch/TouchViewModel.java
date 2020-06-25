package cz.utb.thesisapp.ui.touch;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TouchViewModel extends ViewModel {

    private static final String TAG = "TouchViewModel";
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<Float>> touchStart = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Float>> touchMove = new MutableLiveData<>();
    private MutableLiveData<Boolean> touchUp = new MutableLiveData<>();



    public TouchViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue(" ");
        Log.i(TAG, "::TouchViewModel");
        touchUp.setValue(false);
    }




    public MutableLiveData<ArrayList<Float>> getTouchMove() {
        return touchMove;
    }

    public void setTouchMove(ArrayList<Float> touchStart) {
        this.touchMove.setValue(touchStart);
    }

    public MutableLiveData<Boolean> getTouchUp() {
        return touchUp;
    }

    public void setTouchUp(Boolean touchUp) {
        this.touchUp.setValue(touchUp);
    }

    public MutableLiveData<ArrayList<Float>> getTouchStart() {
        return touchStart;
    }

    public void setTouchStart(ArrayList<Float> touchStart) {
        this.touchStart.setValue(touchStart);
    }

    public LiveData<String> getText() {
        return mText;
    }
}