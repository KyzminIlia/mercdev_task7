package com.example.voicememos;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private boolean isRecord = false;
    private BroadcastReceiver receiver;
    private String formatedIndex;
    private Intent saveMemoIntent;
    private Set<String> listDateTime;
    private boolean isDialogShowed = false;

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_STOP_RECORD));
        stopService(serviceIntent);
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

        serviceIntent = new Intent(this, RecordService.class);
        serviceIntent.setAction(ACTION_START_RECORD);
        backgroundImage = R.drawable.record_icon;
        setContentView(R.layout.a_record_memos);
        recordButton = (ImageButton) findViewById(R.id.record_button);
        recordButton.setOnClickListener(this);
        countDownTextView = (TextView) findViewById(R.id.time_countdown_text_view);
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(VoiceMemosActivity.ACTION_SAVE_MEMO) && !isFinishing()) {
                    finish();
                }
                if (intent.getAction().equals(VoiceMemosActivity.ACTION_BACK_TO_MAIN) && !isFinishing()) {
                    finish();
                }
                if (intent.getAction().equals(RecordService.ACTION_UPDATE_TIME) && !isFinishing()) {
                    countDownTextView.setVisibility(TextView.VISIBLE);
                    countDownTextView.setText(intent.getLongExtra(RecordService.EXTRA_TIME, 0) + " "
                            + getString(R.string.remainig_time));
                    recordButton.setBackgroundResource(R.drawable.stop_record_icon);
                    backgroundImage = R.drawable.stop_record_icon;
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "Receive message onTick()");
                    stop = true;
                }
                if (intent.getAction().equals(RecordService.ACTION_FINISH) && !isFinishing()) {
                    recordButton.setBackgroundResource(R.drawable.record_icon);
                    backgroundImage = R.drawable.record_icon;
                    showAlertDialog();
                    getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                            .putBoolean(PREFS_SHOW_DIALOG, true).commit();
                    isDialogShowed = true;
                    stop = false;
                    Log.d(MEMOS_RECORD_ACTIVITY_TAG, "Receive message FINISH");
                    stopService(serviceIntent);
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(VoiceMemosActivity.ACTION_SAVE_MEMO);
        intentFilter.addAction(VoiceMemosActivity.ACTION_BACK_TO_MAIN);
        intentFilter.addAction(RecordService.ACTION_UPDATE_TIME);
        intentFilter.addAction(RecordService.ACTION_FINISH);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, intentFilter);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RecordService.class.getName().equals(service.service.getClassName())) {
                countDownTextView.setVisibility(TextView.VISIBLE);
                backgroundImage = R.drawable.stop_record_icon;
                stop = true;
            }

        }
    }

    boolean stop = false;

    @Override
    public void onClick(View v) {

        if (stop) {
            stop = false;
            recordButton.setBackgroundResource(R.drawable.record_icon);
            backgroundImage = R.drawable.record_icon;
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_STOP_RECORD));
            showAlertDialog();
            isDialogShowed = true;
            getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit().putBoolean(PREFS_SHOW_DIALOG, true)
                    .commit();
            stopService(serviceIntent);

        } else {
            stop = true;
            recordButton.setBackgroundResource(R.drawable.stop_record_icon);
            backgroundImage = R.drawable.stop_record_icon;
            startService(serviceIntent);

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

                listDateTime = sharedPreferences.getStringSet(PREFS_DATE_TIME, new HashSet<String>());
                listDateTime.add(getCurrentDataAndTime());
                preferencesEditor.putStringSet(PREFS_DATE_TIME, listDateTime);
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

    private String getCurrentDataAndTime() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String formatedDay;
        String formatedMonth;
        String formatedMinute;
        String formatedHour;
        String formatedSecond;

        if (day < 10)
            formatedDay = "0" + day;
        else
            formatedDay = "" + day;

        if (month < 10)
            formatedMonth = "0" + month;
        else
            formatedMonth = "" + month;

        if (hour < 10)
            formatedHour = "0" + hour;
        else
            formatedHour = "" + hour;

        if (second < 10)
            formatedSecond = "0" + second;
        else
            formatedSecond = "" + second;

        if (minute < 10)
            formatedMinute = "0" + minute;
        else
            formatedMinute = "" + minute;

        String time = formatedHour + ":" + formatedMinute + ":" + formatedSecond;
        String date = formatedDay + "/" + formatedMonth + "/" + year;
        String dateTime = date + "\n" + time;
        return dateTime;
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

}
