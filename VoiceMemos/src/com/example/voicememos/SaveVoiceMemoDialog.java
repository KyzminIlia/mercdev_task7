package com.example.voicememos;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SaveVoiceMemoDialog extends DialogFragment implements OnClickListener {
    public static final String DIALOG_TAG = SaveVoiceMemoDialog.class.getSimpleName();

    public static final String EXTRA_NAME = "VoiceMemoName";
    public static final String PREFS_DATE_TIME = "DateTime";

    private Button saveButton;
    private Button cancelButton;
    private Intent saveMemoIntent;
    private EditText memoName;
    private String formatedIndex;

    private Set<String> listDateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.d_save_memos, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        saveButton = (Button) view.findViewById(R.id.save_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        memoName = (EditText) view.findViewById(R.id.name_of_voice_memo_edit);
        SharedPreferences sharedPreferences = getActivity().getPreferences(0);
        int index = sharedPreferences.getInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, 1);
        formatedIndex = "";
        if (index < 10) {
            formatedIndex = "00" + index;
        } else if (index < 100) {
            formatedIndex = "0" + index;
        }

        memoName.setText(getString(R.string.default_memo_name) + " " + formatedIndex);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(VoiceMemosActivity.ACTION_BACK_TO_MAIN));
                dismiss();
                break;
            case R.id.save_button:
                SharedPreferences sharedPreferences = getActivity().getPreferences(0);
                int index = sharedPreferences.getInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, 1);
                Editor preferencesEditor = sharedPreferences.edit();
                if (memoName.getText().toString().equals(getString(R.string.default_memo_name) + " " + formatedIndex))
                    index++;
                preferencesEditor.putInt(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME, index);
                saveMemoIntent = new Intent(VoiceMemosActivity.ACTION_SAVE_MEMO);
                saveMemoIntent.putExtra(EXTRA_NAME, memoName.getText().toString());
                File voiceMemoFile = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                        "/VoiceMemos/voicememo.3gp");
                File renaimedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .getPath() + "/VoiceMemos/" + memoName.getText().toString() + ".3gp");
                if (!memoName.getText().toString().equals(getString(R.string.default_memo_name) + " " + formatedIndex))
                    renaimedFile = setPostfix(renaimedFile);
                if (voiceMemoFile.exists()) {
                    Log.d(DIALOG_TAG, voiceMemoFile.getPath() + " exist");
                } else
                    Log.d(DIALOG_TAG, voiceMemoFile.getName() + " lost");
                Log.d(DIALOG_TAG, "Try rename " + voiceMemoFile.getPath() + " to "
                        + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()
                        + "/VoiceMemos/" + memoName.getText().toString() + ".3gp");
                boolean succes = voiceMemoFile.renameTo(renaimedFile);

                if (succes)
                    Log.d(DIALOG_TAG, "renaming succes");
                else
                    Log.d(DIALOG_TAG, "renaming failed");

                listDateTime = sharedPreferences.getStringSet(PREFS_DATE_TIME, new HashSet<String>());
                listDateTime.add(getCurrentDataAndTime());
                preferencesEditor.putStringSet(PREFS_DATE_TIME, listDateTime);
                preferencesEditor.commit();
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(saveMemoIntent);
                dismiss();
                break;
        }

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
            renaimed = new File(name + " " + getPostfix(postfix) + ".3gp");
            file = renaimed;
            Log.d(DIALOG_TAG, file.getPath());
            postfix++;
        }
        file.renameTo(renaimed);
        return file;
    }
}
