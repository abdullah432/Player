package com.example.player;

public interface OnFileListener {
    void onVideoClick(int position);
    void onIconMoreClick(int position);
    boolean onVideoLongClick(int position);
}