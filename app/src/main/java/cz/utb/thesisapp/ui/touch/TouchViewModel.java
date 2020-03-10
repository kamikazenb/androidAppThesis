package cz.utb.thesisapp.ui.touch;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TouchViewModel extends ViewModel {
    private static final String TAG = "GalleryViewModel";
    private MutableLiveData<String> mText;

    public TouchViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue(" ");
        Log.i(TAG, "::GalleryViewModel");
    }

    public LiveData<String> getText() {
        return mText;
    }
}