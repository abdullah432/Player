package com.example.player;

import android.content.Context;
import android.media.MediaMetadataRetriever;
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
import com.example.player.Modal.Methods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter {

    private ArrayList<FilesInfo> videosList;
    private ArrayList<File> directoryList;
    private OnRecyclerViewListener onRecyclerViewListener;
    private Context mContext;
    SparseBooleanArray selectedItem = new SparseBooleanArray();
    private boolean select_all = false;

    private String filePath;
    private int lastIndex;
    private String filetitle;

    //interface for handling recycler view click
    public interface OnRecyclerViewListener {
        void onClick(int position);
        boolean onLongClick(int position);
        void onIconMoreClick(int position);
    }

    RecyclerViewAdapter(Context mContext, OnRecyclerViewListener onRecyclerViewListener) {
        //Retrieving data from Constant class because it contains updated data
        this.videosList = Constant.allMemoryVideoList;
        this.directoryList = Constant.directoryList;
        this.mContext = mContext;
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    // 1 mean Sort Type is Folder and 2 mean Sort Type is File
    @Override
    public int getItemViewType(int position) {
        if(Constant.SORT_TYPE == 1)
            return 1;
        else if (Constant.SORT_TYPE == 2)
            return 2;
        else
            return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType)
        {
            case 1:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder_sort_list, viewGroup, false);
                return new FolderLayoutHolder(view, onRecyclerViewListener);
            case 2:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_sort_list, viewGroup, false);
                return new FileLayoutHolder(view, onRecyclerViewListener);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (Constant.SORT_TYPE)
        {
            case 1:
                ((FolderLayoutHolder)viewHolder).folderTitle.setText(directoryList.get(position).getName());
                int noMediaFile = Methods.ReadDirectory(directoryList.get(position));
                if (noMediaFile > 1)
                    ((FolderLayoutHolder)viewHolder).nVideosTxt.setText( noMediaFile + " media files"); //Todo:: get data from class
                else
                    ((FolderLayoutHolder)viewHolder).nVideosTxt.setText( noMediaFile + " media file");

                ((FolderLayoutHolder)viewHolder).parentLayout.setSelected(selectedItem.get(position,false));
                ((FolderLayoutHolder)viewHolder).icon_folder.setSelected(selectedItem.get(position,false));
                break;
            case 2:
                final FilesInfo file = videosList.get(position);
                filePath = file.getFile().getName();
                lastIndex = filePath.lastIndexOf(".");
                filetitle = filePath.substring(0,lastIndex);
                ((FileLayoutHolder)viewHolder).video_title.setText(filetitle);

                if (file.getVideoDuration() != null && !file.getVideoDuration().equals("Information not found.")){
                    ((FileLayoutHolder)viewHolder).durationTxtOverThumnail.setVisibility(View.VISIBLE);
                    ((FileLayoutHolder)viewHolder).durationTxtOverThumnail.setText(file.getVideoDuration());
                }
                else
                    ((FileLayoutHolder)viewHolder).durationTxtOverThumnail.setVisibility(View.GONE);

                //Glide increase the image loading process
                Uri uri = Uri.fromFile(new File(file.getFile().getPath()));

                Glide.with(mContext)
                        .applyDefaultRequestOptions(new RequestOptions()
                                .placeholder(R.color.blackColor)
                                .error(R.color.blackColor))
                        .load(uri).thumbnail(0.1f).into(((FileLayoutHolder)viewHolder).thumbnail_imageview);

                //first we will check user want the playback position feature
                //If not then we will not fill the playback_position bar
                if (Constant.playbackState == Constant.playbackRecord.YES){
                    /*First check video already played or not. If not they visible NEW view
                     * else load playback position and calculate percentage of it and assign it*/

                    if (file.getState() == FilesInfo.fileState.NEW) {
                        ((FileLayoutHolder)viewHolder).playback_position.setVisibility(View.GONE);
                        ((FileLayoutHolder)viewHolder).newLabel.setVisibility(View.VISIBLE);

                    } else {
                        ((FileLayoutHolder)viewHolder).newLabel.setVisibility(View.GONE);
                        ((FileLayoutHolder)viewHolder).playback_position.setVisibility(View.VISIBLE);
                        ((FileLayoutHolder)viewHolder).playback_position.setProgress((int) file.getPlaybackPercentage());
                    }
                }
                else {
                    ((FileLayoutHolder)viewHolder).playback_position.setVisibility(View.GONE);

                    if (file.getState() == FilesInfo.fileState.NEW)
                        ((FileLayoutHolder)viewHolder).newLabel.setVisibility(View.VISIBLE);
                    else
                        ((FileLayoutHolder)viewHolder).newLabel.setVisibility(View.GONE);
                }

                ((FileLayoutHolder)viewHolder).parenLayout.setSelected(selectedItem.get(position, false));

                break;
        }


    }


    @Override
    public int getItemCount() {
        switch (Constant.SORT_TYPE)
        {
            case 1:
                if (directoryList != null)
                    return directoryList.size();
                else
                    return 0;
            case 2:
                return videosList.size();
            default:
                    return 0;
        }
//        return videosList.size();
    }

    class FileLayoutHolder extends RecyclerView.ViewHolder{
        View parenLayout;
        FrameLayout thumbnail;
        ContentLoadingProgressBar playback_position;
        TextView newLabel;
        ImageView thumbnail_imageview;
        TextView durationTxtOverThumnail;
        TextView video_title;
        ImageView icon_more;


        public FileLayoutHolder(@NonNull View itemView, final OnRecyclerViewListener onRecyclerViewListener) {
            super(itemView);

            parenLayout = itemView.findViewById(R.id.file_parent_layout);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            thumbnail_imageview = itemView.findViewById(R.id.thumbnail_imgView);
            playback_position = itemView.findViewById(R.id.playback_position);
            newLabel = itemView.findViewById(R.id.new_label);
            durationTxtOverThumnail = itemView.findViewById(R.id.durationTxtOverThumnail);
            video_title = itemView.findViewById(R.id.video_title);
            icon_more = itemView.findViewById(R.id.video_icon_more);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                thumbnail.setClipToOutline(true);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRecyclerViewListener != null){
                        if (getAdapterPosition() != RecyclerView.NO_POSITION)
                            onRecyclerViewListener.onClick(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onRecyclerViewListener != null){
                        if (getAdapterPosition() != RecyclerView.NO_POSITION)
                            return onRecyclerViewListener.onLongClick(getAdapterPosition());
                    }
                    return false;
                }
            });

            icon_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRecyclerViewListener != null){
                        if (getAdapterPosition() != RecyclerView.NO_POSITION)
                            onRecyclerViewListener.onIconMoreClick(getAdapterPosition());
                    }
                }
            });
        }

    }

    class FolderLayoutHolder extends RecyclerView.ViewHolder {
        View parentLayout;
        TextView folderTitle;
        TextView nVideosTxt;
        ImageView icon_folder;

        public FolderLayoutHolder(@NonNull View itemView, final OnRecyclerViewListener onRecyclerViewListener) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            folderTitle = itemView.findViewById(R.id.folder_title);
            nVideosTxt = itemView.findViewById(R.id.nVideosTxt);
            icon_folder = itemView.findViewById(R.id.ic_folder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onRecyclerViewListener != null){
                        if(getAdapterPosition() != RecyclerView.NO_POSITION)
                            onRecyclerViewListener.onClick(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onRecyclerViewListener != null){
                        if(getAdapterPosition() != RecyclerView.NO_POSITION)
                            return onRecyclerViewListener.onLongClick(getAdapterPosition());
                    }

                    return false;
                }
            });

        }

    }

    public void toggleSelection(int pos){
        if (selectedItem.get(pos,false))
            selectedItem.delete(pos);
        else
            selectedItem.put(pos,true);

        notifyItemChanged(pos);
    }

    public void clearSelectedItem(){
        selectedItem.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount(){
        return selectedItem.size();
    }

    public void selectAllItem(){
        if (Constant.SORT_TYPE == 1){
            if (getSelectedItemCount() == directoryList.size())
                select_all = true;
            else
                select_all = false;
        }else if (Constant.SORT_TYPE == 2){
            if (getSelectedItemCount() == videosList.size())
                select_all = true;
            else
                select_all = false;
        }
        if (!select_all) {
            if (Constant.SORT_TYPE == 1) {
                for (int i = 0; i < Constant.directoryList.size(); i++) {
                    selectedItem.put(i, true);
                }
            }else {
                for (int i = 0; i < videosList.size(); i++) {
                    selectedItem.put(i, true);
                }
            }
            select_all = true;
        }
        else {
            selectedItem.clear();
            select_all = false;
        }
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItem.size());
        for (int i = 0; i < selectedItem.size(); i++) {
            items.add(selectedItem.keyAt(i));
        }
        return items;
    }

}
