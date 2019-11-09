package com.example.player.MyDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.player.Modal.Constant;
import com.example.player.R;

import java.io.File;

public class RenameDialog extends AppCompatDialogFragment {

    EditText renameText;
    int position;
    int type;
    String videoTitleWithoutExtension;
    String videoExtension = "";
    String fullTitle;
    int lastPeriodPosition;
    File file;

    DialogsListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //getActivity() in a Fragment returns the Activity the Fragment is currently associated with
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_dialog_layout, null);

        builder.setView(view)
                .setTitle("Rename")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String changeTitle = renameText.getText().toString().trim();
                        mListener.renameFile(file, fullTitle, changeTitle + videoExtension);
                    }
                });

        renameText = view.findViewById(R.id.rename_title);
        position = getArguments().getInt("position");
        type = getArguments().getInt("Type");  // O mean: call from OpenFolderAdapter and 1 mean: call from RecyclerViewAdapter
        if (type == 0){
            file = Constant.currentFolderFiles.get(position).getFile().getParentFile();
            fullTitle = Constant.currentFolderFiles.get(position).getFile().getName();
            lastPeriodPosition = fullTitle.lastIndexOf(".");
            videoTitleWithoutExtension = fullTitle.substring(0,lastPeriodPosition);
            videoExtension = fullTitle.substring(lastPeriodPosition);
            renameText.setText(videoTitleWithoutExtension);
        }
        else if (type == 1) {
            if (Constant.SORT_TYPE == 1) {
                file = Constant.directoryList.get(position).getParentFile();
                fullTitle = Constant.directoryList.get(position).getName();
                renameText.setText(fullTitle);
            }else if (Constant.SORT_TYPE == 2){
                file = Constant.allMemoryVideoList.get(position).getFile().getParentFile();
                fullTitle = Constant.allMemoryVideoList.get(position).getFile().getName();
                lastPeriodPosition = fullTitle.lastIndexOf(".");
                videoTitleWithoutExtension = fullTitle.substring(0,lastPeriodPosition);
                videoExtension = fullTitle.substring(lastPeriodPosition);
                renameText.setText(videoTitleWithoutExtension);
            }
        }


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mListener = (DialogsListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" must implement DialogsListener");
        }

    }

}
