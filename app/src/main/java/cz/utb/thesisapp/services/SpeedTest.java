package cz.utb.thesisapp.services;

import android.os.AsyncTask;
import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

import static cz.utb.thesisapp.GlobalValues.EXTRA_UPLOAD;

public class SpeedTest {
    Broadcast broadcast;
    private static final String TAG = "SpeedTest";
    SpeedTestSocket speedTestSocket;

    public SpeedTest(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    public void startDownload() {
        Log.i(TAG, "startDownload: ~~");
        new DownloadSpeedTask().execute();
    }

    public class DownloadSpeedTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    onCompleteTask(report);
                    new UploadSpeedTask().execute();
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                    onErrorTask(speedTestError, errorMessage);
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    onProgressTask(percent, report);
                }
            });
            speedTestSocket.startDownload("ftp://speedtest.tele2.net/3MB.zip", 1000);
            return true;
        }
    }

    public class UploadSpeedTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    onCompleteTask(report);
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    onErrorTask(speedTestError, errorMessage);
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    onProgressTask(percent, report);
                }
            });

            speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 3000000, 1000);

            return true;
        }
    }

    public void onCompleteTask(SpeedTestReport report) {
        BigDecimal divisor = new BigDecimal("1000000");
        float speed = report.getTransferRateOctet().divide(divisor).round(new MathContext(3)).floatValue();
        broadcast.sendInfoFragmentSpeed(report.getSpeedTestMode().name(), speed, 101);
    }

    public void onProgressTask(float percent, SpeedTestReport report) {
        BigDecimal divisor = new BigDecimal("1000000");
        float speed = report.getTransferRateOctet().divide(divisor).round(new MathContext(3)).floatValue();
        broadcast.sendInfoFragmentSpeed(report.getSpeedTestMode().name(), speed, (int) percent);
    }

    public void onErrorTask(SpeedTestError speedTestError, String errorMessage) {
        broadcast.sendInfoFragmentSpeed(EXTRA_UPLOAD, 0, 100);
    }

}
