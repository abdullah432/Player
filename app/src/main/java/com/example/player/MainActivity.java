package com.example.player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.player.Modal.FilesInfo;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.player.Modal.Constant;
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
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnRecyclerViewListener,
        SwipeRefreshLayout.OnRefreshListener, DialogsListener, VideoOptionBottomSheetDialog.BottomSheetListener {

    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    File directory;
    ArrayList<File> directoryList = new ArrayList<>();
    SharedPreferences.Editor editor;
    SharedPreferences preferences;
    int SORT_TYPE;
    ActionMode mActionMode;
    private ConstraintLayout selection_bottom_layout;
    private View layover_bg;
    private ImageButton checkAll;
    private ImageButton share;
    private ImageButton rename;
    private ImageButton delete;
    MenuItem infoOption;
    MenuItem playOption;
    MenuItem subtitleOption;
    int fPosition; // may be folder or file
    Toolbar toolbar;

    Toast toast = null;
    MenuItem playback;
    String themeState;
    Constant.playbackRecord rememberState;
    boolean showRememberIcon;

    //Search
    private MaterialSearchView mSearchLayout;
    private boolean callFromActionMode;
    private boolean permission;

    //external storage permission
    private final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1;
    Uri sdCardUri;
    private File selectedFile;
    private boolean deleteSingleFileCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissionForStorageAccess();

        //First we will store if changing occur from another activity. Then we will recreate activity onResume()
        themeState = Constant.themePref;
        //First we will store if changing occur from another activity. Then we will recreate activity onResume()
        rememberState = Constant.playbackState;

        preferences = getSharedPreferences("SORT_PREFERENCES", MODE_PRIVATE);
        SORT_TYPE = preferences.getInt("SORT_TYPE", 1);
        Constant.SORT_TYPE = SORT_TYPE;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (SORT_TYPE == 1)
            getSupportActionBar().setTitle("Folders");
        else
            getSupportActionBar().setTitle("Videos");

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
        refreshLayout.setOnRefreshListener(this);

        buildSelectionBottomLayout();

        setRecyclerView();

        initSearch();

        showRememberIcon = Constant.rememberIconState;
    }

    private void setRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);

        //Increase Scrolling Performance of recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerViewAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void initSearch() {
        mSearchLayout = findViewById(R.id.materialSearchViewID);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem fileSort = menu.findItem(R.id.fileSortID);
        MenuItem folderSort = menu.findItem(R.id.folderSortID);
        MenuItem theme = menu.findItem(R.id.nav_nightModeID);
        playback = menu.findItem(R.id.remember_option);

        //Sort type
        if (SORT_TYPE == 1)
            folderSort.setChecked(true);
        else if (SORT_TYPE == 2)
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

        //check if user want to show remember icon on Toolbar or not
        if (Constant.rememberIconState)
            playback.setVisible(true);
        else
            playback.setVisible(false);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchLayout.setRefreshLayout(refreshLayout);
        mSearchLayout.setMenuItem(menuItem);
        return true;
    }

    //Select Nav Menu Option Item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.folderSortID:
                if (Constant.SORT_TYPE != 1) {
                    Constant.SORT_TYPE = 1;
                    item.setChecked(true);
                    getSupportActionBar().setTitle("Folders");
                    adapter.notifyDataSetChanged();

                    editor = getSharedPreferences("SORT_PREFERENCES", MODE_PRIVATE).edit();
                    editor.putInt("SORT_TYPE", 1);
                    editor.apply();
                }

                break;
            case R.id.fileSortID:
                if (Constant.SORT_TYPE != 2) {
                    Constant.SORT_TYPE = 2;
                    adapter.notifyDataSetChanged();
                    item.setChecked(true);
                    getSupportActionBar().setTitle("Videos");

                    editor = getSharedPreferences("SORT_PREFERENCES", MODE_PRIVATE).edit();
                    editor.putInt("SORT_TYPE", 2);
                    editor.apply();
                }

                break;
            case R.id.nav_nightModeID:
                if (Constant.themePref.equals(ThemeHelper.DARK_MODE)) {
                    item.setChecked(false);
                    Constant.themePref = ThemeHelper.LIGHT_MODE;
                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.LIGHT_MODE);
                    editor.apply();
                    ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE);
                    recreate();
                } else {
                    item.setChecked(true);
                    Constant.themePref = ThemeHelper.DARK_MODE;
                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.DARK_MODE);
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
                    toast = Toast.makeText(this, "Videos will be resumed from start", Toast.LENGTH_LONG);
                    toast.show();
                    playback.setIcon(R.drawable.ic_remember_opacity30);
                    editor = getSharedPreferences("PLAYBACK_STATE", MODE_PRIVATE).edit();
                    editor.putBoolean("state", false);
                    editor.apply();
                } else {
                    Constant.playbackState = Constant.playbackRecord.YES;
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(this, "Videos will be resumed from the point where you stopped", Toast.LENGTH_LONG);
                    toast.show();
                    playback.setIcon(R.drawable.ic_remember_black_24dp);
                    editor = getSharedPreferences("PLAYBACK_STATE", MODE_PRIVATE).edit();
                    editor.putBoolean("state", true);
                    editor.apply();
                }
                break;
            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildSelectionBottomLayout() {
        selection_bottom_layout = findViewById(R.id.selection_bottom_layout);
        layover_bg = findViewById(R.id.layover);
        checkAll = findViewById(R.id.ic_check);
        share = findViewById(R.id.ic_share);
        rename = findViewById(R.id.ic_rename);
        delete = findViewById(R.id.ic_delete);
        //share and play button is disable in ActionMode creation

        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAllItem();

                //To make sure rename button will disable when more than 1 video is selected
                updateRenameButton();

                int count = adapter.getSelectedItemCount();

                if (Constant.SORT_TYPE == 1) {
                    mActionMode.setTitle(count + " / " + Constant.directoryList.size());
                    mActionMode.invalidate();
                } else if (Constant.SORT_TYPE == 2) {
                    mActionMode.setTitle(count + " / " + Constant.allMemoryVideoList.size());
                    mActionMode.invalidate();
                }

                disable_enable_buttons();

                adapter.notifyDataSetChanged();
//                Toast.makeText(getApplicationContext(),"check all",Toast.LENGTH_SHORT).show();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "share", Toast.LENGTH_SHORT).show();
            }
        });
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> selectedItem = adapter.getSelectedItems();
                fPosition = selectedItem.get(0);
                openRenameDialog();
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

    //BottomSheetListener Start

    @Override
    public void openDeleteDialog() {
        check_WRITE_EXTERNAL_STORAGE_Permission();
        if (permission) {
            DeleteDialog dialog = new DeleteDialog();
            Bundle bundle = new Bundle();

            int count = adapter.getSelectedItemCount();
            if (Constant.SORT_TYPE == 1) {
                if (count > 1)
                    bundle.putInt("msg", 2); //2 means more than 1 folders is selected
                else
                    bundle.putInt("msg", 1); //1 means 1 folder is selected
            } else if (Constant.SORT_TYPE == 2) {
                if (count > 1)
                    bundle.putInt("msg", 3); //3 means more than 1 files is selected
                else
                    bundle.putInt("msg", 0); //1 means 1 file is selected
            }

            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), dialog.getTag());
        }
    }

    @Override
    public void openRenameDialog() {
        RenameDialog dialog = new RenameDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("position", fPosition);
        bundle.putInt("Type", 1); // 1 means directory
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    @Override
    public void openInformationDialog() {

        InformationDialog dialog = new InformationDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("videoPosition", fPosition);
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), dialog.getTag());

    }

    @Override
    public void openShareDialog() {

    }

    //BottomSheetListener End

    private void openActionModalInfoDialog() {
        ActionModeInfoDialog dialog = new ActionModeInfoDialog();

        List<Integer> selectedItem = adapter.getSelectedItems();
        int numberOfFiles = 0;
        double sizeInBytes = 0.0;
        double sizeInGB;
        String size;
        if (Constant.SORT_TYPE == 1) {
            for (int pos : selectedItem) {
                File filePath = Constant.directoryList.get(pos).getAbsoluteFile();
                numberOfFiles += Methods.getFolderMediaFileCount(filePath);
                File[] file = filePath.listFiles();
                for (int i = 0; i < file.length; i++) {
                    for (String extensionType : Constant.videoExtensions) {
                        if (file[i].getPath().endsWith(extensionType)) {
                            sizeInBytes += file[i].length();
                            break;
                        }
                    }
                }

            }
        } else if (Constant.SORT_TYPE == 2) {
            numberOfFiles = selectedItem.size();

            File file;
            for (int pos : selectedItem) {
                file = Constant.allMemoryVideoList.get(pos).getFile().getAbsoluteFile();
                sizeInBytes += file.length();
            }

        }

        int sizeInKB = (int) (sizeInBytes / 1024);  //all the other value than GB will be shown without decimal point
        if (sizeInKB >= 1048576) {
            sizeInGB = sizeInBytes / (1024 * 1024 * 1024);
            size = Methods.round(sizeInGB, 2) + " GB";
        } else if (sizeInKB >= 1024) {
            size = sizeInKB / 1024 + " MB";
        } else
            size = sizeInKB + " KB";

        Bundle bundle = new Bundle();
        bundle.putString("Contains", String.valueOf(numberOfFiles));
        bundle.putString("TotalSize", size);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    private void disable_enable_buttons() {
        int count = adapter.getSelectedItemCount();
        if (count == 0) {
            rename.setAlpha(.3f);
            rename.setEnabled(false);

            delete.setAlpha(.3f);
            delete.setEnabled(false);

            subtitleOption.setEnabled(false);
//            playOption.setEnabled(false);
            infoOption.setEnabled(false);
        } else {
            delete.setAlpha(1f);
            delete.setEnabled(true);

            subtitleOption.setEnabled(true);

//            playOption.setEnabled(true);

            infoOption.setEnabled(true);
        }
    }

    private void updateRenameButton() {
        int count = adapter.getSelectedItemCount();

        if (count > 1) {
            rename.setAlpha(.3f);
            rename.setEnabled(false);
        } else {
            rename.setAlpha(1f);
            rename.setEnabled(true);
        }
    }

    @Override
    public void onClick(int position) {
        if (mActionMode != null) {
            toggleSelection(position);
            disable_enable_buttons();
        } else {
            Intent intent;
            switch (Constant.SORT_TYPE) {
                case 1:
                    intent = new Intent(MainActivity.this, OpenFolderActivity.class);
                    intent.putExtra("directoryPosition", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("videoPosition", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public boolean onLongClick(int position) {
        if (mActionMode != null)
            return false;

        enableActionMode(position);
        return true;
    }

    @Override
    public void onIconMoreClick(int position) {
        fPosition = position;
        //Open Bottom Modal Sheet
        VideoOptionBottomSheetDialog bottomSheetDialog = new VideoOptionBottomSheetDialog();
        bottomSheetDialog.setPosition(position);
        bottomSheetDialog.show(getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    private void enableActionMode(int pos) {
        mActionMode = startSupportActionMode(mActionModeListener);
        toggleSelection(pos);
        disable_enable_buttons();
    }

    private void toggleSelection(int pos) {
        adapter.toggleSelection(pos);

        updateRenameButton();

        int count = adapter.getSelectedItemCount();

        if (count == 0)
            mActionMode.finish();
        else {
            if (Constant.SORT_TYPE == 1) {
                mActionMode.setTitle(count + " / " + Constant.directoryList.size());
                mActionMode.invalidate();
            } else {
                mActionMode.setTitle(count + " / " + Constant.allMemoryVideoList.size());
                mActionMode.invalidate();
            }
        }
    }

    ActionMode.Callback mActionModeListener = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            selection_bottom_layout.setVisibility(View.VISIBLE);
            layover_bg.setVisibility(View.VISIBLE);

            infoOption = menu.findItem(R.id.info_option);
            playOption = menu.findItem(R.id.play_option);
            subtitleOption = menu.findItem(R.id.search_subtitle_option);

            //When functionality complete then i will enable it.
            share.setAlpha(.3f);
            share.setEnabled(false);

            playOption.setEnabled(false);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.info_option:
                    openActionModalInfoDialog();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            adapter.clearSelectedItem();
            selection_bottom_layout.setVisibility(View.GONE);
            layover_bg.setVisibility(View.GONE);
            mActionMode = null;
        }
    };


    //Remove back button pressed activity transition animation
    @Override
    protected void onResume() {
        super.onResume();

        if (mSearchLayout.isSearchOpen()) {
            mSearchLayout.updateAdapter();
        } else {
            /*if theme changed then activity will recreated else*/

            //if theme is changed, Then we will recreate activity else not
            if (!themeState.equals(Constant.themePref))
                recreate();
            else {
                //if remember option is changed, Then we will change icon else noy
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
            }
        }
    }

    //OnRefreshListener Start
    @Override
    public void onRefresh() {

        checkPermissionForStorageAccess();

        String[] allPath = StorageUtil.getStorageDirectories(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File directory;

                    for (String path : allPath) {
                        directory = new File(path);
                        Methods.refresh_Directory_Files(directory, directory.toString());
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


    @Override
    public void onBackPressed() {
        if (mSearchLayout.isSearchOpen()) {
            mSearchLayout.closeSearch();
        } else {
            //minimize app
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    //DialogListener Start
    @Override
    public void renameFile(File dir, String previousName, String changeName) {
        File from = new File(dir, previousName);
        File to = new File(dir, changeName);
        boolean result = from.renameTo(to);

        if (!result)
            Toast.makeText(this, "File Renaming Fail. Try Again", Toast.LENGTH_SHORT).show();
        else {
//            Methods.removeMedia(getApplicationContext(), from.getPath());

            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(to)));

            new SingleMediaScanner(this, from);

//            MediaScannerConnection.scanFile(
//                    getApplicationContext(),
//                    new String[]{to.getAbsolutePath()},
//                    null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        @Override
//                        public void onScanCompleted(String path, Uri uri) {
//                            Log.v("grokkingandroid",
//                                    "file " + path + " was scanned seccessfully: " + uri);
//                        }
//                    });

            if (mActionMode != null)
                mActionMode.finish();

            //directoryPosition is acquired in rename.setOnClickListener : ActionMode Item
//            Constant.directoryList.set(directoryPosition, to);
//            adapter.notifyItemChanged(directoryPosition);

            if (Constant.SORT_TYPE == 1) {
                //Upper method will not work because if we change base folder name then application will generate error
                Constant.directoryList.clear();
                Constant.allMemoryVideoList.clear();

                String[] allPath = StorageUtil.getStorageDirectories(this);
                File directory;

                for (String path : allPath) {
                    directory = new File(path);
                    Methods.update_Directory_Files(directory, directory.toString());
                }

                adapter.notifyDataSetChanged();
            } else if (Constant.SORT_TYPE == 2) {
                //fPosition:
                Constant.allMemoryVideoList.get(fPosition).setFile(to);
                adapter.notifyItemChanged(fPosition);
            }
        }

        //update the playback record to
        for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
            if ((from.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                Constant.filesPlaybackHistory.get(i).setFileName(to.getName());
                //save the playback history
                Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);
                break;
            }
        }

    }

    @Override
    public void deleteFile() {
        List<Integer> selectedList = adapter.getSelectedItems();
        //callFromActionMode
        List<Integer> sdCardSelectedList = new ArrayList<>();
        //When user  delete folder
        List<Integer> sdCardDirectoryPosList = new ArrayList<>();
        List<FilesInfo> deletedFilesList = new ArrayList<>();

        File currentDirectory;
        boolean result;

        if (Constant.SORT_TYPE == 1) {
            for (int pos : selectedList) {
                currentDirectory = Constant.directoryList.get(pos);

                for (FilesInfo file : Constant.allMemoryVideoList) {
                    if (file.getDirectory().equals(currentDirectory)) {
                        selectedFile = file.getFile();
                        result = file.getFile().delete();
                        if (result) {
                            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getFile())));

                            //You can't remove items from a list while iterating over it.
                            //Constant.allMemoryVideoList.remove(file);

                            deletedFilesList.add(file);

                            /*also remove it from playbackHistory list
                             * getFileName() contain file.getName()*/
                            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                                if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                                    Constant.filesPlaybackHistory.remove(i);
                                    break;
                                }
                            }
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!file.getStorageType().equals("/storage/emulated/0")) {

                                    List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
                                    if (permissions != null && permissions.size() > 0){
                                        sdCardUri = permissions.get(0).getUri();
                                        deleteFileWithSAF();
                                        deletedFilesList.add(file);
                                    }else {
                                        //if file is not from internal memory that can be from sdcard
                                        if (!sdCardDirectoryPosList.contains(pos))
                                            sdCardDirectoryPosList.add(pos);
                                    }

                                }
                            }
                        }

                    }
                }
            }

            //In this stage we delete all selected files and get list of sdcard files
            //which can't be deleted without permission
            if (sdCardDirectoryPosList.size() > 0 && sdCardDirectoryPosList != null){
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
                    for (int pos : sdCardDirectoryPosList){
                        selectedList.remove(Integer.valueOf(pos));
                    }
                }
            }

            //save the playback history
            Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);

            // Remove Deleted Folders from memory
            List<File> listOfSelectedFolders = new ArrayList<>();
            for (int pos : selectedList) {
                listOfSelectedFolders.add(Constant.directoryList.get(pos));
            }

            //Remove deleted files from memory
            if (deletedFilesList.size() > 0 && deletedFilesList != null) {
                for (FilesInfo file : deletedFilesList) {
                    Constant.allMemoryVideoList.remove(file);
                }
            }

            for (File folder : listOfSelectedFolders) {
                Constant.removeFolderFromDirectoryList(folder);
            }

            mActionMode.finish();
            if (selectedList.size() == 1)
                adapter.notifyItemRemoved(selectedList.get(0));
            else
                adapter.notifyDataSetChanged();

        } else if (Constant.SORT_TYPE == 2) {
            if (callFromActionMode) {
                for (int position : selectedList) {
                    FilesInfo filesInfo = Constant.allMemoryVideoList.get(position);
                    selectedFile = filesInfo.getFile().getAbsoluteFile();
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
                    } else {
                        //if result is not successfull that mean file is not deleted so we will remove that file from selectedFile
                        //because at the end we didn't want to remove not selected file from list

                        //removing through IndexOutOfBoundsException
                        //selectedList.remove(position);

                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!filesInfo.getStorageType().equals("/storage/emulated/0")) {

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

                //save the playback history
                Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);

                //check if permission is not available then don't remove sdcard files
                List<UriPermission> permissions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    permissions = getContentResolver().getPersistedUriPermissions();
                    if (permissions == null || permissions.size() == 0){
                        for (int pos : sdCardSelectedList){
                            selectedList.remove(Integer.valueOf(pos));
                        }
                    }
                }


                // Remove Deleted Files from  Constant.allMemoryFileList
                List<FilesInfo> listOfSelectedFiles = new ArrayList<>();
                for (int pos : selectedList) {
                    listOfSelectedFiles.add(Constant.allMemoryVideoList.get(pos));
                }

                for (FilesInfo file : listOfSelectedFiles) {
                    Constant.allMemoryVideoList.remove(file);
                }

                callFromActionMode = false;
                mActionMode.finish();

                adapter.notifyDataSetChanged();
            } else {
                FilesInfo file = Constant.allMemoryVideoList.get(fPosition);
                selectedFile = file.getFile().getAbsoluteFile();

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!file.getStorageType().equals("/storage/emulated/0")) {
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
            }

//            //in api 9 and 8. notifyDataSetChanged not working. So
//            if (Util.SDK_INT < 23)
//                adapter.notifyDataSetChanged();
//            else
//            {
//               onRefresh();
//            }


        }
    }

    /*for < M or M but not external storage file*/
    private void removeSingleFile(){
        boolean result = selectedFile.delete();
        if (!result)
            Toast.makeText(this, "Error: File Not Deleted", Toast.LENGTH_SHORT).show();
        else {
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(selectedFile)));
//                    Methods.removeMedia(this,selectedFile.getPath());

            Constant.allMemoryVideoList.remove(fPosition);

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

            adapter.notifyItemRemoved(fPosition);
        }

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
                    Constant.allMemoryVideoList.remove(fPosition);
                    adapter.notifyItemRemoved(fPosition);
                    deleteSingleFileCall = false;
                }

                /*update the playback record to
                 * getFileName() contain file.getName()*/
                for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                    if ((selectedFile.getName()).equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                        Constant.filesPlaybackHistory.remove(i);
                        //save the playback history

                        Paper.book().write("playbackHistory", Constant.filesPlaybackHistory);
                        break;
                    }
                }

            }
        }
    }

    //DialogListener End

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
                                ActivityCompat.requestPermissions(MainActivity.this,
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission = true;
                }
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    String[] allPath = StorageUtil.getStorageDirectories(this);

                    for (String path: allPath){
                        directory = new File(path);
                        Methods.update_Directory_Files(directory, directory.toString());
                    }
                }
                break;
        }
    }

    private void checkPermissionForStorageAccess() {
        //ContextCompat use to retrieve resources. It provide uniform interface to access resources.
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed to access media file in your phone")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            // Do nothing. Because if permission is already granted then files will be accessed/loaded in splash_screen_activity
        }
    }

    //Permission Request End
}
