package com.example.player.MyDialog;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.player.Modal.Constant;
import com.example.player.R;


public class VideoOptionBottomSheetDialog extends BottomSheetDialogFragment {

    TextView bottomSheetTitleID;
    LinearLayout shareNowLayoutID;
    LinearLayout renameLayoutID;
    LinearLayout propertyLayoutID;
    LinearLayout deleteLayoutID;
    //Position of video on recycler view
    int position;

    BottomSheetListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.icmore_bottom_sheet, container, false);

        bottomSheetTitleID = view.findViewById(R.id.bsTitleID);
        shareNowLayoutID = view.findViewById(R.id.video_share_option);
        renameLayoutID = view.findViewById(R.id.video_rename_option);
        propertyLayoutID = view.findViewById(R.id.video_properties_option);
        deleteLayoutID = view.findViewById(R.id.video_delete_option);

        setBottomSheetTitle();

        shareNowLayoutID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openShareDialog();
                dismiss();
            }
        });

        renameLayoutID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openRenameDialog();
                dismiss();
            }
        });

        propertyLayoutID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openInformationDialog();
                dismiss();
            }
        });

        deleteLayoutID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openDeleteDialog();
                dismiss();
            }
        });

        return view;
    }

    private void setBottomSheetTitle() {
        if (Constant.SORT_TYPE == 1)
            bottomSheetTitleID.setText(Constant.currentFolderFiles.get(position).getFile().getName());
        else
            bottomSheetTitleID.setText(Constant.allMemoryVideoList.get(position).getFile().getName());
    }

    public void setPosition(int position){
        this.position = position;
    }

    public interface BottomSheetListener
    {
        void openShareDialog();
        void openRenameDialog();
        void openInformationDialog();
        void openDeleteDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mListener = (BottomSheetListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }

    }
}
