package com.example.voicememos;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class VoiceMemosRecordFragment extends Fragment implements OnClickListener {
    public final static String MEMOS_RECORD_FRAGMENT_TAG = VoiceMemosRecordFragment.class.getSimpleName();

    private ImageButton recordButton;
    private TextView countDownTextView;
    private int backgroundImage;
    private SaveVoiceMemoDialog saveDialog;
    private MediaRecorder mediaRecorder;
    private CountDownTimer timer;
    private boolean isRecord = false;

    @Override
    public void onDestroy() {
        if (isRecord)
            stopRecording();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recordButton = (ImageButton) view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(this);
        countDownTextView = (TextView) view.findViewById(R.id.time_countdown_text_view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        recordButton.setBackgroundResource(backgroundImage);
        super.onResume();
    }

    private void startRecording() {

        File memoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                "/VoiceMemos/");
        if (!memoFile.exists())
            memoFile.mkdir();
        memoFile = new File(memoFile.getPath(), "voicememo.3gp");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(memoFile.getPath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        isRecord = true;
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.d(MEMOS_RECORD_FRAGMENT_TAG, "prepare() failed");
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        isRecord = false;
        mediaRecorder = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        saveDialog = new SaveVoiceMemoDialog();
        backgroundImage = R.drawable.record_icon;
        super.onCreate(savedInstanceState);
        mediaRecorder = new MediaRecorder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_record_memos, null);
    }

    boolean stop = false;

    @Override
    public void onClick(View v) {

        if (stop) {
            stop = false;
            recordButton.setBackgroundResource(R.drawable.record_icon);
            backgroundImage = R.drawable.record_icon;
            saveDialog.show(getActivity().getSupportFragmentManager(), SaveVoiceMemoDialog.DIALOG_TAG);
            stopRecording();
            timer.cancel();
        } else {
            stop = true;
            timer = new CountDownTimer(11000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    countDownTextView.setVisibility(TextView.VISIBLE);
                    countDownTextView.setText(millisUntilFinished / 1000 + " " + getString(R.string.remainig_time));
                }

                @Override
                public void onFinish() {
                    countDownTextView.setVisibility(TextView.INVISIBLE);
                    stop = false;
                    recordButton.setBackgroundResource(R.drawable.record_icon);
                    saveDialog.show(getActivity().getSupportFragmentManager(), SaveVoiceMemoDialog.DIALOG_TAG);
                    stopRecording();

                }
            }.start();
            recordButton.setBackgroundResource(R.drawable.stop_record_icon);
            backgroundImage = R.drawable.stop_record_icon;
            startRecording();
        }

    }
}
