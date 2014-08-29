package com.example.voicememos;

import java.io.File;
import java.util.ArrayList;
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
        dateTime = (HashSet<String>) ((Activity) context).getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX,
                0).getStringSet(VoiceMemosRecordActivity.PREFS_DATE_TIME, null);
        if (dateTime != null) {
            fileList = getVoiceMemosDirectory().list();
            if (fileList != null)
                fromSetToList();
            else {
                ((Activity) mContext).getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                        .remove(VoiceMemosRecordActivity.PREFS_DATE_TIME)
                        .remove(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME).commit();

            }
        } else {
            ((Activity) mContext).getSharedPreferences(VoiceMemosActivity.PREFS_DEFAULT_INDEX, 0).edit()
                    .remove(VoiceMemosRecordActivity.PREFS_DATE_TIME)
                    .remove(VoiceMemosActivity.PREFS_LAST_INDEX_DEFAULT_NAME).commit();
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
        }
        TextView memoName = (TextView) convertView.findViewById(R.id.memo_name_list);
        TextView dataTimeLabel = (TextView) convertView.findViewById(R.id.data_time_memo_label);

        memoName.setText(name);
        dataTimeLabel.setText(dateTimeList.get(position));

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
}
