package com.example.voicememos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class VoiceMemosFragment extends Fragment implements OnItemClickListener {
    public final static String MEMOS_FRAGMENT_TAG = VoiceMemosFragment.class.getSimpleName();

    private ListView memoList;

    private BroadcastReceiver receiver;

    private MemoListAdapter listAdapter;

    private MediaPlayer mediaPlayer;

    private int prevPosition;
    private boolean isPlaying;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

    }

    private void startPlaying(String mFileName) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mFileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.d(MEMOS_FRAGMENT_TAG, "prepare() failed");
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        isPlaying = false;
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.f_memos, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        memoList = (ListView) view.findViewById(R.id.memos_list);
        memoList.setEmptyView(view.findViewById(android.R.id.empty));
        listAdapter = new MemoListAdapter(getActivity(), R.layout.list_item);
        memoList.setAdapter(listAdapter);
        memoList.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
        if (isPlaying)
            stopPlaying();
        startPlaying(listAdapter.getItem(position));
    }
}
