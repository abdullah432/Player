package com.example.player.Modal;

//import android.content.Context;
//import android.database.Cursor;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Adapter;
//import android.widget.AdapterView;
//import android.widget.CursorAdapter;
//import android.widget.SearchView;
//import android.widget.TextView;
//
//public class SuggestionAdapter extends CursorAdapter {
//
//    LayoutInflater mInflater;
//    Context mContext;
//    SearchView mSearchView;
//
//    public SuggestionAdapter(Context context, Cursor c, SearchView searchView) {
//        super(context, c, false);
//        mContext = context;
//        mSearchView = searchView;
//        mInflater = LayoutInflater.from(context);
//    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        View v = mInflater.inflate(R.layout.search_view_suggestion_row, parent, false);
//        return v;
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        TextView videoTitle = view.findViewById(R.id.text1);
//        int pos = cursor.getInt(0);
//        videoTitle.setText(Constant.outputString.get(pos));
//    }
//}

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.player.OnFileListener;
import com.example.player.R;
import com.example.player.RecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.FileLayoutHolder> implements Filterable {

    public static ArrayList<FilesInfo> suggestionData;
    private ArrayList<FilesInfo> fileList;
    private OnFileListener mFItemListener;
    private String filePath;
    private int lastIndex;
    private String filetitle;
    private Context mContext;
    private SparseBooleanArray selected_items;
    private boolean select_all = true;

    //RecyclerView
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView suggestionRecyclerView;
    private TextView notFoundView;

    public SearchAdapter(Context context,ArrayList<FilesInfo> fileList, SwipeRefreshLayout refreshLayout,
                         RecyclerView suggestionRecyclerView, TextView notFoundView, OnFileListener onFileListener) {
        suggestionData = new ArrayList<>();
        this.fileList = fileList;
        mFItemListener = onFileListener;
        mContext = context;
        selected_items = new SparseBooleanArray();

        this.refreshLayout = refreshLayout;
        this.suggestionRecyclerView = suggestionRecyclerView;
        this.notFoundView = notFoundView;
    }


    @NonNull
    @Override
    public FileLayoutHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_sort_list, viewGroup, false);
        return new FileLayoutHolder(view, mFItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileLayoutHolder fileLayoutHolder, int pos) {
        final FilesInfo file = suggestionData.get(pos);

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
    }

    @Override
    public int getItemCount() {
        return suggestionData.size();
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

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (!TextUtils.isEmpty(constraint)) {

                // Retrieve the autocomplete results.
                List<FilesInfo> searchData = new ArrayList<>();

                for (int i=0; i<fileList.size(); i++) {
                    if (fileList.get(i).getFile().getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        searchData.add(fileList.get(i));
                    }
                }

//                // Assign the suggestionData to the FilterResults
//                if (filterResults.values != null)
//                    filterResults.values = null;

                filterResults.values = searchData;
                filterResults.count = searchData.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                suggestionData = (ArrayList<FilesInfo>) results.values;

                //make it static so if user click on video then we can access it in videoPlayerActivity
                Constant.suggestionData = suggestionData;

                if (suggestionData.size() != 0){
                    notFoundView.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.GONE);
                    suggestionRecyclerView.setVisibility(View.VISIBLE);
                }
                else {
                    refreshLayout.setVisibility(View.GONE);
                    suggestionRecyclerView.setVisibility(View.GONE);
                    notFoundView.setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            }
        }
    };

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
            for (int i = 0; i < suggestionData.size(); i++) {
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

}
