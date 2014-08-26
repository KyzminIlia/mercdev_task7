package com.example.voicememos;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class VoiceMemosActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle saveInstance) {
        if (getCurrentFragment() == null) {
            VoiceMemosFragment currentFragment = new VoiceMemosFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, currentFragment, VoiceMemosFragment.MEMOS_FRAGMENT_TAG).commit();
        }
        super.onCreate(saveInstance);
    }

    private Fragment getCurrentFragment() {
        Fragment current = null;
        if (getFragmentTag() != null)
            current = (Fragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag());
        return null;
    }

    private String getFragmentTag() {
        return getSupportFragmentManager().getFragments().get(0).getTag();
    }
}