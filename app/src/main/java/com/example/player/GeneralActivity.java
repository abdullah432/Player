package com.example.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.player.Modal.Constant;
import com.example.player.Modal.ThemeHelper;

import java.util.Set;

public class GeneralActivity extends AppCompatActivity implements View.OnClickListener{

    Toolbar toolbar;
    SwitchCompat themeBtn;
    RelativeLayout themeLayout;

    SwitchCompat rememberBtn;
    RelativeLayout rememberLayout;

    RelativeLayout removeRememberIcon;
    SwitchCompat removeRememberIconSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);

        initiateComponent();

        setToolbar();

        //initialize theme button
        if (Constant.themePref.equals(ThemeHelper.LIGHT_MODE))
            themeBtn.setChecked(false);
        else
            themeBtn.setChecked(true);

        //intialize remember button
        if (Constant.playbackState == Constant.playbackRecord.YES)
            rememberBtn.setChecked(true);
        else
            rememberBtn.setChecked(false);

        if (Constant.rememberIconState)
            removeRememberIconSwitch.setChecked(false);
        else
            removeRememberIconSwitch.setChecked(true);

    }

    private void initiateComponent() {
        toolbar = findViewById(R.id.toolbar);
        themeBtn = findViewById(R.id.themeBtn);
        themeLayout = findViewById(R.id.themeLayout);
        rememberBtn = findViewById(R.id.rememberBtn);
        rememberLayout = findViewById(R.id.rememberLayout);
        removeRememberIconSwitch = findViewById(R.id.rememberRemoveIconSwitch);
        removeRememberIcon = findViewById(R.id.remove_rememberIconLayout);

        themeBtn.setOnClickListener(this);
        themeLayout.setOnClickListener(this);
        rememberBtn.setOnClickListener(this);
        rememberLayout.setOnClickListener(this);
        removeRememberIcon.setOnClickListener(this);
        removeRememberIconSwitch.setOnClickListener(this);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("General");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor;
        switch (v.getId()) {
            case R.id.themeBtn:
                if (Constant.themePref.equals(ThemeHelper.LIGHT_MODE)) {
                    Constant.themePref = ThemeHelper.DARK_MODE;

                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.DARK_MODE);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(() -> ThemeHelper.applyTheme(ThemeHelper.DARK_MODE), 10);
//                ThemeHelper.applyTheme(ThemeHelper.DARK_MODE);

                    themeBtn.setChecked(true);
                } else {
                    Constant.themePref = ThemeHelper.LIGHT_MODE;

                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.LIGHT_MODE);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(() -> ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE), 1);
//                ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE);
                    themeBtn.setChecked(false);
                }
                break;
            case R.id.themeLayout:
                if (Constant.themePref.equals(ThemeHelper.LIGHT_MODE)) {
                    Constant.themePref = ThemeHelper.DARK_MODE;

                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.DARK_MODE);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(() -> ThemeHelper.applyTheme(ThemeHelper.DARK_MODE), 10);
//                ThemeHelper.applyTheme(ThemeHelper.DARK_MODE);

                    themeBtn.setChecked(true);
                } else {
                    Constant.themePref = ThemeHelper.LIGHT_MODE;

                    editor = getSharedPreferences("THEME", MODE_PRIVATE).edit();
                    editor.putString("themePref", ThemeHelper.LIGHT_MODE);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(() -> ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE), 1);
//                ThemeHelper.applyTheme(ThemeHelper.LIGHT_MODE);
                    themeBtn.setChecked(false);
                }
                break;
            case R.id.rememberBtn:
                if (Constant.playbackState == Constant.playbackRecord.YES){
                    Constant.playbackState = Constant.playbackRecord.NOT;
                    rememberBtn.setChecked(false);

                    Constant.playbackState = Constant.playbackRecord.NOT;

                    editor = getSharedPreferences("PLAYBACK_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("state",false);
                    editor.apply();
                }else {
                    Constant.playbackState = Constant.playbackRecord.YES;
                    rememberBtn.setChecked(true);

                    Constant.playbackState = Constant.playbackRecord.YES;

                    editor = getSharedPreferences("PLAYBACK_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("state",true);
                    editor.apply();
                }
                break;
            case R.id.rememberLayout:
                if (Constant.playbackState == Constant.playbackRecord.YES){
                    Constant.playbackState = Constant.playbackRecord.NOT;
                    rememberBtn.setChecked(false);

                    editor = getSharedPreferences("PLAYBACK_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("state",false);
                    editor.apply();
                }else {
                    Constant.playbackState = Constant.playbackRecord.YES;
                    rememberBtn.setChecked(true);

                    editor = getSharedPreferences("PLAYBACK_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("state",true);
                    editor.apply();
                }
                break;
            case R.id.remove_rememberIconLayout:
                if (Constant.rememberIconState){
                    Constant.rememberIconState = false;
                    removeRememberIconSwitch.setChecked(true);

                    editor = getSharedPreferences("PLAYBACK_ICON_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("playbackIcon",false);
                    editor.apply();
                }else {
                    Constant.rememberIconState = true;
                    removeRememberIconSwitch.setChecked(false);

                    editor = getSharedPreferences("PLAYBACK_ICON_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("playbackIcon",true);
                    editor.apply();
                }
                break;
            case R.id.rememberRemoveIconSwitch:
                if (Constant.rememberIconState){
                    Constant.rememberIconState = false;
                    removeRememberIconSwitch.setChecked(true);

                    editor = getSharedPreferences("PLAYBACK_ICON_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("playbackIcon",false);
                    editor.apply();
                }else {
                    Constant.rememberIconState = true;
                    removeRememberIconSwitch.setChecked(false);

                    editor = getSharedPreferences("PLAYBACK_ICON_STATE",MODE_PRIVATE).edit();
                    editor.putBoolean("playbackIcon",true);
                    editor.apply();
                }
                break;
        }
    }


    protected void onResume() {
        super.onResume();
        overridePendingTransition(0,0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Todo
        //It's a fix. Later find out solution and fix it

        //fix does not work. Error in Mainactivity backpressed

//        finish();
//        startActivity(new Intent(this,MainActivity.class));
    }
}
