package com.example.player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.player.Modal.Methods;
import com.example.player.Modal.StorageUtil;

import java.io.File;

public class Splash_Screen_Activity extends AppCompatActivity {

    private File directory;
    String[] allPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allPath = StorageUtil.getStorageDirectories(this);

        for (String path: allPath){
            directory = new File(path);
            Methods.update_Directory_Files(directory);
        }

//        directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
//        directory = new File("/mnt/");

        Intent intent = new Intent(Splash_Screen_Activity.this, MainActivity.class);
        startActivity(intent);
    }

}
