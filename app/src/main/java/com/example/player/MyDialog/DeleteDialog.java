package com.example.player.MyDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DeleteDialog extends AppCompatDialogFragment {

    DialogsListener mListener;
    String message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        int msg = getArguments().getInt("msg");
        switch (msg){
            case 0:
                message = "This file will be deleted permanently.";
                break;
            case 1:
                message = "This folder will be deleted permanently.";
                break;
            case 2:
                message = "These folders will be deleted permanently.";
                break;
            case 3:
                message = "These files will be deleted permanently.";
                break;
                default:
                    message = "This file will be deleted permanently.";
        }

        builder.setTitle("Delete")
                .setMessage(message)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.deleteFile();
                    }
                });

        return builder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DialogsListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement DialogsListener Interface");
        }

    }
}
