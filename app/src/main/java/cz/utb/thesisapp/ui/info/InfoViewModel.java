package cz.utb.thesisapp.ui.info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cz.utb.thesisapp.MainActivity;

public class InfoViewModel extends ViewModel {
    public List download = Collections.synchronizedList(new ArrayList<Entry>());
    volatile LineDataSet setDownloads = new LineDataSet(download, "Download MB/s *100");
    private MutableLiveData<LineDataSet> dl = new MutableLiveData<>();

    public List upload = Collections.synchronizedList(new ArrayList<Entry>());
    volatile LineDataSet setUploads = new LineDataSet(upload, "Upload MB/s *100");
    private MutableLiveData<LineDataSet> up = new MutableLiveData<>();

    public List delays = Collections.synchronizedList(new ArrayList<Entry>());
    volatile LineDataSet setDelays = new LineDataSet(delays, "Delay");
    private MutableLiveData<LineDataSet> delay = new MutableLiveData<>();

    private MutableLiveData<String> downloadSpeedText = new MutableLiveData<>();
    private MutableLiveData<String> uploadSpeedText= new MutableLiveData<>();
    private MutableLiveData<Integer> progress = new MutableLiveData<>();

    public InfoViewModel() {
        downloadSpeedText.setValue("");
        uploadSpeedText.setValue("");
        progress.setValue(0);
        dl.setValue(setDownloads);
        up.setValue(setUploads);
        delay.setValue(setDelays);
    }

    public void setDownloadSpeedText(String downloadSpeedText) {
        this.downloadSpeedText.setValue(downloadSpeedText);
    }
    public void setUploadSpeedText(String uploadSpeedText){
        this.uploadSpeedText.setValue(uploadSpeedText);
    }
    public void setProgress(Integer progress){
        this.progress.setValue(progress);
    }

    public MutableLiveData<Integer> getProgress() {
        return progress;
    }

    public MutableLiveData<String> getDownloadSpeedText() {
        return downloadSpeedText;
    }

    public MutableLiveData<String> getUploadSpeedText() {
        return uploadSpeedText;
    }

    public void uploadAddEntry(Entry entry){
        upload.add(entry);
    }
   public void downloadAddEntry(Entry entry){
        download.add(entry);
   }
   public void delayAddEntry(Entry entry){
        delays.add(entry);
   }
    public LineDataSet getDl (){
        return dl.getValue();
    }
    public LineDataSet getUp (){
        return up.getValue();
    }
    public LineDataSet getDelay(){
        return delay.getValue();
    }
    public List getDelays(){
        return delays;
    }

    public LiveData<String> getText() {
        return downloadSpeedText;
    }


}