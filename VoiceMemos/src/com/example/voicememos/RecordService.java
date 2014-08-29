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
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class RecordService extends Service {

    public static final String SERVICE_TAG = RecordService.class.getSimpleName();

    public static final String ACTION_UPDATE_TIME = "UPDATE_TIME";
    public static final String ACTION_FINISH = "FINISH";

    public static final String EXTRA_TIME = "TIME_UNTIL";

    private MediaRecorder mediaRecorder;
    private CountDownTimer timer;
    private BroadcastReceiver receiver;

    @Override
    public void onDestroy() {
        if (mediaRecorder != null)
            stopRecording();
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                if (intent.getAction().equals(VoiceMemosRecordActivity.ACTION_STOP_RECORD)) {
                    timer.cancel();
                    stopRecording();

                }
                if (intent.getAction().equals(VoiceMemosRecordActivity.ACTION_START_RECORD)) {
                    startRecording();
                    timer = new CountDownTimer(11000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            Intent tickIntent = new Intent(ACTION_UPDATE_TIME);
                            tickIntent.putExtra(EXTRA_TIME, (millisUntilFinished - 1) / 1000);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(tickIntent);
                            Log.d(SERVICE_TAG, "onTick()");
                        }

                        @Override
                        public void onFinish() {
                            Intent finishIntent = new Intent(ACTION_FINISH);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(finishIntent);
                            stopRecording();
                            Log.d(SERVICE_TAG, "onFinish()");
                        }
                    }.start();

                }
            }

        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VoiceMemosRecordActivity.ACTION_STOP_RECORD);
        intentFilter.addAction(VoiceMemosRecordActivity.ACTION_START_RECORD);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, intentFilter);
        Log.d(SERVICE_TAG, "service onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(VoiceMemosRecordActivity.ACTION_START_RECORD)) {
            startRecording();
            timer = new CountDownTimer(11000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    Intent tickIntent = new Intent(ACTION_UPDATE_TIME);
                    tickIntent.putExtra(EXTRA_TIME, (millisUntilFinished - 1) / 1000);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(tickIntent);
                    Log.d(SERVICE_TAG, "onTick()");
                }

                @Override
                public void onFinish() {
                    Intent finishIntent = new Intent(ACTION_FINISH);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(finishIntent);
                    stopRecording();
                    Log.d(SERVICE_TAG, "onFinish()");
                }
            }.start();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startRecording() {

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
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
