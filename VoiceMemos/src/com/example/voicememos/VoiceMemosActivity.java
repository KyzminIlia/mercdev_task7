package com.example.voicememos;

import java.io.IOException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class VoiceMemosActivity extends FragmentActivity implements OnItemClickListener {

    public static final String ACTIVITY_TAG = VoiceMemosActivity.class.getSimpleName();

    public static final String ACTION_SAVE_MEMO = "SAVE_MEMO";
    public static final String ACTION_BACK_TO_MAIN = "BACK_TO_MAIN";

    public static final String PREFS_LAST_INDEX_DEFAULT_NAME = "LAST_INDEX";
    public static final String PREFS_DEFAULT_INDEX = "DEFAULT_INDEX";

    private boolean isInRecordingFragment = false;

    private BroadcastReceiver receiver;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private ListView memoList;

    private MemoListAdapter listAdapter;

    private int prevPosition = -1;

    private void startPlaying(String mFileName) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mFileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.d(ACTIVITY_TAG, "prepare() failed");
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        isPlaying = false;
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    protected void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        Log.d(ACTIVITY_TAG, "onCreate() activity with hash " + hashCode());

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_SAVE_MEMO) && !isFinishing()) {
                    isInRecordingFragment = false;
                    memoList.invalidateViews();
                    Log.d(ACTIVITY_TAG, "Receive message SAVE MEMO");

                }
                if (intent.getAction().equals(ACTION_BACK_TO_MAIN) && !isFinishing()) {
                    isInRecordingFragment = false;
                    Log.d(ACTIVITY_TAG, "Receive message BACK TO MAIN");

                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(ACTION_SAVE_MEMO);
        intentFilter.addAction(ACTION_BACK_TO_MAIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
        setContentView(R.layout.a_memos);
        memoList = (ListView) findViewById(R.id.memos_list);
        memoList.setEmptyView(findViewById(android.R.id.empty));
        listAdapter = new MemoListAdapter(this, R.layout.list_item);
        memoList.setAdapter(listAdapter);
        memoList.setOnItemClickListener(this);
        memoList.invalidateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_memos:
                if (!isInRecordingFragment) {
                    isInRecordingFragment = true;
                    startActivity(new Intent(this, VoiceMemosRecordActivity.class));
                } else
                    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (isInRecordingFragment)
            isInRecordingFragment = false;
        super.onBackPressed();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(ACTIVITY_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
        if (isPlaying)
            stopPlaying();
        if (prevPosition != position) {
            startPlaying(listAdapter.getItem(position));
            prevPosition = position;
        } else
            prevPosition = -1;
    }

}