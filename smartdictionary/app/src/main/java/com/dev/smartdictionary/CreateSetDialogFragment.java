package com.dev.smartdictionary;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class CreateSetDialogFragment extends DialogFragment {

    private static final String EXTRA_NAME = "com.dev.smartdictionary.setname";
    private EditText SetName;

    public interface OkListener{
        void dialogResult(String name);
    }
    private  OkListener listener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.listener = (OkListener) getParentFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_create_set_dialog, null);
        SetName = (EditText) v.findViewById(R.id.set_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle(R.string.SetDialogTitle)
                .setIcon(R.drawable.add_set_icon)
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String Name = SetName.getText().toString();
                        listener.dialogResult(Name);
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();

    }
}