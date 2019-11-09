package com.example.player.MyDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.player.Modal.Constant;
import com.example.player.Modal.Methods;
import com.example.player.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InformationDialog extends AppCompatDialogFragment {

    TextView IFileNameTxt;
    TextView IFileLocationTxt;
    TextView IFileLengthTxt;
    TextView IFileSizeTxt;
    TextView IFileResolutionTxt;
    TextView IFileFormatTxt;
    TextView IFileDateTxt;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.information_dialog_layout,null);

        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        IFileNameTxt = view.findViewById(R.id.IFileNameTxt);
        IFileLocationTxt = view.findViewById(R.id.IFileLocationTxt);
        IFileLengthTxt = view.findViewById(R.id.IFileLengthTxt);
        IFileSizeTxt = view.findViewById(R.id.IFileSizeTxt);
        IFileFormatTxt = view.findViewById(R.id.IFileFormatTxt);
        IFileResolutionTxt = view.findViewById(R.id.IFileResolutionTxt);
        IFileDateTxt = view.findViewById(R.id.IFileDateTxt);

        int videoPosition = getArguments().getInt("videoPosition");
        File selectedFile;

        if (Constant.SORT_TYPE == 1)
            selectedFile = new File(Constant.currentFolderFiles.get(videoPosition).getFile().getPath());
        else
            selectedFile = new File(Constant.allMemoryVideoList.get(videoPosition).getFile().getPath());

        IFileNameTxt.setText(selectedFile.getName());
        IFileLocationTxt.setText(selectedFile.getParent());
        IFileSizeTxt.setText(Methods.getFolderSizeLabel(selectedFile));
        IFileLengthTxt.setText(Methods.getVideoDuration(selectedFile));
        IFileResolutionTxt.setText(Methods.getVideoResolution(selectedFile));
        IFileDateTxt.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date(selectedFile.lastModified())));
        IFileFormatTxt.setText(Methods.getVideoFormat(selectedFile));

        return builder.create();

    }
}
