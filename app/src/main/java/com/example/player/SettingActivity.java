package com.example.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    LinearLayout general_setting;
    LinearLayout player_setting;
    LinearLayout about_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initComponents();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initComponents() {
        toolbar = findViewById(R.id.toolbar);
        general_setting = findViewById(R.id.general_setting);
        player_setting = findViewById(R.id.player_setting);
        about_setting = findViewById(R.id.about_setting);

        general_setting.setOnClickListener(this);
        player_setting.setOnClickListener(this);
        about_setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.general_setting:
                Intent intent = new Intent(SettingActivity.this, GeneralActivity.class);
                startActivity(intent);
            break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
