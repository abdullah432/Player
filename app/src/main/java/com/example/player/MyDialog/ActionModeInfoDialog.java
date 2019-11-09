package com.example.player.MyDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.player.R;

public class ActionModeInfoDialog extends AppCompatDialogFragment {

    TextView contains;
    TextView totalSize;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.actionmode_info_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Information")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        contains = view.findViewById(R.id.containsID);
        totalSize = view.findViewById(R.id.totalSizeID);

        String contain = getArguments().getString("Contains");
        String size = getArguments().getString("TotalSize");

        contains.setText(contain);
        totalSize.setText(size);

        return builder.create();

    }
}
