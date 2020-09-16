package com.kgeorge.myapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


import static com.kgeorge.myapp.MusicAdapter.mFiles;
import static com.kgeorge.myapp.MiniPlayerFragment.mediaPlayer;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView songName, artistName, durationPlayed, durationLeft, albumName;
    ImageView skipNext, skipPrev, backBtn, shuffleBtn, repeatBtn, cover_art, repeatFinite, sleepTimer;
    boolean shuffleOn = false, repeatOff = true;
    FloatingActionButton playPauseBtn;
    NotificationManager notificationManager;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> songsList = new ArrayList<>();
    static Uri uri;
    Bitmap bitmap;
    Thread playThread, skipNextThread, skipPrevThread, sleepTimerThread;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getInfo();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getIntentMethod();
        albumName.setText(songsList.get(position).getAlbum());
        songName.setText(songsList.get(position).getTitle());
        artistName.setText(songsList.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPosition;
                if (mediaPlayer != null) {
                    currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    int subtraction = ((mediaPlayer.getDuration() / 1000) - currentPosition);
                    durationLeft.setText("-" + timeFormat(subtraction));
                    durationPlayed.setText(timeFormat(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!shuffleOn) {
                    shuffleOn = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                } else {
                    shuffleOn = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                }
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatOff) {
                    repeatOff = false;
                    repeatBtn.setImageResource(R.drawable.repeat_on);
                    System.out.println();
                } else {
                    repeatOff = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat);
                }

            }
        });
    }



    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action){
                case CreateNotification.PREV:
                    skipPrevBtnClicked();
                    break;
                case CreateNotification.PLAY:
                    playPauseBtnClicked();
                    break;
                case CreateNotification.NEXT:
                    skipBtnClicked();
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        playThreadBtn();
        SkipNextBtn();
        SkipPrevBtn();

    }

    private void SkipPrevBtn() {
        skipPrevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                skipPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        skipPrevBtnClicked();
                    }
                });
            }
        };
        skipPrevThread.start();
    }

    private void skipPrevBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            if (shuffleOn && repeatOff) {
                position = getRandomNo(songsList.size() - 1);
            } else if (!shuffleOn && repeatOff) {
                position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
            }
            nextSongSetup();
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
            CreateNotification.notiCreate(this, mFiles.get(position),bitmap,R.drawable.ic_pause_24px);
            readyToStart();
        } else {
            if (shuffleOn && repeatOff) {
                position = getRandomNo(songsList.size() - 1);
            } else if (!shuffleOn && repeatOff) {
                position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
            }
            nextSongSetup();
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
            CreateNotification.notiCreate(this, songsList.get(position), bitmap, R.drawable.ic_pause_24px);
            readyToStart();
        }

    }


    private void SkipNextBtn() {
        skipNextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                skipNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        skipBtnClicked();
                    }
                });
            }
        };

        skipNextThread.start();
    }

    private void skipBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            if (shuffleOn && repeatOff) {
                position = getRandomNo(songsList.size() - 1);
            } else if (!shuffleOn && repeatOff) {
                position = ((position + 1) % songsList.size());
            }
            nextSongSetup();
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
            CreateNotification.notiCreate(this, songsList.get(position), bitmap, R.drawable.ic_pause_24px);
            readyToStart();

        } else {
            if (shuffleOn && repeatOff) {
                position = getRandomNo(songsList.size() - 1);
            } else if (!shuffleOn && repeatOff) {
                position = ((position + 1) % songsList.size());
            }
            nextSongSetup();
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
            CreateNotification.notiCreate(this, songsList.get(position), bitmap, R.drawable.ic_pause_24px);
            readyToStart();
        }
    }

    private void nextSongSetup() {
        mediaPlayer.stop();
        mediaPlayer.release();
        uri = Uri.parse(songsList.get(position).getPath());
        metaData(uri);
        mediaPlayer = MediaPlayer.create(this, uri);
        songName.setText(songsList.get(position).getTitle());
        artistName.setText(songsList.get(position).getArtist());
        albumName.setText(songsList.get(position).getAlbum());
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        setDetails();
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };

        playThread.start();

    }

    private void playPauseBtnClicked() {
        boolean isPlaying;
        if (mediaPlayer.isPlaying()) {
            isPlaying = true;
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
            CreateNotification.notiCreate(this, songsList.get(position), bitmap, R.drawable.ic_play_arrow);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            setDetails();
        } else {
            isPlaying = false;
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
            CreateNotification.notiCreate(this, songsList.get(position), bitmap, R.drawable.ic_pause_24px);
            mediaPlayer.start();
            setDetails();
        }
        MiniPlayerFragment.setPlayPauseStatus(isPlaying);
    }


    private void setDetails() {
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private int getRandomNo(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private String timeFormat(int position) {
        String totalOut;
        String totalNew;
        String seconds = String.valueOf(position % 60);
        String minutes = String.valueOf(position / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }


    private void getInfo() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        albumName = findViewById(R.id.albumName);
        durationPlayed = findViewById(R.id.durationPlayed);
        durationLeft = findViewById(R.id.durationLeft);
        skipNext = findViewById(R.id.skipNext);
        skipPrev = findViewById(R.id.skipPrev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.shuffle_off);
        repeatBtn = findViewById(R.id.repeat);
        cover_art = findViewById(R.id.cover_art);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekbar);
        repeatFinite = findViewById(R.id.repeat_finite);
        sleepTimer = findViewById(R.id.sleep);
    }


    private void getIntentMethod(){
        position = getIntent().getIntExtra("position", -1);
        songsList = mFiles;
        if (songsList != null) {
            uri = Uri.parse(songsList.get(position).getPath());
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        if (mediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_pause_24px);
        }
        else {
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
        }
        metaData(uri);
    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] cover = retriever.getEmbeddedPicture();
        if (cover != null) {
            bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            Animation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gradient = findViewById(R.id.ImageViewGradient);
                        RelativeLayout cont = findViewById(R.id.player_activity);
                        gradient.setBackgroundResource(R.drawable.m_gradient);
                        cont.setBackgroundResource(R.drawable.m_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        cont.setBackground(gradientDrawableBg);
                    }
                }
            });
        } else {
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.sample);
            Animation(this, cover_art, bitmap);
            Glide.with(this).load(R.drawable.sample).into(cover_art);
            ImageView gradient = findViewById(R.id.ImageViewGradient);
            RelativeLayout cont = findViewById(R.id.player_activity);
            gradient.setBackgroundResource(R.drawable.m_gradient);
            cont.setBackgroundResource(R.drawable.m_bg);


        }
        retriever.release();

    }
    public void Animation(final Context context, final ImageView imageView, final Bitmap bitmap ){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    private void readyToStart(){
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skipBtnClicked();
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(this);

        }
    }

}