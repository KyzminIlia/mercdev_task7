package com.example.voicememos;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class RecordService extends Service {

    public static final String SERVICE_TAG = RecordService.class.getSimpleName();

    private MediaRecorder mediaRecorder;
    private IBinder binder = new RecordBinder();
    private boolean isRecorded;

    @Override
    public void onDestroy() {
        Log.d(SERVICE_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d(SERVICE_TAG, "onCreate()");
        super.onCreate();

    }

    public void startRecording() {

        File memoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                "/VoiceMemos/");
        if (!memoFile.exists())
            memoFile.mkdir();
        memoFile = new File(memoFile.getPath(), "voicememo" + getString(R.string.deafult_extension));
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(memoFile.getPath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.d(SERVICE_TAG, "prepare() failed");
            e.printStackTrace();
        }

        mediaRecorder.start();
        isRecorded = true;
    }

    public void stopRecording() {
        Log.d(SERVICE_TAG, "recorder stopped in stoprecording. MediaRecorder is " + mediaRecorder);
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecorded = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean isRecorded() {
        return isRecorded;
    }

    public void setRecorded(boolean isRecorded) {
        this.isRecorded = isRecorded;
    }

    public class RecordBinder extends Binder {
        RecordService getService() {
            return RecordService.this;
        }

    }
}
