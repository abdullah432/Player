package com.example.player.Modal;

public class FilesPlaybackHistory {
   /* current Tag value will be assign to fileName
   *  Tag have file.getName() value*/
    private String fileName;
    private long playbackPosition;

    public FilesPlaybackHistory(String fileName, long playbackPercentage){
        this.fileName = fileName;
        this.playbackPosition = playbackPercentage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(long playbackPosition) {
        this.playbackPosition = playbackPosition;
    }

}
