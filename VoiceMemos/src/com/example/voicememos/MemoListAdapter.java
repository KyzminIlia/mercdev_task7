package com.example.voicememos;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class MemoListAdapter extends ArrayAdapter<String> implements ListAdapter {
    private List<String> dateTimeList;
    private HashSet<String> dateTime;
    private Context mContext;
    private String[] fileList;

    public MemoListAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        fileList = getVoiceMemosDirectory().list();
        if (fileList == null) {
            ((Activity) context).getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                    .remove(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME);
        }

    }

    @Override
    public int getCount() {
        if (fileList != null)
            return fileList.length;
        else
            return 0;

    }

    @Override
    public String getItem(int position) {
        return getVoiceMemosDirectory() + "/" + fileList[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = fileList[position];
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.i_memo, null);
        }
        TextView memoName = (TextView) convertView.findViewById(R.id.memo_name_list);
        TextView dataTimeLabel = (TextView) convertView.findViewById(R.id.data_time_memo_label);
        Calendar date = Calendar.getInstance();
        File currentFile = new File(getVoiceMemosDirectory() + "/" + name);

        memoName.setText(name);
        dataTimeLabel.setText(getFileDataAndTime(currentFile));

        return convertView;
    }

    public void addDateTime() {

    }

    private File getVoiceMemosDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),
                "/VoiceMemos/");
    }

    private void fromSetToList() {
        Iterator iterator = dateTime.iterator();
        dateTimeList = new ArrayList<String>();
        while (iterator.hasNext()) {
            dateTimeList.add((String) iterator.next());
        }
    }

    private String getFileDataAndTime(File file) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(file.lastModified());
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
}
