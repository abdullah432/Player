package com.example.player;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.player.Modal.Constant;
import com.example.player.Modal.FilesInfo;
import com.example.player.Modal.FilesPlaybackHistory;
import com.example.player.Modal.TrackSelectionDialog;
import com.example.player.Modal.TrackSelectionV;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener, TrackSelectionV.onOkListener {

    ExoPlayer player;
    PlayerView playerView;

    //Root Start
    LinearLayout root;              //root contains media controller view   (Not include: volume, brightness bar etc)
    //Top Root
    LinearLayout rootTopBar;
    ImageButton backBtn;
    TextView videoTitle;
    TextView tracks;
    //Root Middle
    ImageButton rotateBtn;
    ImageButton mode_change_btn;
    //Root Bottom seekBar
    LinearLayout seekBarLayout;
    TextView currentTime;
    SeekBar seekBar;
    TextView totalDuration;
    //Root Bottom controls
    LinearLayout controlsLayout;
    ImageButton lockBtn;
    ImageButton previousBtn;
    ImageButton playBtn;
    ImageButton pauseBtn;
    ImageButton nextBtn;
    ImageButton resizeBtn;
    //Root End

    //Loading Bar Layout
    RelativeLayout loadingBarLayout;

    //Volume Progress Bar
    LinearLayout volumeProgressBarContainer;
    ProgressBar volumeSlider;

    //Brightness Progress Bar
    LinearLayout brightnessProgressBarContainer;
    ProgressBar brightnessSlider;

    //Unlock Panel
    LinearLayout unlockPanel;
    ImageButton unlockBtn;

    //Volume Icon in Screen Mid
    LinearLayout volumeIcon_Text_ScreenMid_layout;
    TextView volumeMidText;
    ImageView volumeMideIcon;

    //Brightness Icon in Screen Mid
    LinearLayout brightness_center_text_icon_layout;
    TextView brightnessMidText;
    ImageView brightnessMideIcon;

    //Center seek scroll
    LinearLayout centerSeekTextLayout;
    TextView currentSeekTime;
    TextView seekSec;

    //Resize center Text
    LinearLayout resizeLayout;
    TextView resizeTxt;

    View decorView;
    boolean mShowing = false;
    Handler mHandler;
    boolean mDragging;

    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private AccessibilityManager mAccessibilityManager;
    private int sDefaultTimeout = 5000;

    //Media
    MediaSource mediaSource;
    private int videoPosition;
    ConcatenatingMediaSource concatenatingMediaSource;
//    private ExtractorMediaSource.Factory mediaSourceFactory;
    private Timeline.Window windowTimeline;
    DefaultTrackSelector trackSelector;

    //Store Info
//    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor editor;
    int RESIZE_TYPE;    // 1 : BEST FIT, 2 : FILL, 3 : CENTER, 4 : (4:3 SIZE), 5 : (16:9), 6 : CROP

    //Volume and Brightness UP and Down
    private Display display;
    private Point size;
    private int sWidth;
    private int sHeight;
    private String seekDur;
    private ContentResolver cResolver;
    private Window window;
    private int calculatedTime;
    private int mediavolume,device_height,device_width;
    private AudioManager audioManager;
    boolean seekDetect;
    boolean volumeDetect;
    boolean brightnessDetect;
    float DownX1;      //Down X Position
    float DownY1;      //Down Y Position
    float DownX2;      //Move X Position
    float DownY2;      //Move Y Position
    long DiffX;
    long DiffY;
    static final int MINIMUM_DISTANCE_COVER = 100;
    private double SEEKSPEED = 0.2;
    private double T = 60000;
    double BRIGHTNESS_SPEED = 0.02;
    private boolean leftSide;
    private boolean rightSide;
    private double percentage;

    //Lock and Unlock feature
    public enum ControlsMode {
        LOCK, FULLCONTORLS
    }
    private ControlsMode controlsState;

    private float distanceCovered = 0;
    private AudioManager audio;
    private int currentVolume;
    private int maxVolume;
    private double volper;
    private double per;
    private float brightness;
    private float seekdistance = 0;

    private boolean pauseState = false;

    //Track Selection
    private boolean setTrackbyUser;
    private TrackGroupArray audioTrackGroupArray;
    private TrackGroupArray trackGroups;
    private boolean isAudioDisable;
    private @Nullable
    DefaultTrackSelector.SelectionOverride override;
    int rendererIndexAudio = 1; // renderer for audio
    int rendererIndexSubtitle = 3; // renderer for audio
    MappingTrackSelector.MappedTrackInfo mappedTrackInfo;
    private boolean isShowingTrackSelectionDialog;

    //Restoring Playback Position
    // autoplay = false
    private boolean autoPlay = false;

    // used to remember the playback position
    private int currentWindow;
    private long playbackPosition;

    // constant fields for saving and restoring bundle
    public static final String AUTOPLAY = "autoplay";
    public static final String CURRENT_WINDOW_INDEX = "current_window_index";
    public static final String PLAYBACK_POSITION = "playback_position";

    private final int REGULARTYPE = 0;
    private final int SEARCHTYPE = 1;
    private int dataType;
    private boolean searchDataType = false;

    //Mode: shuffle, repeat etc
    public enum playerMode{
        REGULAR,
        SHUFFLE,
        REPEAT_LIST,
        REPEAT_ONE,
    }

    playerMode playerState = playerMode.REGULAR;

    //PlayBack Position
    String currentTag;
    //if STATE_ENDED then playback position should be C.TIME_UNSET
    boolean callFromStateEnded = false;

    //Toast
    Toast toast;

    IntentFilter intentFilter;
    BecomingNoisyReceiver myNoisyAudioStreamReceiver;

    //save brightness
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                seekDetect = false;
                volumeDetect = false;
                brightnessDetect = false;

                getScreenSize();

                if (event.getX() < (sWidth / 2)) {
                    leftSide = true;
                    rightSide = false;
                } else if (event.getX() > (sWidth / 2)) {
                    leftSide = false;
                    rightSide = true;
                }

                seekdistance = 0;
                distanceCovered = 0;

                DownX1 = event.getX();
                DownY1 = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                //finger move to screen
                DownX2 = event.getX();
                DownY2 = event.getY();

                distanceCovered = getDistance(DownX2, DownY2, event);

                if(controlsState == ControlsMode.FULLCONTORLS){
                    root.setVisibility(View.GONE);

                    if (!volumeDetect && !brightnessDetect && !seekDetect) {
                        DiffX = (long) Math.ceil(DownX2 - DownX1);
                        DiffY = (long) Math.ceil(DownY2 - DownY1);

                        if (Math.abs(DiffX) > MINIMUM_DISTANCE_COVER || Math.
                                abs(DiffY) > MINIMUM_DISTANCE_COVER) {
                            if (Math.abs(DiffY) > Math.abs(DiffX)) {
                                if (leftSide){
                                    brightnessDetect = true;
//                                    changeBrightness(event);
                                }
                                else if (rightSide){
                                    volumeDetect = true;
                                }
                            } else if (Math.abs(DiffX) > Math.abs(DiffY)) {
                                seekDetect = true;
//                                changeSeekPosition();
                            }
                        }
                    }
                    else {
                        try{
                            if (volumeDetect)
                                changeVolume( event.getHistoricalY(0, 0), DownY2, distanceCovered);
                            else if (brightnessDetect)
                                changeBrightness(event.getHistoricalY(0,0),DownY2, distanceCovered);
                            else if (seekDetect) {
                                //When we detect seek, we will stop the player
                                player.setPlayWhenReady(false);

                                setProgress();
                                changeSeek(event.getHistoricalX(0, 0), DownX2, distanceCovered);
                            }
                        }catch (Exception e){

                        }
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (seekDetect || volumeDetect || brightnessDetect){
                    centerSeekTextLayout.setVisibility(View.GONE);
                    brightness_center_text_icon_layout.setVisibility(View.GONE);
                    volumeIcon_Text_ScreenMid_layout.setVisibility(View.GONE);
                    brightnessProgressBarContainer.setVisibility(View.GONE);
                    volumeProgressBarContainer.setVisibility(View.GONE);
                    seekBarLayout.setVisibility(View.VISIBLE);
                    rootTopBar.setVisibility(View.VISIBLE);
                    controlsLayout.setVisibility(View.VISIBLE);
                    root.setVisibility(View.GONE);
                    if (pauseState)
                        player.setPlayWhenReady(false);
                    else {
                        player.setPlayWhenReady(true);
                    }
                }else {

                    if (!mShowing)
                        show(sDefaultTimeout);
                    else if (mShowing) {
                        hide();
                        mShowing = false;
                    }
                }

                break;

        }
        return super.onTouchEvent(event);
    }

    ///THIS METHOD FOR GET SCREEN SIZE
    /// USE FOR DETECT SCREEN SIDE

    private void getScreenSize() {
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        device_height = displaymetrics.heightPixels;
        device_width = displaymetrics.widthPixels;
    }


    private float getDistance(float startX, float startY, MotionEvent ev) {
        float distanceSum = 0;
        final int historySize = ev.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            float hx = ev.getHistoricalX(0, h);
            float hy = ev.getHistoricalY(0, h);
            float dx = (hx - startX);
            float dy = (hy - startY);
            distanceSum += Math.sqrt(dx * dx + dy * dy);
            startX = hx;
            startY = hy;
        }
        float dx = (ev.getX(0) - startX);
        float dy = (ev.getY(0) - startY);
        distanceSum += Math.sqrt(dx * dx + dy * dy);
        return distanceSum;
    }

    private void changeBrightness(float Y, float y, float distance) {
            distance = distance / 270;
            if (y < Y) {
                commonBrightness(distance);
            } else {
                commonBrightness(-distance);
            }
    }
    private void commonBrightness(float distance) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();

        double brightness = (getWindow().getAttributes().screenBrightness + distance ) * 100;
        if (brightness >= 100)
            brightness = 100;
        else if (brightness <= 0)
            brightness = 0;

            brightnessProgressBarContainer.setVisibility(View.VISIBLE);
            brightness_center_text_icon_layout.setVisibility(View.VISIBLE);
            if ((int) brightness > 100) {
                brightnessSlider.setProgress(100);
                brightnessMidText.setText("100");
                brightnessMideIcon.setImageResource(R.drawable.ic_brightness_high_white_35dp);
            } else if ((int) brightness  < 0) {
                brightnessSlider.setProgress(0);
                brightnessMidText.setText("0");
                brightnessMideIcon.setImageResource(R.drawable.ic_brightness_low_white_35dp);
            } else {
                brightnessSlider.setProgress((int) ((getWindow().getAttributes().screenBrightness + distance) * 100));
                brightnessMidText.setText(Integer.valueOf((int) brightness).toString());
                brightnessMideIcon.setImageResource(R.drawable.ic_brightness_medium_white_35dp);
            }

            layout.screenBrightness = getWindow().getAttributes().screenBrightness + distance;
            if (layout.screenBrightness >= 1)
                layout.screenBrightness = 1;
            else if (layout.screenBrightness <= 0)
                layout.screenBrightness = 0;

            Log.i("Birghtness: ", String.valueOf(layout.screenBrightness));

            getWindow().setAttributes(layout);

            editor = getSharedPreferences("brightness",MODE_PRIVATE).edit();
            editor.putFloat("preferences", layout.screenBrightness);
            editor.apply();

    }

    private void changeSeek(float X, float x, float distance){
        distance = distance / 600;
        if (x > X) {
            seekCommon(distance);
        } else {
            seekCommon(-distance);
        }
    }

    public void seekCommon(float distance) {
        seekdistance += ((distance * T));   //calculated time

        root.setVisibility(View.VISIBLE);
        centerSeekTextLayout.setVisibility(View.VISIBLE);
        seekBarLayout.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.GONE);
        rootTopBar.setVisibility(View.GONE);
        String totime = "";

        if (player != null) {
            Log.e("after", player.getCurrentPosition() + (int) (distance * T) + "");
            Log.e("seek distance", (int) (seekdistance) + "");

            //Test Start
            String NextPosition = player.getContentPosition() + (int) distance * T + "";
            float seekDistance = seekdistance;

            //Test End

            if (player.getCurrentPosition() + (int) (distance * 60000) > 0 && player.getCurrentPosition() + (int) (distance * T) < player.getDuration() + 10) {
                player.seekTo(player.getCurrentPosition() + (int) (distance * T));
//                seekDur = "[ "+ Math.abs((int) (seekdistance / 60000)) + ":" + String.valueOf(Math.abs((int) ((seekdistance) % 60000))).substring(0, 2) +" ]";
//                totime = "[" + (int) ((player.getCurrentPosition() + (int) (distance * T)) / T) + ":" + String.valueOf((int) ((player.getCurrentPosition() + (int) (distance * T)) % (T))).substring(0, 2) + "]";
                if (seekdistance > 0) {
//                    seekDur = "+" + Math.abs((int) (seekdistance / T)) + ":" + String.valueOf(Math.abs((int) ((seekdistance) % T))).substring(0, 2);

                    seekDur = String.format(Locale.getDefault(),"[ +%02d:%02d ]",
                            TimeUnit.MILLISECONDS.toMinutes((long) seekdistance) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) seekdistance)),
                            TimeUnit.MILLISECONDS.toSeconds((long) seekdistance) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) seekdistance)));
                } else{
//                    seekDur = "-" + Math.abs((int) (seekdistance / T)) + ":" + String.valueOf(Math.abs((int) ((seekdistance) % T))).substring(0, 2);
                    seekDur = String.format(Locale.getDefault(),"[ -%02d:%02d ]",
                            Math.abs(TimeUnit.MILLISECONDS.toMinutes((long) seekdistance) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) seekdistance))),
                            Math.abs(TimeUnit.MILLISECONDS.toSeconds((long) seekdistance) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) seekdistance))));
                }

                totime = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) (player.getCurrentPosition() + (distance * T))) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) (player.getCurrentPosition() + (distance * T)))), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds((long) (player.getCurrentPosition() + (distance * T))) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) (player.getCurrentPosition() + (distance * T)))));

                seekSec.setText(seekDur);

                currentSeekTime.setText(totime);

                seekBar.setProgress(Integer.parseInt(player.getCurrentPosition() + (seekDur)));
            }
        }
    }

    private void changeVolume(float Y, float y, float distance) {
            if (y < Y) {
                distance = distance / 65;
                commonVolume(distance);
            } else {
                distance = distance / 680;
                commonVolume(-distance);
            }
    }

    private void commonVolume(float distance) {

//        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        per = (double) currentVolume / (double) maxVolume;
        Log.e("per", per + "");
        Log.e("distance: ",String.valueOf(distance));

        volumeProgressBarContainer.setVisibility(View.VISIBLE);
        volumeIcon_Text_ScreenMid_layout.setVisibility(View.VISIBLE);

        if (distance > 0.05 || distance < -0.01) {

            int newVolume = (int) ((per + ((double)distance)) * 15);

            if (newVolume > 15)
                newVolume = 15;
            else if (newVolume < 1)
                newVolume = 0;

            Log.i("Volume: ", String.valueOf(newVolume));

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            if (newVolume < 1) {
                volumeSlider.setProgress(0);
                volumeMidText.setVisibility(View.GONE);
                volumeMideIcon.setImageResource(R.drawable.ic_volume_mute_whire_35dp);
            } else if (newVolume >= 1 && newVolume < 12) {
                volumeSlider.setProgress(newVolume);
                volumeMidText.setVisibility(View.VISIBLE);
                volumeMidText.setText(String.valueOf(newVolume));
                volumeMideIcon.setImageResource(R.drawable.ic_volume_down_white_35dp);
            } else if (newVolume >= 12) {
                volumeSlider.setProgress(newVolume);
                volumeMidText.setText(String.valueOf(newVolume));
                volumeMideIcon.setImageResource(R.drawable.ic_volume_full_white_35dp);
            }

            editor = getSharedPreferences("volume",MODE_PRIVATE).edit();
            editor.putInt("volumePref",newVolume);
            editor.apply();

        }

//        if (per + distance <= 1 && per + distance >= 0) {
//            if (distance > 0.05 || distance < -0.05) {
////                volumeSlider.setProgress((int) ((per + distance)));
////                volumeMidText.setText((int) ((per + distance)) + "");
////                volumeMidText.setText(String.valueOf(per + distance));
//                volumeSlider.setProgress((int) ((per + distance) * 100));
//                volumeMidText.setText((int) ((per + distance) * 100) + "%");
////                volumeMidText.setText(String.valueOf(currentVolume));
//                volper = (per + (double) distance);
//                audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (volper * 15), 0);
//            }
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        //for headphone removal etc
        intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

        //Should be on MainActivity
        Paper.init(this);

        //videoPosition is position of allMemoryVideoList item
        videoPosition = getIntent().getIntExtra("videoPosition",-1);
        dataType = getIntent().getIntExtra("DATA_TYPE",0);
        //Full Screen Display
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        statusBarBackChange();

        initializeComponents();
        hideSystemUI();

        controlsState = ControlsMode.FULLCONTORLS;

        // if we have saved player state, restore it
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION);
            currentWindow = savedInstanceState.getInt(CURRENT_WINDOW_INDEX);
            autoPlay = savedInstanceState.getBoolean(AUTOPLAY);
        }
        else {
//            clearStartPosition();
            playSelectedVideo();
        }


    }

    private void statusBarBackChange() {
        //Todo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private void initializeComponents() {
        playerView = findViewById(R.id.player_view);

        //Root Start
        root = findViewById(R.id.root);
        //Top Root
        rootTopBar = findViewById(R.id.topRoot);
        backBtn = findViewById(R.id.btn_back);
        videoTitle = findViewById(R.id.txt_title);
        mode_change_btn = findViewById(R.id.mode_change_btn);
        tracks = findViewById(R.id.tracks);
        //Root Middle
        rotateBtn = findViewById(R.id.rotateBtn);
        //Root Bottom seekBar
        seekBarLayout = findViewById(R.id.seekbar_time);
        currentTime = findViewById(R.id.txt_currentTime);
        seekBar = findViewById(R.id.seekbar);
        if (seekBar != null){
            seekBar.setOnSeekBarChangeListener(mSeekBarListener);
            seekBar.setMax(1000);
        }
        totalDuration = findViewById(R.id.txt_totalDuration);
        //RootBottom Controls
        controlsLayout = findViewById(R.id.controls);
        lockBtn = findViewById(R.id.btn_lock);
        previousBtn = findViewById(R.id.btn_prev);
        playBtn = findViewById(R.id.btn_play);
        pauseBtn = findViewById(R.id.btn_pause);
        nextBtn = findViewById(R.id.btn_next);
        resizeBtn = findViewById(R.id.btn_resize);

        //Loading Bar Layout
        loadingBarLayout = findViewById(R.id.loadingBarLayout);

        //Volume Progress Bar
        volumeProgressBarContainer = findViewById(R.id.volume_slider_container);
        volumeSlider = findViewById(R.id.volume_slider);

        //Brightness Progress Bar
        brightnessProgressBarContainer = findViewById(R.id.brightness_slider_container);
        brightnessSlider = findViewById(R.id.brightness_slider);

        //Unlock Panel
        unlockPanel = findViewById(R.id.unlock_panel);
        unlockBtn = findViewById(R.id.btn_unlock);

        //Volume Icon in Screen Mid
        volumeIcon_Text_ScreenMid_layout = findViewById(R.id.vol_center_text_layout);
        volumeMidText = findViewById(R.id.vol_perc_center_text);
        volumeMideIcon = findViewById(R.id.vol_image);

        //Brightness Icon in Screen Mid
        brightness_center_text_icon_layout = findViewById(R.id.brightness_center_text_icon_layout);
        brightnessMidText = findViewById(R.id.brigtness_perc_center_text);
        brightnessMideIcon = findViewById(R.id.brightness_image);

        //Center seek scroll
        centerSeekTextLayout = findViewById(R.id.seekbar_center_text_layout);
        currentSeekTime = findViewById(R.id.seek_currTime);
        seekSec = findViewById(R.id.seek_secs_increase);

        //Resize layout
        resizeLayout = findViewById(R.id.resizeLayout);
        resizeTxt = findViewById(R.id.resizeTxt);

        //Handler
        mHandler = new Handler();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mAccessibilityManager = (AccessibilityManager) this.getSystemService(ACCESSIBILITY_SERVICE);

        windowTimeline = new Timeline.Window();

        concatenatingMediaSource = new ConcatenatingMediaSource();

//        sharedPreferences = getSharedPreferences("RESIZE_PREFERENCES",MODE_PRIVATE );
//        RESIZE_TYPE = sharedPreferences.getInt("RESIZE_TYPE", 1);

        //BtnClickListener
        backBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        lockBtn.setOnClickListener(this);
        resizeBtn.setOnClickListener(this);
        tracks.setOnClickListener(this);
        unlockBtn.setOnClickListener(this);
        rotateBtn.setOnClickListener(this);
        mode_change_btn.setOnClickListener(this);

        //get Save Brightness
        preferences = getSharedPreferences("brightness", MODE_PRIVATE);
        brightness = preferences.getFloat("preferences",
                android.provider.Settings.System.getFloat(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1));
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = brightness;
        getWindow().setAttributes(layout);

        //get Save Volume
        preferences = getSharedPreferences("volume",MODE_PRIVATE);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = preferences.getInt("volumePref", audio.getStreamVolume(AudioManager.STREAM_MUSIC));

//        brightness = android.provider.Settings.System.getFloat(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1);
//        WindowManager.LayoutParams layout = getWindow().getAttributes();
//        layout.screenBrightness = brightness / 255;
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                             View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                // Set the content to appear under the system bars so that the
                                // content doesn't resize when the system bars hide and show.
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                // Hide the nav bar and status bar
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            decorView = getWindow().getDecorView();
            // Show Status Bar.
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            );
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = isPlaying();
        if (!playing && playBtn != null) {
            playBtn.requestFocus();
        } else if (playing && pauseBtn != null) {
            pauseBtn.requestFocus();
        }
    }

    private void hide() {
        if (playerView == null)
            return;

        if (controlsState == ControlsMode.LOCK){
            unlockPanel.setVisibility(View.GONE);
            mShowing = false;
        }else {
            if (mShowing && !pauseState){
                mHandler.removeCallbacks(mShowProgress);
                root.setVisibility(View.GONE);
                mShowing = false;

                hideSystemUI();
            }
        }

    }

    private void show(int timeout) {
        if (controlsState == ControlsMode.LOCK && !mShowing) {
            mShowing = true;
            unlockPanel.setVisibility(View.VISIBLE);
        }else {
            if (!mShowing && root != null) {
                setProgress();
                requestPlayPauseFocus();
                root.setVisibility(View.VISIBLE);
                mShowing = true;
            }
            updatePausePlay();
            updatePreNext();

            showSystemUI();

            // cause the progress bar to be updated even if mShowing
            // was already true.  This happens, for example, if we're
            // paused with the progress bar showing the user hits play.
            mHandler.post(mShowProgress);
        }

        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled()) {
            mHandler.removeCallbacks(mFadeOut);
            mHandler.postDelayed(mFadeOut, timeout);
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && isPlaying()) {
                mHandler.postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (player == null || mDragging) {
            return 0;
        }
        int position = (int) player.getCurrentPosition();
        int duration = (int) player.getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                seekBar.setProgress( (int) pos);
            }
//            int percent = player.getBufferedPercentage();
//            seekBar.setSecondaryProgress(percent * 10);
        }

        if (totalDuration != null)
            totalDuration.setText(stringForTime(duration));
        if (currentTime != null)
            currentTime.setText(stringForTime(position));

        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(player == null)
                return;

            if (!fromUser)
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;

            long duration = player.getDuration();
            long newPosition = (duration * progress) / 1000L;

            player.seekTo(newPosition);

            if (currentTime != null)
                currentTime.setText(stringForTime((int) newPosition));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeCallbacks(mShowProgress);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            setProgress();
//            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.post(mShowProgress);
        }
    };

    private boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    void releasePlayer(){
        if(player != null){
            updateStartPosition();
            playerView.setPlayer(null);
            player.release();
            player = null;
            mediaSource = null;
            trackSelector = null;
        }
    }

    private void initializePlayer() {

        trackSelector = new DefaultTrackSelector();   //
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

//        override = new DefaultTrackSelector.SelectionOverride(0,0);

        playerView.setPlayer(player);
        player.addListener(new PlayerEventListener());

        // resume playback position
//        boolean haveStartPosition = currentWindow != C.INDEX_UNSET;
//        if (haveStartPosition) {
//            player.seekTo(currentWindow, playbackPosition);
//        }

        //This method make concatenating mediaSource of selected folder files or allfileslist

        buildMediaSource();

        player.prepare(concatenatingMediaSource);

//        playSelectedVideo();

        player.seekTo(currentWindow, playbackPosition);

        //if selected video is played then currentWindow is initialized in playSelectedVideo else in updateStartPosition
        rotateScreen(currentWindow);
//        player.prepare(concatenatingMediaSource, !haveStartPosition, false);

//        MappingTrackSelector.MappedTrackInfo trackInfo =
//                trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
//
//        if (trackInfo != null) {
//            trackGroups = trackInfo.getTrackGroups(rendererIndex);
//            TrackSelectionV.override = new DefaultTrackSelector.SelectionOverride(1, 0);
//            applySelection();
//        }

        player.setPlayWhenReady(autoPlay);
    }

    // save app state before all members are gone forever :D
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
         * A simple configuration change such as screen rotation will destroy this activity
         * so we'll save the player state here in a bundle (that we can later access in onCreate) before everything is lost
         * NOTE: we cannot save player state in onDestroy like we did in onPause and onStop
         * the reason being our activity will be recreated from scratch and we would have lost all members (e.g. variables, objects) of this activity
         */
        updateStartPosition();
        if (player == null) {
            outState.putLong(PLAYBACK_POSITION, playbackPosition);
            outState.putInt(CURRENT_WINDOW_INDEX, currentWindow);
            outState.putBoolean(AUTOPLAY, autoPlay);
        }
    }
    private void updateStartPosition() {
        if (player != null) {
            autoPlay = player.getPlayWhenReady();
            currentWindow = player.getCurrentWindowIndex();
            playbackPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void clearStartPosition() {
        autoPlay = true;
        currentWindow = C.INDEX_UNSET;
        playbackPosition = C.TIME_UNSET;
    }

    void buildMediaSource(){
        //        DefaultDataSourceFactory defaultDataSourceFactory =
//                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo-demo"));
//
//        mediaSourceFactory = new ExtractorMediaSource.Factory(defaultDataSourceFactory);

        ArrayList<File> mediaSourc = new ArrayList<>();
        if (dataType == SEARCHTYPE) {
            for (FilesInfo file : Constant.suggestionData)
            {
                mediaSourc.add(file.getFile());
            }
        }
        else        //regulartype
            {
            if (Constant.SORT_TYPE == 1) {
                for (FilesInfo file : Constant.allMemoryVideoList)
                {
                    if (file.getDirectory().equals(Constant.selectedDirecotory))
                        mediaSourc.add(file.getFile());
                }
            } else{
                for (FilesInfo file : Constant.allMemoryVideoList)
                {
                    mediaSourc.add(file.getFile());
                }
            }

        }
//        MediaSource[] mediaSources = new MediaSource[mediaSourc.size()];
        for (int i=0;i<mediaSourc.size();i++)
        {
            addMediaSource(mediaSourc.get(i));
//            mediaSources[i]= mediaSourceFactory
//                    .setTag(mediaSourc.get(i).getName())
//                    .createMediaSource(Uri.fromFile(mediaSourc.get(i)));
        }

//        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
//                : new ConcatenatingMediaSource(mediaSources);
//        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);

//        File file = Constant.currentFolderFiles.get(videoPosition);
//        playSelectedVideo(file);
    }

    private void addMediaSource(File file) {
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        extractorsFactory.setConstantBitrateSeekingEnabled(true);

        DefaultDataSourceFactory dataSourceFactory = new
                DefaultDataSourceFactory(this,Util.getUserAgent(this,"exo-player"));

    // Create a progressive media source pointing to a stream uri.
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory,extractorsFactory)
                .setTag(file.getName())
                .createMediaSource(Uri.fromFile(file));

//        // Add mediaId (e.g. uri) as tag to the MediaSource.
//        ExtractorMediaSource.Factory extractorMediaSource =
//                new ExtractorMediaSource.Factory(dataSourceFactory);

//        mediaSource = extractorMediaSource
//                .setTag(file.getName())
//                .setExtractorsFactory(extractorsFactory)
//                .createMediaSource(Uri.fromFile(file));

                concatenatingMediaSource.addMediaSource(mediaSource);
    }


    private void playSelectedVideo() {
//        rotateScreen(videoPosition);
//
//        player.seekTo(videoPosition,C.TIME_UNSET);
//
        String title;
        if (dataType == SEARCHTYPE){
            title = Constant.suggestionData.get(videoPosition).getFile().getName();
        }else {     //regular type
            if (Constant.SORT_TYPE == 1)
                title = Constant.currentFolderFiles.get(videoPosition).getFile().getName();
            else
                title = Constant.allMemoryVideoList.get(videoPosition).getFile().getName();
        }
        videoTitle.setText(title);

        //first check playback state
        if (Constant.playbackState == Constant.playbackRecord.YES) {

            //check it if already exist or not if yes then start from there else start from start position
            int existIndex = -1;
            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                if (title.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                    existIndex = i;
                    break;
                }
            }

            if (existIndex != -1) {
                currentWindow = videoPosition;
                playbackPosition = Constant.filesPlaybackHistory.get(existIndex).getPlaybackPosition();
                autoPlay = true;
            } else {
                currentWindow = videoPosition;
                playbackPosition = C.TIME_UNSET;
                autoPlay = true;
            }
        }
        else {
            currentWindow = videoPosition;
            playbackPosition = C.TIME_UNSET;
            autoPlay = true;
        }
    }

    private void playNextVideo() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty())
            return;

        //Method will first check if user allow to remember playback position or not
        int currentWindowIndex = player.getCurrentWindowIndex();
        updatePlaybackPosition(currentWindowIndex);

        //move to next video
        int nextWindowIndex = player.getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET){

            player.seekTo(nextWindowIndex, C.TIME_UNSET);
            //get next video tag
            currentTag = player.getCurrentTag().toString();
            //first check playback state
            if (Constant.playbackState == Constant.playbackRecord.YES) {
                player.setPlayWhenReady(false);
                //check it if already exist or not if yes then start from there else start from start position
                int existIndex = -1;
                for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                    if (currentTag.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                        existIndex = i;
                        break;
                    }
                }

                if (existIndex != -1) {
                    player.seekTo(nextWindowIndex, Constant.filesPlaybackHistory.get(existIndex).getPlaybackPosition());
                    player.setPlayWhenReady(true);
                } else {
                    player.seekTo(nextWindowIndex, C.TIME_UNSET);
                    player.setPlayWhenReady(true);
                }
            }

//            String tag = player.getCurrentTag().toString();
            videoTitle.setText(currentTag);
            // Setting following line in manifest solve change rotation problem
            //android:configChanges="orientation|screenSize|layoutDirection"
            rotateScreen(nextWindowIndex);
        }
    }

    private void playPreviousVideo() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty())
            return;

        int currentWindowIndex = player.getCurrentWindowIndex();
        updatePlaybackPosition(currentWindowIndex);

//        String currentTag;
//        //first check user want to remember playback history
//        if (Constant.playbackState == Constant.playbackRecord.YES) {
//            //if timeline is not null, then we will get previous video tag and playbackposition
//            currentTag = player.getCurrentTag().toString();
//            long playbackPosition = Math.max(0, player.getContentPosition());
//            //now we will check video previous playback history, if yes then replace it else create one
//            boolean alreadyExist = false;
//            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
//                if (currentTag.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
//                    Constant.filesPlaybackHistory.get(i).setPlaybackPosition(playbackPosition);
//                    alreadyExist = true;
//                    break;
//                }
//            }
//            if (!alreadyExist)
//                Constant.filesPlaybackHistory.add(new FilesPlaybackHistory(player.getCurrentTag().toString(), playbackPosition));
//        }

        //move to previous video
        int previousWindowIndex = player.getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET){
            player.seekTo(previousWindowIndex, C.TIME_UNSET);

            //get next video tag
            currentTag = player.getCurrentTag().toString();
            //first check playback state
            if (Constant.playbackState == Constant.playbackRecord.YES) {
                player.setPlayWhenReady(false);
                //check it if already exist or not if yes then start from there else start from start position
                int existIndex = -1;
                for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                    if (currentTag.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                        existIndex = i;
                    }
                }

                if (existIndex != -1) {
                    player.seekTo(previousWindowIndex, Constant.filesPlaybackHistory.get(existIndex).getPlaybackPosition());
                    player.setPlayWhenReady(true);
                } else {
                    player.seekTo(previousWindowIndex, C.TIME_UNSET);
                    player.setPlayWhenReady(true);
                }
            }

            videoTitle.setText(currentTag);
            rotateScreen(previousWindowIndex);
        }
    }

    private void rotateScreen(int pos) {
        try {
            String filePath;
            if (dataType == SEARCHTYPE){
                filePath = Constant.suggestionData.get(pos).getFile().getPath();
            }else {     //reguartype
//                filePath = Constant.allMemoryVideoList.get(pos).getFile().getPath();
                if (Constant.SORT_TYPE == 1)
                    filePath = Constant.currentFolderFiles.get(pos).getFile().getPath();
                else
                    filePath = Constant.allMemoryVideoList.get(pos).getFile().getPath();
            }
            //Create a new instance of MediaMetadataRetriever
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //Declare the Bitmap
            Bitmap bmp;
            //Set the video Uri as data source for MediaMetadataRetriever
            retriever.setDataSource(filePath);
            //Get one "frame"/bitmap - * NOTE - no time was set, so the first available frame will be used
            bmp = retriever.getFrameAtTime();

            //Get the bitmap width and height
            int videoWidth = bmp.getWidth();
            int videoHeight = bmp.getHeight();

            //If the width is bigger then the height then it means that the video was taken in landscape mode and we should set the orientation to landscape
            if (videoWidth > videoHeight) {
                //Set orientation to landscape
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            //If the width is smaller then the height then it means that the videhjjo was taken in portrait mode and we should set the orientation to portrait
            if (videoWidth < videoHeight) {
                //Set orientation to portrait
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            retriever.release();

        } catch (RuntimeException ex) {
            //error occurred
            Log.e("MediaMetadataRetriever", "- Failed to rotate the video");

            ex.printStackTrace();

        }
    }

    // 1 : BEST FIT, 2 : FILL, 3 : CENTER, 4 : (4:3 SIZE), 5 : (16:9), 6 : CROP
    private void setResizeType() {
        if (RESIZE_TYPE == 1) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            resizeLayout.setVisibility(View.VISIBLE);
            resizeTxt.setText("BEST FIT");
        }
        else if (RESIZE_TYPE == 2) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            resizeLayout.setVisibility(View.VISIBLE);
            resizeTxt.setText("FILL");
        }
        else if (RESIZE_TYPE == 3) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            resizeLayout.setVisibility(View.VISIBLE);
            resizeTxt.setText("CROP");
        }

        mHandler.postDelayed(resizeTxtFadOut, 1000);
    }

    Runnable resizeTxtFadOut = new Runnable() {
        @Override
        public void run() {
            resizeLayout.setVisibility(View.GONE);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //this will be called onStop(): we want to store data before release the player
//        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    This will destroy player when we minimize it
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        releasePlayer();
//    }

    /**
     * Before API level 24 we release player resources early
     * because there is no guarantee of onStop being called before the system terminates our app
     * remember onPause means the activity is partly obscured by something else (e.g. incoming call, or alert dialog)
     * so we do not want to be playing media while our activity is not in the foreground.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {

            if (callFromStateEnded){
                int pos = player.getCurrentWindowIndex();
                updatePosDiscontinuityPlayback(pos);
            }
            else{
                int pos = player.getCurrentWindowIndex();
                updatePlaybackPosition(pos);
            }

            //save the playback history
            Paper.book().write("playbackHistory",Constant.filesPlaybackHistory);

            unregisterReceiver(myNoisyAudioStreamReceiver);

            releasePlayer();
        }
    }

    // API level 24+ we release the player resources when the activity is no longer visible (onStop)
    // NOTE: On API 24+, onPause is still visible!!! So we do not not want to release the player resources
    // this is made possible by the new Android Multi-Window Support https://developer.android.com/guide/topics/ui/multi-window.html
    // We stop playing media on API 24+ only when our activity is no longer visible aka onStop
    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {

            if (callFromStateEnded){
                int pos = player.getCurrentWindowIndex();
                updatePosDiscontinuityPlayback(pos);
            }
            else{
                int pos = player.getCurrentWindowIndex();
                updatePlaybackPosition(pos);
            }

            //save the playback history
            Paper.book().write("playbackHistory",Constant.filesPlaybackHistory);

            unregisterReceiver(myNoisyAudioStreamReceiver);

            releasePlayer();
        }
    }

    /*
     * NOTE: we initialize the player either in onStart or onResume according to API level
     * API level 24 introduced support for multiple windows to run side-by-side. So it's safe to initialize our player in onStart
     * more on Multi-Window Support here https://developer.android.com/guide/topics/ui/multi-window.html
     * Before API level 24, we wait as long as onResume (to grab system resources) before initializing player
     */

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {

            registerReceiver(myNoisyAudioStreamReceiver, intentFilter);

            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // start in pure full screen
        hideSystemUI();
        if ((Util.SDK_INT <= 23 || player == null)) {

            registerReceiver(myNoisyAudioStreamReceiver, intentFilter);

            initializePlayer();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pause:
                pauseVideo();
                break;
            case R.id.btn_play:
                if (player.getPlaybackState() == Player.STATE_IDLE)
                    player.prepare(mediaSource);

                //screen on when video playing
                playerView.setKeepScreenOn(true);

                player.setPlayWhenReady(true);
                player.getPlaybackState();

                //player is not in pause state
                pauseState = false;

                //show controls
                show(sDefaultTimeout);

                break;
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_next:
//                Toast.makeText(this,"Next Video Logic Here",Toast.LENGTH_SHORT).show();
                playNextVideo();
                show(sDefaultTimeout);
                break;
            case R.id.btn_prev:
//                Toast.makeText(this,"Previous Video Logic Here",Toast.LENGTH_SHORT).show();
                playPreviousVideo();
                show(sDefaultTimeout);
                break;
            case R.id.btn_lock:
//                Toast.makeText(this,"Lock Logic Here",Toast.LENGTH_SHORT).show();
                controlsState = ControlsMode.LOCK;
                updateLockUnlock();
                break;
            case R.id.btn_resize:
                if (RESIZE_TYPE == 3)
                    RESIZE_TYPE = 0;
                RESIZE_TYPE += 1;
//                editor = getSharedPreferences("RESIZE_PREFERENCES",MODE_PRIVATE).edit();
//                editor.putInt("RESIZE_TYPE",RESIZE_TYPE);
//                editor.apply();
                setResizeType();
//                Toast.makeText(this,"Resize Logic Here",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tracks:
                if (!isShowingTrackSelectionDialog && TrackSelectionDialog.willHaveContent(trackSelector)){
                    isShowingTrackSelectionDialog = true;
                    TrackSelectionDialog trackSelectionDialog =
                            TrackSelectionDialog.createForTrackSelector(
                                    trackSelector,
                                    /* onDismissListener= */ new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dismissedDialog) {
                                            isShowingTrackSelectionDialog = false;
                                        }
                                    });
                    trackSelectionDialog.show(getSupportFragmentManager(),null);
                }

                break;
//            case R.id.dual_audio_ic:
////                Toast.makeText(this,"Dual Audio Logic Here",Toast.LENGTH_SHORT).show();
//
//                //rendererIndex 1 is used for audio track,
//                mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//                if (mappedTrackInfo != null) {
//                    int rendererType = mappedTrackInfo.getRendererType(rendererIndexAudio);
//                    boolean allowAdaptiveSelections =
//                            rendererType == C.TRACK_TYPE_VIDEO
//                                    || (rendererType == C.TRACK_TYPE_AUDIO
//                                    && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
//                                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);
//
//                    CharSequence title = "Audio Track";
//                    Pair<AlertDialog, TrackSelectionV> dialogPair =
//                            TrackSelectionV.getDialog(this, title, trackSelector, rendererIndexAudio, (TrackSelectionV.onOkListener) this);
//                    dialogPair.second.setShowDisableOption(false);
//                    dialogPair.second.setShowAutoOption(false);
//                    dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
//                    dialogPair.first.show();
//
//                    setPauseState(true);
//                }
//                break;
            case R.id.btn_unlock:
                controlsState = ControlsMode.FULLCONTORLS;
                updateLockUnlock();
                break;
            case R.id.rotateBtn:
//                Toast.makeText(this,"Rotate Logic Here",Toast.LENGTH_SHORT).show();
                int orientation = this.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    // code for portrait mode
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    // code for landscape mode
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case R.id.mode_change_btn:
                switch (playerState){
                    case REGULAR:
                        playerState = playerMode.SHUFFLE;
                        mode_change_btn.setImageResource(R.drawable.exo_controls_shuffle_on);  //Todo check shuffle_on correct
                        player.setShuffleModeEnabled(true);

                        //first we check, if toast is showing then cancel it
                        if (toast != null)
                            toast.cancel();
                        toast = Toast.makeText(this,"Shuffle",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case SHUFFLE:
                        playerState = playerMode.REPEAT_LIST;
                        mode_change_btn.setImageResource(R.drawable.exo_controls_repeat_all);
                        player.setShuffleModeEnabled(false);
                        player.setRepeatMode(Player.REPEAT_MODE_ALL);

                        if (toast != null)
                            toast.cancel();

                        toast = Toast.makeText(this,"Repeat List",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case REPEAT_LIST:
                        playerState = playerMode.REPEAT_ONE;
                        mode_change_btn.setImageResource(R.drawable.exo_controls_repeat_one);
                        player.setRepeatMode(Player.REPEAT_MODE_ONE);

                        if (toast != null)
                            toast.cancel();
                        toast = Toast.makeText(this,"Repeat One",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case REPEAT_ONE:
                        playerState = playerMode.REGULAR;
                        mode_change_btn.setImageResource(R.drawable.ic_play_in_order_24dp);
                        player.setRepeatMode(Player.REPEAT_MODE_OFF);


                        if (toast != null)
                            toast.cancel();
                        toast = Toast.makeText(this,"Play in order",Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                }
                show(sDefaultTimeout);
                break;
        }
    }

    void pauseVideo(){
        player.setPlayWhenReady(false);
        player.getPlaybackState();

        //When video is not playing, screen on mode should be off
        playerView.setKeepScreenOn(false);

        pauseState = true;

        updatePausePlay();
    }

    void playVideo(){
        player.setPlayWhenReady(true);
        pauseState = false;

        setProgress();
        show(sDefaultTimeout);
        mHandler.post(mShowProgress);

        updatePausePlay();
    }

    private void updatePausePlay() {
        if (root == null || pauseBtn == null)
            return;

        boolean requestPlayPauseFocus = false;
        boolean playing = isPlaying();
        if (pauseBtn != null) {
            requestPlayPauseFocus |= !playing && pauseBtn.isFocused();
            pauseBtn.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
        if (playBtn != null) {
            requestPlayPauseFocus |= playing && playBtn.isFocused();
            playBtn.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    public void setPauseState(boolean state){
        if (state){
            pauseVideo();
        }
        else
        {
            playVideo();
        }
    }

    @Override
    public void onOkClick() {
        setPauseState(false);
    }

    private void updateLockUnlock(){
        if (root == null || unlockBtn == null)
            return;

        if (controlsState == ControlsMode.FULLCONTORLS){
            lockBtn.requestFocus();
            unlockPanel.setVisibility(View.GONE);
            root.setVisibility(View.VISIBLE);
            show(sDefaultTimeout);
        }else {
            unlockPanel.requestFocus();
            unlockPanel.setVisibility(View.VISIBLE);
            unlockBtn.requestFocus();
            root.setVisibility(View.GONE);
            hideSystemUI();
//            show(sDefaultTimeout);
        }
    }

    private void updatePreNext() {
        if (root == null) {
            return;
        }
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, windowTimeline);
            enablePrevious = windowTimeline.isDynamic || player.getPreviousWindowIndex() != C.INDEX_UNSET;
            enableNext = windowTimeline.isDynamic || player.getNextWindowIndex() != C.INDEX_UNSET;
        }
        setButtonEnabled(enablePrevious, previousBtn);
        setButtonEnabled(enableNext, nextBtn);
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        view.setVisibility(View.VISIBLE);
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

            //not called after video transition

//            int currentIndex = player.getCurrentWindowIndex();
//
//            rotateScreen(currentIndex);
//            String tag = player.getCurrentTag().toString();
//            videoTitle.setText(tag);
//
//            override = new DefaultTrackSelector.SelectionOverride(0, 0);
//            applySelection();
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
            if (trackSelector == null || trackInfo == null)
                //the view is not initialized.
                return;


            if (trackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS ) {
                Toast.makeText(getApplicationContext(), "Unsupported Audio", Toast.LENGTH_SHORT).show();
            }

            if (trackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS ) {
//                Toast.makeText(getApplicationContext(), "Unsupported Video", Toast.LENGTH_SHORT).show();
            }
            //Todo: show dialog

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState){
                case Player.STATE_READY:
                    loadingBarLayout.setVisibility(View.GONE);
                    break;
                case Player.STATE_BUFFERING:
                    loadingBarLayout.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_IDLE:
                    loadingBarLayout.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:
                    //This path finished playback. So we will find it in playback history.
                    //if find then replace it playback with C.TIME_UNSET else make new one
                    callFromStateEnded = true;

                    onBackPressed();
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            //getApplicationContext trigger error
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
            builder.setTitle("Can't play this video")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*Player will release in onPause() or onStop(). and data will be save their*/
//                            releasePlayer();
                            finish();
                        }
                    })
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK){
                                /*Player will release in onPause() or onStop(). and data will be save their*/
//                            releasePlayer();
                                finish();
                                return true;
                            }
                            return false;
                        }
                    }).show();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION){

                int currentIndex = player.getCurrentWindowIndex();
                //get next video tag
                String currentTag = player.getCurrentTag().toString();
                //first check playback state
                if (Constant.playbackState == Constant.playbackRecord.YES) {
                    player.setPlayWhenReady(false);
                    //check it if already exist or not if yes then start from there else start from start position
                    int existIndex = -1;
                    for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                        if (currentTag.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                            existIndex = i;
                            break;
                        }
                    }

                    if (existIndex != -1) {
                        player.seekTo(currentIndex, Constant.filesPlaybackHistory.get(existIndex).getPlaybackPosition());
                        player.setPlayWhenReady(true);
                    } else {
                        player.seekTo(currentIndex, C.TIME_UNSET);
                        player.setPlayWhenReady(true);
                    }

                    //This path finished playback. So we will find it in playback history.
                    //if find then replace it playback with C.TIME_UNSET else make new one
                    int pos = player.getPreviousWindowIndex();
                    updatePosDiscontinuityPlayback(pos);

                }

                rotateScreen(currentIndex);

                videoTitle.setText(currentTag);
            }

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }

    private void updatePlaybackPosition(int videoPosition) {
        String currentTag;
        //first check user want to remember playback history
        if (Constant.playbackState == Constant.playbackRecord.YES) {
            //if timeline is not null, then we will get previous video tag and playbackposition
            currentTag = player.getCurrentTag().toString();
            int playbackPosition = (int) Math.max(0, player.getContentPosition());
            //now we will check video previous playback history, if yes then replace it else create one
            boolean alreadyExist = false;
            for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
                if (currentTag.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                    long percentage;

                    //update Constant.allMemoryList and Constant.currentFolderFiles
                    /*first check type of file*/
                    FilesInfo filesInfo;
                    if (dataType == SEARCHTYPE){
                        filesInfo = Constant.suggestionData.get(videoPosition);
                        //set PlaybackPercentage not playbackPosition
                        int duration = getVideoDuration(filesInfo.getFile().getPath());
                        if (duration > 0)
                            percentage = 1000L * playbackPosition / duration;
                        else
                            percentage = C.TIME_UNSET;

                        filesInfo.setPlaybackPercentage(percentage);
                        int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                        Constant.allMemoryVideoList.get(index).setPlaybackPercentage(playbackPosition);
                    }else {
                        if (Constant.SORT_TYPE == 1){
                            filesInfo = Constant.currentFolderFiles.get(videoPosition);
                            //set playbackPercentage not playbackPosition
                            int duration = getVideoDuration(filesInfo.getFile().getPath());
                            if (duration > 0)
                                percentage = 1000L * playbackPosition / duration;
                            else
                                percentage = C.TIME_UNSET;

                            filesInfo.setPlaybackPercentage(percentage);

                            int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                            Constant.allMemoryVideoList.get(index).setPlaybackPercentage(percentage);
                        }else {
                            filesInfo = Constant.allMemoryVideoList.get(videoPosition);
                            //set playbackPercentage not playbackPosition
                            int duration = getVideoDuration(filesInfo.getFile().getPath());
                            percentage = 1000L * playbackPosition / duration;

                            filesInfo.setPlaybackPercentage(percentage);
                        }
                    }

                    Constant.filesPlaybackHistory.get(i).setPlaybackPosition(playbackPosition);

                    alreadyExist = true;
                    break;
                }
            }
            if (!alreadyExist) {
                long percentage;

                //update Constant.allMemoryVideoList ...
                /*first check type of file*/
                FilesInfo filesInfo;
                if (dataType == SEARCHTYPE){
                    filesInfo = Constant.suggestionData.get(videoPosition);
                    //set playbackPercentage not playbackPosition
                    int duration = getVideoDuration(filesInfo.getFile().getPath());
                    if (duration > 0)
                        percentage = 1000L * playbackPosition / duration;
                    else
                        percentage = C.TIME_UNSET;

                    filesInfo.setPlaybackPercentage(percentage);
                    filesInfo.setState(FilesInfo.fileState.NOT_NEW);

                    int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                    Constant.allMemoryVideoList.get(index).setPlaybackPercentage(percentage);
                    Constant.allMemoryVideoList.get(index).setState(FilesInfo.fileState.NOT_NEW);
                }else {
                    if (Constant.SORT_TYPE == 1){
                        filesInfo = Constant.currentFolderFiles.get(videoPosition);
                        //set playbackPercentage not playbackPosition
                        int duration = getVideoDuration(filesInfo.getFile().getPath());
                        if (duration > 0)
                            percentage = 1000L * playbackPosition / duration;
                        else
                            percentage = C.TIME_UNSET;

                        filesInfo.setPlaybackPercentage(percentage);
                        filesInfo.setState(FilesInfo.fileState.NOT_NEW);

                        int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                        Constant.allMemoryVideoList.get(index).setPlaybackPercentage(percentage);
                        Constant.allMemoryVideoList.get(index).setState(FilesInfo.fileState.NOT_NEW);
                    }else {
                        filesInfo = Constant.allMemoryVideoList.get(videoPosition);
                        //set playbackPercentage not playbackPosition
                        int duration = getVideoDuration(filesInfo.getFile().getPath());
                        if (duration > 0)
                            percentage = 1000L * playbackPosition / duration;
                        else
                            percentage = C.TIME_UNSET;

                        filesInfo.setPlaybackPercentage(percentage);
                        filesInfo.setState(FilesInfo.fileState.NOT_NEW);
                    }
                }
                Constant.filesPlaybackHistory.add(new FilesPlaybackHistory(currentTag, playbackPosition));
            }
        }
    }

    private void updatePosDiscontinuityPlayback(int pos) {
        String path;

        if (dataType == SEARCHTYPE) {
            path = Constant.suggestionData.get(pos).getFile().getName();
        }
        else        //regulartype
        {
            //when allVideoList and currentFolderFiles : i use following method Todo letter
            if (Constant.SORT_TYPE == 1) {
                path = Constant.currentFolderFiles.get(pos).getFile().getName();
            } else
                path = Constant.allMemoryVideoList.get(pos).getFile().getName();
        }

        boolean alreadyExist = false;
        for (int i = 0; i < Constant.filesPlaybackHistory.size(); i++) {
            if (path.equals(Constant.filesPlaybackHistory.get(i).getFileName())) {
                Constant.filesPlaybackHistory.get(i).setPlaybackPosition(C.TIME_UNSET);

                //update Constant.allMemoryList and Constant.currentFolderFiles
                /*first check type of file*/
                FilesInfo filesInfo;
                if (dataType == SEARCHTYPE){
                    filesInfo = Constant.suggestionData.get(pos);

                    filesInfo.setPlaybackPercentage(0);
                    int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                    Constant.allMemoryVideoList.get(index).setPlaybackPercentage(0);
                }else {
                    if (Constant.SORT_TYPE == 1){
                        filesInfo = Constant.currentFolderFiles.get(pos);
                        filesInfo.setPlaybackPercentage(0);

                        int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                        Constant.allMemoryVideoList.get(index).setPlaybackPercentage(0);
                    }else {
                        filesInfo = Constant.allMemoryVideoList.get(pos);
                        filesInfo.setPlaybackPercentage(0);
                    }
                }

                alreadyExist = true;
                break;
            }
        }
        if (!alreadyExist){
            Constant.filesPlaybackHistory.add(new FilesPlaybackHistory(path, C.TIME_UNSET));

            //update Constant.allMemoryVideoList ...
            /*first check type of file*/
            FilesInfo filesInfo;
            if (dataType == SEARCHTYPE){
                filesInfo = Constant.suggestionData.get(pos);

                filesInfo.setPlaybackPercentage(0);
                filesInfo.setState(FilesInfo.fileState.NOT_NEW);

                int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                Constant.allMemoryVideoList.get(index).setPlaybackPercentage(0);
                Constant.allMemoryVideoList.get(index).setState(FilesInfo.fileState.NOT_NEW);
            }else {
                if (Constant.SORT_TYPE == 1){
                    filesInfo = Constant.currentFolderFiles.get(pos);

                    filesInfo.setPlaybackPercentage(0);
                    filesInfo.setState(FilesInfo.fileState.NOT_NEW);

                    int index = Constant.allMemoryVideoList.indexOf(filesInfo);
                    Constant.allMemoryVideoList.get(index).setPlaybackPercentage(0);
                    Constant.allMemoryVideoList.get(index).setState(FilesInfo.fileState.NOT_NEW);
                }else {
                    filesInfo = Constant.allMemoryVideoList.get(pos);

                    filesInfo.setPlaybackPercentage(0);
                    filesInfo.setState(FilesInfo.fileState.NOT_NEW);
                }
            }
        }
    }

    public int getVideoDuration(String path){
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return Integer.parseInt(time);

        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeProgressBarContainer.setVisibility(View.VISIBLE);
                    volumeIcon_Text_ScreenMid_layout.setVisibility(View.VISIBLE);

                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                    if (currentVolume < 1) {
                        volumeSlider.setProgress(0);
                        volumeMidText.setVisibility(View.GONE);
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_mute_whire_35dp);
                    } else if (currentVolume >= 1 && currentVolume < 12) {
                        volumeSlider.setProgress(currentVolume);
                        volumeMidText.setVisibility(View.VISIBLE);
                        volumeMidText.setText(String.valueOf(currentVolume));
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_down_white_35dp);
                    }else if (currentVolume >= 12){
                        volumeSlider.setProgress(currentVolume);
                        volumeMidText.setText(String.valueOf(currentVolume));
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_full_white_35dp);
                    }

                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                if (action == KeyEvent.ACTION_UP)
                    mHandler.postDelayed(mFadeOutVolumeComponent,sDefaultTimeout);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeProgressBarContainer.setVisibility(View.VISIBLE);
                    volumeIcon_Text_ScreenMid_layout.setVisibility(View.VISIBLE);

                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                    if (currentVolume < 1) {
                        volumeSlider.setProgress(0);
                        volumeMidText.setVisibility(View.GONE);
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_mute_whire_35dp);
                    } else if (currentVolume >= 1 && currentVolume < 12) {
                        volumeSlider.setProgress(currentVolume);
                        volumeMidText.setVisibility(View.VISIBLE);
                        volumeMidText.setText(String.valueOf(currentVolume));
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_down_white_35dp);
                    }else if (currentVolume >= 12){
                        volumeSlider.setProgress(currentVolume);
                        volumeMidText.setText(String.valueOf(currentVolume));
                        volumeMideIcon.setImageResource(R.drawable.ic_volume_full_white_35dp);
                    }

                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                if (action == KeyEvent.ACTION_UP)
                    mHandler.postDelayed(mFadeOutVolumeComponent,sDefaultTimeout);
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private final Runnable mFadeOutVolumeComponent = new Runnable() {
        @Override
        public void run() {
            volumeProgressBarContainer.setVisibility(View.GONE);
            volumeIcon_Text_ScreenMid_layout.setVisibility(View.GONE);
        }
    };

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                if (player != null)
                    pauseVideo();
            }
        }
    }

}
