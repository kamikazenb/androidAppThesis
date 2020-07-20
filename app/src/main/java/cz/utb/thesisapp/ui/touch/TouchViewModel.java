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

import static cz.utb.thesisapp.GlobalValues.*;

public class TouchViewModel extends ViewModel {

    private static final String TAG = "TouchViewModel";
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<GlobalValues.Touch>> touch = new MutableLiveData<>();
    private MutableLiveData<Integer> test = new MutableLiveData<>();

    public TouchViewModel() {
        test.setValue(TOUCH_NO_TEST);
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

    public MutableLiveData<Integer> getTest() {
        return test;
    }

    public void setTest(int test) {
        if (test == TOUCH_FAB_TOUCHED) {
            if (this.test.getValue() == TOUCH_START_TEST) {
                this.test.setValue(TOUCH_BREAK_TEST);
            } else if (this.test.getValue() == TOUCH_BREAK_TEST | this.test.getValue() == TOUCH_NO_TEST) {
                this.test.setValue(TOUCH_START_TEST);
            }
        } else {
            this.test.setValue(test);
        }

    }
}