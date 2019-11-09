package com.example.player.MyDialog;

import java.io.File;

public interface DialogsListener {
    void renameFile(File directory, String previousName, String changeName);
    void deleteFile();
}
