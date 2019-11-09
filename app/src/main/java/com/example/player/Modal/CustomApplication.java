package com.example.player.Modal;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.player.MainActivity;
import com.example.player.RecyclerViewAdapter;

import java.util.ArrayList;

import io.paperdb.Paper;

public class CustomApplication extends Application {

    SharedPreferences sharedPreferences;

    public void onCreate() {
        super.onCreate();
        sharedPreferences =
                getSharedPreferences("THEME",MODE_PRIVATE);

        String themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE);
        Constant.themePref = themePref;
        ThemeHelper.applyTheme(themePref);

        sharedPreferences = getSharedPreferences("PLAYBACK_STATE", MODE_PRIVATE);
        boolean state = sharedPreferences.getBoolean("state",true);
        if (state) {
            Constant.playbackState = Constant.playbackRecord.YES;
        }
        else
            Constant.playbackState = Constant.playbackRecord.NOT;

        Paper.init(this);
        Constant.filesPlaybackHistory = Paper.book().read("playbackHistory",new ArrayList<>());

        //remember playback icon state
        sharedPreferences = getSharedPreferences("PLAYBACK_ICON_STATE",MODE_PRIVATE);
        Constant.rememberIconState = sharedPreferences.getBoolean("playbackIcon",true);
    }

}