package com.example.voicememos;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class SaveVoiceMemoDialog extends DialogFragment implements OnClickListener {
    public static final String DIALOG_TAG = SaveVoiceMemoDialog.class.getSimpleName();
    public static final String EXTRA_NAME = "VoiceMemoName";

    private Button saveButton;
    private Button cancelButton;
    private Intent saveMemoIntent;

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                dismiss();
                break;
            case R.id.save_button:
                saveMemoIntent = new Intent(VoiceMemosActivity.ACTION_SAVE_MEMO);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(saveMemoIntent);
                dismiss();
                break;
        }

    }
}
