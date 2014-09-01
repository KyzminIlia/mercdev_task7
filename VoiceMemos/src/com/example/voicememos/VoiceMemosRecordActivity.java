package com.example.voicememos;

import java.io.File;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.voicememos.RecordService.RecordBinder;

public class VoiceMemosRecordActivity extends FragmentActivity implements OnClickListener {
    public final static String MEMOS_RECORD_ACTIVITY_TAG = VoiceMemosRecordActivity.class.getSimpleName();

    public final static String ACTION_STOP_RECORD = "STOP_RECORD";
    public final static String ACTION_START_RECORD = "START_RECORD";

    public static final String EXTRA_NAME = "VoiceMemoName";

    public static final String PREFS_DATE_TIME = "DateTime";
    public static final String PREFS_SHOW_DIALOG = "ShowDialog";

    private Intent serviceIntent;
    private ImageButton recordButton;
    private TextView countDownTextView;
    private int backgroundImage;

    private CountDownTimer timer;
    private boolean isBinded = false;
    private BroadcastReceiver receiver;
    private String formatedIndex;
    private Intent saveMemoIntent;
    private boolean isDialogShowed = false;

    RecordService recordService;

    @Override
    protected void onStop() {
        if (isBinded)
            unbindService(connection);
        isBinded = false;
        super.onStop();
    }

    @Override
    protected void onStart() {
        Intent recordServiceIntent = new Intent(this, RecordService.class);
        bindService(recordServiceIntent, connection, Context.BIND_AUTO_CREATE);

        super.onStart();
    }

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_STOP_RECORD));
        if (recordService.isRecorded()) {
            recordService.stopRecording();
            unbindService(connection);
        }
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                "/VoiceMemos/voicememo" + getString(R.string.deafult_extension)).delete();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        recordButton.setBackgroundResource(backgroundImage);
        SharedPreferences sharedPreferences = getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0);
        isDialogShowed = sharedPreferences.getBoolean(PREFS_SHOW_DIALOG, false);
        if (isDialogShowed)
            showAlertDialog();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, RecordService.class));
        backgroundImage = R.drawable.record_icon;
        setContentView(R.layout.a_record_memos);
        recordButton = (ImageButton) findViewById(R.id.record_button);
        recordButton.setOnClickListener(this);
        countDownTextView = (TextView) findViewById(R.id.time_countdown_text_view);
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(VoiceMemosActivity.ACTION_SAVE_MEMO)) {
                    finish();
                }
                if (intent.getAction().equals(VoiceMemosActivity.ACTION_BACK_TO_MAIN)) {
                    finish();
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VoiceMemosActivity.ACTION_SAVE_MEMO);
        intentFilter.addAction(VoiceMemosActivity.ACTION_BACK_TO_MAIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onClick(View v) {

        if (recordService.isRecorded()) {

            recordButton.setBackgroundResource(R.drawable.record_icon);
            backgroundImage = R.drawable.record_icon;
            recordService.stopRecording();
            showAlertDialog();
            isDialogShowed = true;
            getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit().putBoolean(PREFS_SHOW_DIALOG, true)
                    .commit();

        } else {

            recordButton.setBackgroundResource(R.drawable.stop_record_icon);
            backgroundImage = R.drawable.stop_record_icon;
            recordService.startRecording();
            timer = new CountDownTimer(11000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "onTick()");
                    countDownTextView.setVisibility(TextView.VISIBLE);
                    countDownTextView.setText((millisUntilFinished - 1) / 1000 + " "
                            + getString(R.string.remainig_time));
                    recordButton.setBackgroundResource(R.drawable.stop_record_icon);
                    backgroundImage = R.drawable.stop_record_icon;
                }

                @Override
                public void onFinish() {
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "recorder stopped onFinish()");
                    recordService.stopRecording();
                    recordButton.setBackgroundResource(R.drawable.record_icon);
                    backgroundImage = R.drawable.record_icon;
                    showAlertDialog();
                    getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                            .putBoolean(PREFS_SHOW_DIALOG, true).commit();
                    isDialogShowed = true;

                }
            }.start();

        }

    }

    public void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.save_label));

        alertDialog.setMessage(getString(R.string.dialog_label));
        final EditText memoName = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        memoName.setLayoutParams(lp);
        alertDialog.setView(memoName);
        SharedPreferences sharedPreferences = getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0);
        int index = sharedPreferences.getInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, 1);
        formatedIndex = "";
        if (index < 10) {
            formatedIndex = "00" + index;
        } else if (index < 100) {
            formatedIndex = "0" + index;
        }

        memoName.setText(getString(R.string.default_memo_name) + " " + formatedIndex);

        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0);
                int index = sharedPreferences.getInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, 1);
                Editor preferencesEditor = sharedPreferences.edit();
                if (memoName.getText().toString().equals(getString(R.string.default_memo_name) + " " + formatedIndex))
                    index++;
                preferencesEditor.putInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, index);
                saveMemoIntent = new Intent(VoiceMemosActivity.ACTION_SAVE_MEMO);
                saveMemoIntent.putExtra(EXTRA_NAME, memoName.getText().toString());
                File voiceMemoFile = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                        "/VoiceMemos/voicememo" + getString(R.string.deafult_extension));
                File renaimedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .getPath()
                        + "/VoiceMemos/"
                        + memoName.getText().toString()
                        + getString(R.string.deafult_extension));
                if (!memoName.getText().toString().equals(getString(R.string.default_memo_name) + " " + formatedIndex))
                    renaimedFile = setPostfix(renaimedFile);
                if (voiceMemoFile.exists()) {
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, voiceMemoFile.getPath() + " exist");
                } else
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, voiceMemoFile.getName() + " lost");
                Log.d(MEMOS_RECORD_ACTIVITY_TAG,
                        "Try rename " + voiceMemoFile.getPath() + " to " + renaimedFile.getPath());
                boolean succes = voiceMemoFile.renameTo(renaimedFile);

                if (succes)
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "renaming succes");
                else
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "renaming failed");
                preferencesEditor.commit();
                getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                        .putBoolean(PREFS_SHOW_DIALOG, false).commit();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(saveMemoIntent);

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                        .putBoolean(PREFS_SHOW_DIALOG, false).commit();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                        new Intent(VoiceMemosActivity.ACTION_BACK_TO_MAIN));
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                        "/VoiceMemos/voicememo" + getString(R.string.deafult_extension)).delete();
                dialog.cancel();

            }
        });
        alertDialog.show();

    }

    private String getPostfix(int postfix) {
        String formattedPostfix;
        if (postfix < 10)
            formattedPostfix = "00" + postfix;
        else if (postfix < 100)
            formattedPostfix = "0" + postfix;
        else
            formattedPostfix = "" + postfix;
        return formattedPostfix;
    }

    private File setPostfix(File file) {
        int postfix = 1;
        File renaimed = file;
        while (file.exists()) {
            String name = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
            if (file.getPath().lastIndexOf(' ') > 0)
                name = file.getPath().substring(0, file.getPath().lastIndexOf(' '));
            renaimed = new File(name + " " + getPostfix(postfix) + getString(R.string.deafult_extension));
            file = renaimed;
            Log.d(MEMOS_RECORD_ACTIVITY_TAG, file.getPath());
            postfix++;
        }
        file.renameTo(renaimed);
        return file;
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBinded = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecordBinder binder = (RecordBinder) service;
            recordService = binder.getService();
            if (recordService.isRecorded()) {
                countDownTextView.setVisibility(TextView.VISIBLE);
                backgroundImage = R.drawable.stop_record_icon;
            }
            isBinded = true;

        }
    };

}
