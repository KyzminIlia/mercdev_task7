package com.example.voicememos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class VoiceMemosActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle saveInstance) {

        super.onCreate(saveInstance);
    }

    private Fragment getCurrentFragment() {
        return (Fragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag());
    }

    private String getFragmentTag() {
        return getSupportFragmentManager().getFragments().get(0).getTag();
    }
}