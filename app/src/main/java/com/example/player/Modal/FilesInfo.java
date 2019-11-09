package com.example.player.Modal;

import java.io.File;

public class FilesInfo {
    public enum  fileState
    {
        NEW,
        NOT_NEW
    }

    private File file;
    private File directory;
    private String videoDuration;
    private fileState state;
    private long playbackPercentage;

    public FilesInfo(File file, File directory, String videoDuration, fileState state, long playbackPercentage) {
        this.file = file;
        this.directory = directory;
        this.videoDuration = videoDuration;
        this.state = state;
        this.playbackPercentage = playbackPercentage;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public fileState getState() {
        return state;
    }

    public void setState(fileState state) {
        this.state = state;
    }

    public long getPlaybackPercentage() {
        return playbackPercentage;
    }

    public void setPlaybackPercentage(long playbackPercentage) {
        this.playbackPercentage = playbackPercentage;
    }
}
