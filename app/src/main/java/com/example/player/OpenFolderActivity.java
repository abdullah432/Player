package com.example.player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.player.Modal.Constant;
import com.example.player.Modal.FilesInfo;
import com.example.player.Modal.MaterialSearchView;
import com.example.player.Modal.Methods;
import com.example.player.Modal.SingleMediaScanner;
import com.example.player.Modal.StorageUtil;
import com.example.player.Modal.ThemeHelper;
import com.example.player.MyDialog.ActionModeInfoDialog;
import com.example.player.MyDialog.DeleteDialog;
import com.example.player.MyDialog.DialogsListener;
import com.example.player.MyDialog.InformationDialog;
import com.example.player.MyDialog.RenameDialog;
import com.example.player.MyDialog.VideoOptionBottomSheetDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;

import static java.security.AccessController.getContext;

public class OpenFolderActivity extends AppCompatActivity implements OnFileListener,
        VideoOptionBottomSheetDialog.BottomSheetListener, DialogsListener , SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;
    OpenFolderAdapter adapter;
    Toolbar toolbar;
    private int directoryPosition;
    private int videoPosition;
    private ArrayList<FilesInfo> directoryFiles;
    boolean permission;
    private ActionMode mActionMode;
    private ConstraintLayout selection_bottom_layout;
    private View layover_bg;
    private ImageButton checkAll;
    private ImageButton share;
    private ImageButton rename;
    private ImageButton delete;
    MenuItem infoOption;
    MenuItem playOption;
    MenuItem subtitleOption;
    private boolean callFromActionMode = false;

    //Search
    private MaterialSearchView mSearchLayout;

    //theme preferences
    SharedPreferences.Editor editor;

    private Toast toast;
    //we will record state of playback, if change from setting then onResume() we apply changes
    Constant.playbackRecord rememberState;
    String themeState;
    boolean showRememberIcon;
    //we will change image when state change
    MenuItem playback;

    //FilesInfo
    FilesInfo filesInfo;

    //To delete or rename
    private File selectedFile;
    private Uri sdCardUri;
    private final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1;
    private boolean deleteSingleFileCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_folder);

        rememberState = Constant.playbackState;

        mSearchLayout = findViewById(R.id.materialSearchViewID);

        //If we set elevation on toolbar then search box is not showing: Need Fix
//        //act as elevation for pre lollipop
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            findViewById(R.id.gradientShadow).setVisibility(View.GONE);
//        }

        buildRecyclerView();

        buildSelectionBottomLayout();

        //First we will store if changing occur from another activity. Then we will recreate activity onResume()
        themeState = Constant.themePref;
        //same here
        showRememberIcon = Constant.rememberIconState;
    }

    private void buildRecyclerView() {
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
        refreshLayout.setOnRefreshListener(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Receiving putExtra data from MainActivity
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            directoryPosition = extras.getInt("directoryPosition"); // retrieve the data using keyName

        Constant.selectedDirecotory = Constant.directoryList.get(directoryPosition);

        /*First we will checked the clicked directory and then we will get all the files of clicked directory
        * and assign it to new ArrayList
        * Note: if we didn't clear the currentFolderFiles. On second time when user click on directory.
        * Files will be added on it*/
        Constant.currentFolderFiles.clear();

        Constant.selectedDirecotory = Constant.directoryList.get(directoryPosition);
        //Constant.currentFolderFiles will always contain file which is currently open
        for (FilesInfo filesInfo : Constant.allMemoryVideoList)
        {
            if (Constant.selectedDirecotory.equals(filesInfo.getDirectory()))
            {
               Constant.currentFolderFiles.add(filesInfo);
            }
        }
//        for (int i=0; i < Constant.allMemoryVideoList.size(); i++)
//        {
//            if (Constant.selectedDirecotory.equals(Constant.allMemoryVideoList.get(i).getDirectory()))
//            {
//                Constant.currentFolderFiles.add(Constant.allMemoryVideoList.get(i));
//            }
//        }

        directoryFiles = Constant.currentFolderFiles;

        adapter = new OpenFolderAdapter(directoryFiles, this,this);

        //increase scroll performance but repeat the adapter
//        adapter.setHasStableIds(true);

        getSupportActionBar().setTitle(Constant.directoryList.get(directoryPosition).getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Increase Scrolling Performance of recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(adapter);
    }

    private void buildSelectionBottomLayout() {
        selection_bottom_layout = findViewById(R.id.selection_bottom_layout);
        layover_bg = findViewById(R.id.layover);
        checkAll = findViewById(R.id.ic_check);
        share = findViewById(R.id.ic_share);
        share.setVisibility(View.GONE);
        rename = findViewById(R.id.ic_rename);
        delete = findViewById(R.id.ic_delete);


        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAllItem();

                //To make sure rename button will disable when more than 1 video is selected
                updateRenameButton();

                int count = adapter.getSelectedItemCount();

                mActionMode.setTitle(count +" / "+Constant.currentFolderFiles.size());
                mActionMode.invalidate();

                disable_enable_buttons();

                adapter.notifyDataSetChanged();
//                Toast.makeText(getApplicationContext(),"check all",Toast.LENGTH_SHORT).show();
            }
        });

//        share.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(getApplicationContext(),"share",Toast.LENGTH_SHORT).show();
//            }
//        });
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adapter.getSelectedItems return position of currentFolderFiles
                List<Integer> selectedItem = adapter.getSelectedItems();
                videoPosition = selectedItem.get(0);
                openRenameDialog();
//                adapter.notifyItemChanged(videoPosition);
//                Toast.makeText(getApplicationContext(),"rename",Toast.LENGTH_SHORT).show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callFromActionMode = true;
                openDeleteDialog();
//                Toast.makeText(getApplicationContext(),"delete",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRenameButton() {
        int count = adapter.getSelectedItemCount();
        if (count > 1){
            rename.setAlpha(.3f);
        }
        else {
            rename.setAlpha(1f);
        }
    }

    private void disable_enable_buttons() {
        if (adapter.getSelectedItemCount() == 0){
            //When no file selected then we will disable all other buttons then check
            rename.setAlpha(.3f);
            rename.setEnabled(false);
//            share.setEnabled(false);
            delete.setAlpha(.3f);
            delete.setEnabled(false);

            infoOption.setEnabled(false);
            playOption.setEnabled(false);
            subtitleOption.setEnabled(false);
        }
        else
        {
//            share.setEnabled(true);

            delete.setAlpha(1f);
            delete.setEnabled(true);

            infoOption.setEnabled(true);
            playOption.setEnabled(true);
            subtitleOption.setEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem folderSort = menu.findItem(R.id.folderSortID);
        MenuItem fileSort = menu.findItem(R.id.fileSortID);
        MenuItem theme = menu.findItem(R.id.nav_nightModeID);
        playback = menu.findItem(R.id.remember_option);

        if(Constant.SORT_TYPE == 1)
            folderSort.setChecked(true);
        else
            fileSort.setChecked(true);

        //Theme
        if (Constant.themePref.equals(ThemeHelper.DARK_MODE))
            theme.setChecked(true);
        else
            theme.setChecked(false);

        //Remember playback
        if (Constant.playbackState == Constant.playbackRecord.YES)
            playback.setIcon(R.drawable.ic_remember_black_24dp);
        else
            playback.setIcon(R.drawable.ic_remember_opacity30);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchLayout.setRefreshLayout(refreshLayout);
        mSearchLayout.setMenuItem(menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            //From this activity this option is already selected. When second option will be selected then this activity will be destroy
            case R.id.folderSortID:
                break;
            case R.id.fileSortID:
                if(Constant.SORT_TYPE != 2)
                {
                    Constant.SORT_TYPE = 2;

                    SharedPreferences.Editor editor = getSharedPreferences("SORT_PREFERENCES",MODE_PRIVATE).edit();
                    editor.putInt("SORT_TYPE",2);
                    editor.apply();

//                    onBackPressed();
                    Intent intent = new Intent(OpenFolderActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
                break;
            case R.id.nav_nightModeID:
                if (Constant.themePref.equals(ThemeHelper.DARK_MODE)) {
                    item.setChecked(false);
                    Constant.themePref = ThemeHelper.LIGHT_MODE;
                    editor = getSharedPreferences("THEME",MODE_PRIVATE).edit();
                    editor.putString("themePref",ThemeHelper.LIGHT_MODE);
                    editor.apply();
                    ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE);
                    recreate();
                }
                else {
                    item.setChecked(true);
                    Constant.themePref = ThemeHelper.DARK_MODE;
                    editor = getSharedPreferences("THEME",MODE_PRIVATE).edit();
                    editor.putString("themePref",ThemeHelper.DARK_MODE);
                    editor.apply();
                    ThemeHelper.applyTheme(ThemeHelper.DARK_MODE);
                    recreate();
                }
                break;
            case R.id.remember_option:
                if (Constant.playbackState == Constant.playbackRecord.YES) {
                    Constant.playbackState = Constant.playbackRecord.NOT;
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"Videos will be resumed from start",Toast.LENGTH_LONG);
                    toast.show();
                    playback.setIcon(R.drawable.ic_remember_opacity30);
                    adapter.notifyDataSetChanged();
                }
                else {
                    Constant.playbackState = Constant.playbackRecord.YES;
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"Videos will be resumed from the point where you stopped",Toast.LENGTH_LONG);
                    toast.show();
                    playback.setIcon(R.drawable.ic_remember_black_24dp);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.setting:
                Intent intent = new Intent(OpenFolderActivity.this,SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSearchLayout.isSearchOpen()){
            mSearchLayout.closeSearch();
        }
        else {
//            super.onBackPressed();
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
//        else
//        {
//            Intent intent = new Intent(this,MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }

    }

    public void setVideoPosition(int videoPosition) {
        this.videoPosition = videoPosition;
    }

    //Permission Request Start

    private void check_WRITE_EXTERNAL_STORAGE_Permission() {
        //ContextCompat use to retrieve resources. It provide uniform interface to access resources.
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed to rename media file in your phone")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(OpenFolderActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        0);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                permission = true;
            }
        }
    }

    //Permission Request End

    //OpenFolderListener Start ------>

    @Override
    public void onVideoClick(int position) {
        if(mActionMode != null){
            toggleSelection(position);
            disable_enable_buttons();
        }
        else
        {
            Intent intent = new Intent(OpenFolderActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoPosition",position);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

    }

    @Override
    public void onIconMoreClick(int videoPosition) {
        setVideoPosition(videoPosition);
        //Open Bottom Modal Sheet
        VideoOptionBottomSheetDialog bottomSheetDialog = new VideoOptionBottomSheetDialog();
        bottomSheetDialog.setPosition(videoPosition);
        bottomSheetDialog.show(getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    @Override
    public boolean onVideoLongClick(int position) {
        if (mActionMode != null)
            return false;

        enableActionMode(position);
        return true;
    }

    private void enableActionMode(int pos){
        mActionMode = startSupportActionMode(mActionModeCallBack);
        toggleSelection(pos);
        disable_enable_buttons();
    }

    private void toggleSelection(int position){
        adapter.toggleSelection(position);

        updateRenameButton();

        int count = adapter.getSelectedItemCount();

        if (count == 0)
            mActionMode.finish();
        else {
            mActionMode.setTitle(count +" / "+Constant.currentFolderFiles.size());
            mActionMode.invalidate();
        }

    }

    private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            selection_bottom_layout.setVisibility(View.VISIBLE);
            layover_bg.setVisibility(View.VISIBLE);

//            share.setAlpha(.3f);

            infoOption = menu.findItem(R.id.info_option);
            playOption = menu.findItem(R.id.play_option);
            subtitleOption = menu.findItem(R.id.search_subtitle_option);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            switch (menuItem.getItemId())
            {
                case R.id.info_option:
                    int count = adapter.getSelectedItemCount();
                    if (count == 1){
                        List<Integer> selectedItem = adapter.getSelectedItems();
                        videoPosition = selectedItem.get(0);
                        openInformationDialog();
                    }
                    else {
                        ActionModeInfoDialog dialog = new ActionModeInfoDialog();

                        List<Integer> selectedItem = adapter.getSelectedItems();
                        double sizeInBytes = 0.0;
                        double sizeInGB;
                        String size;
                        int numberOfFiles = selectedItem.size();
                        File file;
                        for(int pos : selectedItem){
                            file = Constant.allMemoryVideoList.get(pos).getFile().getAbsoluteFile();
                            sizeInBytes += file.length();
                        }

                        int sizeInKB = (int) (sizeInBytes / 1024);  //all the other value than GB will be shown without decimal point
                        if (sizeInKB >= 1048576){
                            sizeInGB = sizeInBytes / (1024 * 1024 * 1024);
                            size = Methods.round(sizeInGB, 2) + " GB";
                        }
                        else if (sizeInKB >= 1024){
                            size = sizeInKB/1024 + " MB";
                        }
                        else
                            size = sizeInKB + " KB";

                        Bundle bundle = new Bundle();
                        bundle.putString("Contains",String.valueOf(numberOfFiles));
                        bundle.putString("TotalSize",size);
                        dialog.setArguments(bundle);
                        dialog.show(getSupportFragmentManager(),dialog.getTag());
                    }
//                    Toast.makeText(getApplicationContext(),"Info",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.play_option:
                    Toast.makeText(getApplicationContext(),"Play",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.search_subtitle_option:
                    Toast.makeText(getApplicationContext(),"Search Subtitle",Toast.LENGTH_SHORT).show();
                    return true;
                    default:
                        return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            adapter.clearSelectedItem();
            layover_bg.setVisibility(View.GONE);
            selection_bottom_layout.setVisibility(View.GONE);
            mActionMode = null;
        }
    };

    //OpenFolderListener End ------>

    //BottomSheetListener Start ------>

    @Override
    public void openShareDialog() {

    }

    @Override
    public void openRenameDialog() {
        check_WRITE_EXTERNAL_STORAGE_Permission();

        if(permission){
            RenameDialog dialog = new RenameDialog();

            //passing directoryPosition to new Fragment
            Bundle bundle = new Bundle();
            bundle.putInt("position", videoPosition);
            bundle.putInt("type",0);    //0 mean video
            dialog.setArguments(bundle);

            dialog.show(getSupportFragmentManager(), dialog.getTag());
        }
    }

    @Override
    public void openInformationDialog() {

        InformationDialog dialog = new InformationDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("videoPosition", videoPosition);
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), dialog.getTag());

    }

    @Override
    public void openDeleteDialog() {
        check_WRITE_EXTERNAL_STORAGE_Permission();
        if (permission){
            DeleteDialog dialog = new DeleteDialog();
            Bundle bundle = new Bundle();
            int count = adapter.getSelectedItemCount();
            if (callFromActionMode){
                if (count > 1)
                    bundle.putInt("msg", 3);    //3 means more than 1 file is selected
            }
            else
                bundle.putInt("msg", 0);    //0 means 1 file is selected

            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), dialog.getTag());
        }

    }

    //BottomSheetListener End ------>

    //DialogsListener Start ------>

    @Override
    public void renameFile(File directory, String previousName, String changeName) {

        //Resume permission check

        File from = new File(directory, previousName);
        File to = new File(directory, changeName);
        boolean result = from.renameTo(to);
        if(!result)
            Toast.makeText(this,"File Renaming Fail. Try Again",Toast.LENGTH_SHORT).show();
        else
        {
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(to)));

            new SingleMediaScanner(this, from);

            //when file is rename. Changes should be occur in Constant.allMemoryFiles
            //adapter.getSelectedItems return position of selected Item. Which we assign to videoPosition
            int index = Constant.allMemoryVideoList.indexOf(Constant.currentFolderFiles.get(videoPosition));
            Constant.allMemoryVideoList.get(index).setFile(to);
            Constant.currentFolderFiles.get(videoPosition).setFile(to);

            if (mActionMode != null)
                mActionMode.finish();

            adapter.notifyItemChanged(videoPosition);

            //update the playback record to
            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                if ((from.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                    Constant.filesPlaybackHistory.get(i).setFileName(to.getName());
                    //save the playback history
                    Paper.book().write("playbackHistory",Constant.filesPlaybackHistory);
                    break;
                }
            }
        }

    }

    @Override
    public void deleteFile() {
        if (callFromActionMode) {
            List<Integer> selectedFilePosition = adapter.getSelectedItems();
            List<Integer> sdCardSelectedList = new ArrayList<>();
            boolean result;
            for (int position : selectedFilePosition) {
                selectedFile = Constant.currentFolderFiles.get(position).getFile().getAbsoluteFile();
                result = selectedFile.delete();
                if (result) {
                    //remove file from memory
                    getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedFile)));
                    //Methods.removeMedia(this,selectedFile.getPath());

                    //do not remove file from list here. because it will deleted wrong position due to remove list position

                    /*also remove it from playbackHistory list
                     * getFileName() contain file.getName()*/
                    for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                        if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                            Constant.filesPlaybackHistory.remove(i);
                            break;
                        }
                    }
                }else {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!selectedFile.getParent().equals("/storage/emulated/0")) {

                            List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
                            if (permissions != null && permissions.size() > 0){
                                sdCardUri = permissions.get(0).getUri();
                                deleteFileWithSAF();
                            }else {
                                //if file is not from internal memory that can be from sdcard
                                sdCardSelectedList.add(position);

                            }

                        }
                    }
                }
            }

            //In this stage we delete all selected files and get list of sdcard files
            //which can't be deleted without permission
            if (sdCardSelectedList.size() > 0 && sdCardSelectedList != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please select external storage directory (e.g SDCard)")
                        .setMessage("Due to change in android security policy it is not possible to delete or rename file in external storage without granting permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // call for document tree dialog
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }

            //check if permission is not available then don't remove sdcard files
            List<UriPermission> permissions = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissions = getContentResolver().getPersistedUriPermissions();
                if (permissions == null || permissions.size() == 0){
                    for (int pos : sdCardSelectedList){
                        selectedFilePosition.remove(Integer.valueOf(pos));
                    }
                }
            }

            //save the playback history
            Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);

            // Remove Deleted Folders from Constant.currentFolderFiles, Constant.allMemoryFileList
            List<FilesInfo> listOfSelectedFiles = new ArrayList<>();
            for (int pos : selectedFilePosition) {
                listOfSelectedFiles.add(Constant.currentFolderFiles.get(pos));
            }

            for (FilesInfo file : listOfSelectedFiles) {
                Constant.allMemoryVideoList.remove(file);
                Constant.currentFolderFiles.remove(file);
            }

            callFromActionMode = false;
            mActionMode.finish();

            adapter.notifyDataSetChanged();
        } else {
            selectedFile = Constant.currentFolderFiles.get(videoPosition).getFile().getAbsoluteFile();

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!selectedFile.getParent().equals("/storage/emulated/0")) {
                    List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
                    if (permissions != null && permissions.size() > 0) {
                        sdCardUri = permissions.get(0).getUri();
                        deleteSingleFileCall = true;
                        deleteFileWithSAF();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Please select external storage directory (e.g SDCard)")
                                .setMessage("Due to change in android security policy it is not possible to delete or rename file in external storage without granting permission")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // call for document tree dialog
                                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                } else {
                    removeSingleFile();
                }
            } else {
                removeSingleFile();
            }
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
//
////                File photoLcl = new File(homeDirectory + "/" + fileNameLcl);
////                Uri fileUri = FileProvider.getUriForFile(this,
////                        this.getApplicationContext().getPackageName() +
////                                ".provider", selectedFile);
//                Uri contentUri = FileProvider.getUriForFile(this,
//                        this.getApplicationContext().getPackageName() + ".fileprovider", selectedFile);
//
//                this.grantUriPermission(this.getApplicationContext().getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION  | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                int result = this.getContentResolver().delete(contentUri,null,null);
////                boolean result = false;
////                                    try {
////                       result = DocumentsContract.deleteDocument(this.getContentResolver(),contentUri);
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    }
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////                    try {
////                        result = DocumentsContract.deleteDocument(this.getContentResolver(),contentUri);
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    }
////                }
//                if(result == 0)
//                    Toast.makeText(this,"Error: File Not Deleted",Toast.LENGTH_SHORT).show();
//                else {
//                    getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedFile)));
////                    Methods.removeMedia(this,selectedFile.getPath());
//
//                    Constant.allMemoryVideoList.remove(Constant.currentFolderFiles.get(videoPosition));
//                    Constant.currentFolderFiles.remove(videoPosition);
//
//                    /*update the playback record to
//                     * getFileName() contain file.getName()*/
//                    for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
//                        if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
//                            Constant.filesPlaybackHistory.remove(i);
//                            //save the playback history
//                            Paper.book().write("playbackHistory",Constant.filesPlaybackHistory);
//                            break;
//                        }
//                    }
//
//                    adapter.notifyItemRemoved(videoPosition);
//                }
//            }
        }

        //If folder have 1 video and we delete it then recycler view should remove that file
        //ToDo

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_DOCUMENT_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    sdCardUri = data.getData();
                    getContentResolver().takePersistableUriPermission(sdCardUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    deleteFileWithSAF();

                }
                break;
        }
    }

    private void deleteFileWithSAF() {
        //First we get `DocumentFile` from the `TreeUri` which in our case is `sdCardUri`.
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, sdCardUri);

        //Then we split file path into array of strings.
        //ex: parts:{"", "storage", "extSdCard", "MyFolder", "MyFolder", "myImage.jpg"}
        // There is a reason for having two similar names "MyFolder" in
        //my exmple file path to show you similarity in names in a path will not
        //distract our hiarchy search that is provided below.
        String[] parts = (selectedFile.getPath()).split("\\/");

        // findFile method will search documentFile for the first file
        // with the expected `DisplayName`

        // We skip first three items because we are already on it.(sdCardUri = /storage/extSdCard)
        for (int i = 3; i < parts.length; i++) {
            if (documentFile != null) {
                documentFile = documentFile.findFile(parts[i]);
            }
        }

        if (documentFile == null) {

            // File not found on tree search
            // User selected a wrong directory as the sd-card
            // Here must inform user about how to get the correct sd-card
            // and invoke file chooser dialog again.

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please select root of external storage directory (click SELECT button at bottom)")
                    .setMessage("Due to change in android security policy it is not possible to delete or rename file in external storage without granting permission")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // call for document tree dialog
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        } else {

            // File found on sd-card and it is a correct sd-card directory
            // save this path as a root for sd-card on your database(SQLite, XML, txt,...)

            // Now do whatever you like to do with documentFile.
            // Here I do deletion to provide an example.


            if (documentFile.delete()) {// if delete file succeed
                // Remove information related to your media from ContentResolver,
                // which documentFile.delete() didn't do the trick for me.
                // Must do it otherwise you will end up with showing an empty
                // ImageView if you are getting your URLs from MediaStore.

//                            Uri contentUri = FileProvider.getUriForFile(this,
//                                    this.getApplicationContext().getPackageName() + ".fileprovider", selectedFile);
//                            getContentResolver().delete(contentUri , null, null);
//


                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedFile)));
//                              Methods.removeMedia(this,selectedFile.getPath());

                if (deleteSingleFileCall){
                    Constant.allMemoryVideoList.remove(videoPosition);
                    adapter.notifyItemRemoved(videoPosition);
                    deleteSingleFileCall = false;
                }

                /*update the playback record to
                 * getFileName() contain file.getName()*/
                for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                    if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                        Constant.filesPlaybackHistory.remove(i);
                        break;
                    }
                }

                //save the playback history
                Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);

            }
        }
    }

    private void removeSingleFile() {
        boolean result;
        result = selectedFile.delete();
        if (!result)
            Toast.makeText(this, "Error: File Not Deleted", Toast.LENGTH_SHORT).show();
        else {
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedFile)));
//                    Methods.removeMedia(this,selectedFile.getPath());

            Constant.allMemoryVideoList.remove(Constant.currentFolderFiles.get(videoPosition));
            Constant.currentFolderFiles.remove(videoPosition);

            /*update the playback record to
             * getFileName() contain file.getName()*/
            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                    Constant.filesPlaybackHistory.remove(i);
                    break;
                }
            }

            //save the playback history
            Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);

            adapter.notifyItemRemoved(videoPosition);
        }

    }

    //DialogsListener End ------>

    //OnRefreshListener Start ------>

    @Override
    public void onRefresh() {

        String[] allPath = StorageUtil.getStorageDirectories(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File directory;

                    for (String path: allPath){
                        directory = new File(path);
                        Methods.refresh_Directory_Files(directory);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
    //OnRefreshListener End

    //onResume will be called

    @Override
    protected void onResume() {
        super.onResume();

        if (mSearchLayout.isSearchOpen())
        {
            mSearchLayout.updateAdapter();
        }
        else
        {
            /*if theme changed then activity will recreated else*/

            //if theme is changed, Then we will recreate activity else not
            if (!themeState.equals(Constant.themePref))
                recreate();
            else
            {
                //if remember option is changed, Then we will change icon else not
                if (!rememberState.equals(Constant.playbackState)) {
                    if (Constant.playbackState == Constant.playbackRecord.YES) {
                        playback.setIcon(R.drawable.ic_remember_black_24dp);
                        //update local variable
                        rememberState = Constant.playbackState;
                    } else {
                        playback.setIcon(R.drawable.ic_remember_opacity30);
                        //update local variable
                        rememberState = Constant.playbackState;
                    }
                }

                if (showRememberIcon != Constant.rememberIconState) {
                    if (Constant.rememberIconState) {
                        playback.setVisible(true);
                        //update local variable
                        showRememberIcon = Constant.rememberIconState;
                    } else {
                        playback.setVisible(false);
                        //update local variable
                        showRememberIcon = Constant.rememberIconState;
                    }
                }

                overridePendingTransition(0, 0);

                adapter.notifyDataSetChanged();

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        directoryFiles.clear();
//                        directoryFiles = Constant.currentFolderFiles;
//                        adapter.notifyDataSetChanged();
//                    }
//                });
            }
        }
    }


    //OnRefreshListener End ------>

}
