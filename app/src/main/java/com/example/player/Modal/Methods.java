package com.example.player.Modal;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;

import com.google.android.exoplayer2.C;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Methods {

    private static ArrayList<File> directoryList = new ArrayList<>();
    private static ArrayList<File> directoryFileList = new ArrayList<>();
    private static boolean unique_directory;
    private static String currentDirectory;


    public static int ReadDirectory(File directoryPath)
    {
        File[] files = directoryPath.listFiles();
        String path;
        int count = 0;

        for(int i = 0; i<files.length; i++) {
            for (String ext : Constant.videoExtensions) {
                path = files[i].getPath().toLowerCase();
                if (path.endsWith(ext)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    // Retrieving files from memory
    public static void  update_Directory_Files(File directory) {
        //we can'nt do videoArrayList.clear(). Because method is called inside of method
        //clearing videoArrayList before call from MainActivity.java solve the problem

        //Get all file in storage
        File[] fileList = directory.listFiles();
        //check storage is empty or not
        if(fileList != null && fileList.length > 0)
        {
            for (int i=0; i<fileList.length; i++)
            {
                boolean restricted_directory = false;
                //check file is directory or other file
                if(fileList[i].isDirectory())
                {
                    for (String path : Constant.removePath){
                        if (path.equals(fileList[i].getPath())) {
                            restricted_directory = true;
                            break;
                        }
                    }
                    if (!restricted_directory)
                        update_Directory_Files(fileList[i]);
                }
                else
                {
                    String name = fileList[i].getName().toLowerCase();
                    for (String ext : Constant.videoExtensions){
                        //Check the type of file
                        if(name.endsWith(ext))
                        {
                            //first getVideoDuration
                            String videoDuration = Methods.getVideoDuration(fileList[i]);
                            long playbackPosition;
                            long percentage = C.TIME_UNSET;
                            FilesInfo.fileState state;

                            /*First check video already played or not. If not then state is NEW
                             * else load playback position and calculate percentage of it and assign it*/

                            //check it if already exist or not if yes then start from there else start from start position
                            int existIndex = -1;
                            for (int j = 0; j < Constant.filesPlaybackHistory.size(); j++) {
                                String fListName = fileList[i].getName();
                                String fPlaybackHisName = Constant.filesPlaybackHistory.get(j).getFileName();
                                if (fListName.equals(fPlaybackHisName)) {
                                    existIndex = j;
                                    break;
                                }
                            }

                            try {
                                if (existIndex != -1) {
                                    //if true that means file is not new
                                    state = FilesInfo.fileState.NOT_NEW;
                                    //set playbackPercentage not playbackPosition
                                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                    retriever.setDataSource(fileList[i].getPath());
                                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    retriever.release();

                                    int duration = Integer.parseInt(time);
                                    playbackPosition = Constant.filesPlaybackHistory.get(existIndex).getPlaybackPosition();

                                    if (duration > 0)
                                        percentage = 1000L * playbackPosition / duration;
                                    else
                                        percentage = C.TIME_UNSET;

                                }
                                else
                                    state = FilesInfo.fileState.NEW;

                                //playbackPosition have value in percentage
                                Constant.allMemoryVideoList.add(new FilesInfo(fileList[i],
                                        directory,videoDuration, state, percentage));

                                //directory portion
                                currentDirectory = directory.getPath();
                                unique_directory = true;

                                for(int j=0; j<directoryList.size(); j++)
                                {
                                    if((directoryList.get(j).toString()).equals(currentDirectory)){
                                        unique_directory = false;
                                    }
                                }

                                if(unique_directory){
                                    directoryList.add(directory);
                                }

                                //When we found extension from videoExtension array we will break it.
                                break;

                            }catch (Exception e){
                                e.printStackTrace();
                                Constant.allMemoryVideoList.add(new FilesInfo(fileList[i],
                                        directory,videoDuration, FilesInfo.fileState.NOT_NEW, C.TIME_UNSET));
                            }

                        }
                    }
                }
            }
        }
        Constant.directoryList = directoryList;
//        Constant.allFilesList = videoArrayList;
    }

    public static void  refresh_Directory_Files(File directory) {
        //we can'nt do videoArrayList.clear(). Because method is called inside of method
        //clearing videoArrayList before call from MainActivity.java solve the problem

        //Get all file in storage
        File[] fileList = directory.listFiles();
        //check storage is empty or not
        if(fileList != null && fileList.length > 0)
        {
            for (int i=0; i<fileList.length; i++)
            {
                boolean restricted_directory = false;
                //check file is directory or other file
                if(fileList[i].isDirectory())
                {
                    for (String path : Constant.removePath){
                        if (path.equals(fileList[i].getPath())) {
                            restricted_directory = true;
                            break;
                        }
                    }
                    if (!restricted_directory)
                        refresh_Directory_Files(fileList[i]);
                }
                else
                {
                    String name = fileList[i].getName().toLowerCase();
                    for (String ext : Constant.videoExtensions){
                        //Check the type of file
                        if(name.endsWith(ext))
                        {
                            //if file is already loaded then newFile will become false
                            boolean newFile = true;

                            //first check file is new or already loaded
                            for (FilesInfo file: Constant.allMemoryVideoList){
                                if (file.getFile().equals(fileList[i])){
                                    newFile = false;
                                    break;
                                }
                            }

                            if (newFile){
                                //first getVideoDuration
                                String videoDuration = Methods.getVideoDuration(fileList[i]);
                                long playbackPosition = C.TIME_UNSET;
                                FilesInfo.fileState state;

                                try {
                                    state = FilesInfo.fileState.NEW;

                                    Constant.allMemoryVideoList.add(new FilesInfo(fileList[i],
                                            directory, videoDuration, state, playbackPosition));

                                    //if user open file and then refresh. New file belong to this folder should be added their
                                    if (!Constant.currentFolderFiles.isEmpty()){
                                        if (Constant.currentFolderFiles.get(0).getDirectory().equals(directory)){
                                            Constant.currentFolderFiles.add(new FilesInfo(
                                                    fileList[i],directory,videoDuration,state,playbackPosition
                                            ));
                                        }
                                    }

                                    //directory portion
                                    currentDirectory = directory.getPath();
                                    unique_directory = true;

                                    for (int j = 0; j < directoryList.size(); j++) {
                                        if ((directoryList.get(j).toString()).equals(currentDirectory)) {
                                            unique_directory = false;
                                        }
                                    }

                                    if (unique_directory) {
                                        directoryList.add(directory);
                                    }

                                    //When we found extension from videoExtension array we will break it.
                                    break;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }
            }
        }
        Constant.directoryList = directoryList;
//        Constant.allFilesList = videoArrayList;
    }

    //Retrieve size of both file and folder
    public static String getFolderSizeLabel(File file) {
        double sizeInBytes = getFolderSize(file); // Get size and convert bytes into Kb.
        int sizeInKB = (int) (sizeInBytes / 1024);  //all the other value than GB will be shown without decimal point
        if (sizeInKB >= 1048576){
            double sizeInGB = sizeInBytes / (1024 * 1024 * 1024);
            return Methods.round(sizeInGB, 2) + " GB";
        }
        else if (sizeInKB >= 1024){
            return sizeInKB/1024 + " MB";
        }
        else
            return sizeInKB + " KB";
    }

    //Above method call this method
    static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                size += getFolderSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }

    public static String getVideoDuration(File file){

        try
        {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getPath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilliseconds = Long.parseLong(time);

            retriever.release();

            long hours = (timeInMilliseconds / 1000) / (60 * 60);
            long hoursInMilli = TimeUnit.MILLISECONDS.toHours(timeInMilliseconds);
            if (hours > 0)
                timeInMilliseconds = timeInMilliseconds - hours * (60 * 60 * 1000);
            long minutes = (timeInMilliseconds / 1000) / 60;
            long seconds = (timeInMilliseconds / 1000) % 60;

            if(hours == 0)
                return String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
            else
                return String.format(Locale.getDefault(),"%02d:%02d:%02d",hours,minutes,seconds);
        }
        catch (Exception e){
            e.printStackTrace();
            return "Information not found.";
        }


    }

    public static String getVideoResolution(File file)
    {
        try
        {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getPath());
            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int rotation = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            }
            retriever.release();

            if(rotation != 0)
            {
                if(rotation == 90 || rotation == 270)
                {
                    int swipe = width;
                    width = height;
                    height = swipe;
                }
            }

            return width + " x " + height;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Information not found.";
        }
    }

    public static String getVideoFormat(File file){
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
            retriever.setDataSource(inputStream.getFD());
            String format = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            retriever.release();
            return format;
        }
        catch (Exception e){
            e.printStackTrace();
            return "Information not found.";
        }

    }

    public static int getFolderMediaFileCount(File filePath){
        File[] file = filePath.listFiles();
        int count = 0;
        for (int i=0; i<file.length; i++){
            for (String extensionType : Constant.videoExtensions) {
                if (file[i].getPath().endsWith(extensionType))
                    count++;
            }
        }
        return count;
    }

    public static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


    public static void removeMedia(final Context c, String path) {
        try {
            MediaScannerConnection.scanFile(c, new String[] { path },
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            c.getContentResolver()
                                    .delete(uri, null, null);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSizeOfSelectedFile(File selectedDirectory)
    {
        int count = 0;
        for (FilesInfo file : Constant.allMemoryVideoList)
        {
            if (file.getDirectory().equals(selectedDirectory))
                count ++;
        }

        return count;
    }


}
