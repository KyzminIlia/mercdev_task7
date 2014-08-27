package com.example.voicememos;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class VoiceMemosActivity extends FragmentActivity {
    public static final String ACTIVITY_TAG = VoiceMemosActivity.class.getSimpleName();
    public static final String ACTION_SAVE_MEMO = "SAVE_MEMO";

    private boolean isInRecordingFragment = false;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        if (getCurrentFragment() == null) {
            VoiceMemosFragment currentFragment = new VoiceMemosFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, currentFragment, VoiceMemosFragment.MEMOS_FRAGMENT_TAG).commit();
            Log.d(ACTIVITY_TAG, "current fragment: " + currentFragment.getTag());
        } else {
            Fragment currentFragment = getCurrentFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, currentFragment, currentFragment.getTag()).commit();
            Log.d(ACTIVITY_TAG, "current fragment: " + currentFragment.getTag());
        }
        receiver  = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
              Log.d(ACTIVITY_TAG,"Receive message");
                
            }
        };
        IntentFilter intentFilter = new IntentFilter(ACTION_SAVE_MEMO);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    private Fragment getCurrentFragment() {
        Fragment current = null;
        if (getFragmentTag() != null)
            current = (Fragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag());
        return current;
    }

    private String getFragmentTag() {
        String currentFragmentTag = null;
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (getSupportFragmentManager().getFragments() != null)
            currentFragmentTag = getSupportFragmentManager().getFragments()
                    .get(getSupportFragmentManager().getFragments().size() - 1).getTag();
        return currentFragmentTag;
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
                    replaceToRecordFragment();
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

    private void replaceToRecordFragment() {
        VoiceMemosRecordFragment replaceFragment = new VoiceMemosRecordFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, replaceFragment, VoiceMemosRecordFragment.MEMOS_RECORD_FRAGMENT_TAG)
                .addToBackStack(null).commit();
        Log.d(ACTIVITY_TAG, "fragment replaced to:  " + replaceFragment.getTag());
    }
}