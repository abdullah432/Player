package com.example.player;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.player.Modal.Constant;
import com.example.player.Modal.FilesInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpenFolderAdapter extends RecyclerView.Adapter<OpenFolderAdapter.FileLayoutHolder> {

    private ArrayList<FilesInfo> directoryFileList;
    private OnFileListener mFolderItemListener;
    private Context mContext;
    private SparseBooleanArray selected_items;
    private boolean select_all = true;
    private String filePath;
    private int lastIndex;
    private String filetitle;

    OpenFolderAdapter(ArrayList<FilesInfo> directoryFileList, Context mContext, OnFileListener mFolderItemListener){
        this.directoryFileList = directoryFileList;
        this.mContext = mContext;
        this.mFolderItemListener = mFolderItemListener;
        selected_items = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public FileLayoutHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_sort_list, viewGroup, false);
        return new FileLayoutHolder(view, mFolderItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileLayoutHolder fileLayoutHolder, int pos) {
        final FilesInfo file = directoryFileList.get(pos);

        filePath = file.getFile().getName();
        lastIndex = filePath.lastIndexOf(".");
        filetitle = filePath.substring(0, lastIndex);
        fileLayoutHolder.video_title.setText(filetitle);

        if (file.getVideoDuration() != null && !file.getVideoDuration().equals("Information not found.")) {
            fileLayoutHolder.durationTxtOverThumnail.setVisibility(View.VISIBLE);
            fileLayoutHolder.durationTxtOverThumnail.setText(file.getVideoDuration());
        } else
            fileLayoutHolder.durationTxtOverThumnail.setVisibility(View.GONE);

        //Glide increase the image loading process
        Uri uri = Uri.fromFile(new File(file.getFile().getPath()));

        Glide.with(mContext)
                .applyDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.color.blackColor)
                        .error(R.color.blackColor))
                .load(uri).thumbnail(0.1f).into(fileLayoutHolder.thumbnail_imageview);

        //first we will check user want the playback position feature
        //If not then we will not fill the playback_position bar
        if (Constant.playbackState == Constant.playbackRecord.YES) {
            /*First check video already played or not. If not they visible NEW view
             * else load playback position and calculate percentage of it and assign it*/

            if (file.getState() == FilesInfo.fileState.NEW) {
                fileLayoutHolder.playback_position.setVisibility(View.GONE);
                fileLayoutHolder.newLabel.setVisibility(View.VISIBLE);

            } else {
                fileLayoutHolder.newLabel.setVisibility(View.GONE);
                fileLayoutHolder.playback_position.setVisibility(View.VISIBLE);
                fileLayoutHolder.playback_position.setProgress((int) file.getPlaybackPercentage());
            }
        } else {
            fileLayoutHolder.playback_position.setVisibility(View.GONE);

            if (file.getState() == FilesInfo.fileState.NEW)
                fileLayoutHolder.newLabel.setVisibility(View.VISIBLE);
            else
                fileLayoutHolder.newLabel.setVisibility(View.GONE);
        }

        fileLayoutHolder.parentLayout.setSelected(selected_items.get(pos, false));
    }

    @Override
    public int getItemCount()
    {
        return directoryFileList.size();
    }


    class FileLayoutHolder extends RecyclerView.ViewHolder{
        FrameLayout thumbnail;
        ImageView thumbnail_imageview;
        ContentLoadingProgressBar playback_position;
        TextView newLabel;
        TextView durationTxtOverThumnail;
        TextView video_title;
        View icon_more;
        View parentLayout;

        FileLayoutHolder(@NonNull View itemView, final OnFileListener onFileListener) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            thumbnail_imageview = itemView.findViewById(R.id.thumbnail_imgView);
            playback_position = itemView.findViewById(R.id.playback_position);
            newLabel = itemView.findViewById(R.id.new_label);
            durationTxtOverThumnail = itemView.findViewById(R.id.durationTxtOverThumnail);
            video_title = itemView.findViewById(R.id.video_title);
            icon_more = itemView.findViewById(R.id.video_icon_more);
            parentLayout = itemView.findViewById(R.id.file_parent_layout);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                thumbnail.setClipToOutline(true);
            }

            icon_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFileListener.onIconMoreClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onFileListener != null){
                        if(getAdapterPosition() != RecyclerView.NO_POSITION){
                            return onFileListener.onVideoLongClick(getAdapterPosition());
                        }
                    }
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onFileListener != null){
                        if(getAdapterPosition() != RecyclerView.NO_POSITION){
                            onFileListener.onVideoClick(getAdapterPosition());
                        }
                    }
                }
            });

            icon_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onFileListener != null){
                        if(getAdapterPosition() != RecyclerView.NO_POSITION){
                            onFileListener.onIconMoreClick(getAdapterPosition());
                        }
                    }
                }
            });
        }

    }

    public void toggleSelection(int position){
        if (selected_items.get(position,false))
            selected_items.delete(position);
        else
            selected_items.put(position, true);

        notifyItemChanged(position);
    }

    public int getSelectedItemCount(){
        return selected_items.size();
    }

    public void clearSelectedItem(){
        selected_items.clear();
        notifyDataSetChanged();
    }

    public void selectAllItem(){
        if (select_all) {
            for (int i = 0; i < Constant.currentFolderFiles.size(); i++) {
                selected_items.put(i, true);
            }
            select_all = false;
        }
        else {
            selected_items.clear();
            select_all = true;
        }
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    public void Refresh(ArrayList<FilesInfo> fileList){
        directoryFileList = fileList;
        notifyDataSetChanged();
    }

}
