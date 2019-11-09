package com.example.player.Modal;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Constant implements Serializable {

    public static int SORT_TYPE;
    public static String themePref;
    public static ArrayList<FilesPlaybackHistory> filesPlaybackHistory;

    /*Store the name of selectedDirectory in static variable so we can access it from videoPlayerActivity*/
    public static File selectedDirecotory;

    //it is constant because. We need to access it during onBindHolder and During Next, Previous etc functionality
    //user can change it to NO. NO: mean user did'nt want to save playback history
    public static enum playbackRecord{
        YES,
        NOT
    }
    public static playbackRecord playbackState;

    //playback icon state for icon show or hide
    public static boolean rememberIconState;

    //all the directory that contains files
    public static ArrayList<File> directoryList = null;
    //Files of directory that is currently open
    public static ArrayList<FilesInfo> allMemoryVideoList = new ArrayList<>();
    //when user will clicked on directory. Files of that directory will assign to this ArrayList
    public static ArrayList<FilesInfo> currentFolderFiles = new ArrayList<>();

    public static ArrayList<FilesInfo> suggestionData = null;

    public static String[] videoExtensions = {".mp4",".ts",".mkv",".mov",".3gp",".mv2",".m4v",".webm",".mpeg1",".mpeg2",".mts",".ogm",".bup",
            ".dv",".flv",".m1v",".m2ts",".mpeg4",".vlc",".3g2",".avi",".mpeg",".mpg",".wmv",".asf"};

    public static String[] removePath = {"/storage/emulated/0/Android/data","/storage/emulated/0/Android/obb"};

    public static void removeFolderFromDirectoryList(File file){
        directoryList.remove(file);
    }

    public static void addFolderToDirectoryList(File file){
        directoryList.add(file);
    }

}
