package cz.utb.thesisapp.ui.gallery;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {
    private static final String TAG = "GalleryViewModel";
    private MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
        Log.i(TAG, "::GalleryViewModel");
    }

    public LiveData<String> getText() {
        return mText;
    }
}