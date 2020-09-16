package com.kgeorge.myapp;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;


import static com.kgeorge.myapp.MusicAdapter.mFiles;

/**
 * A simple {@link Fragment} subclass.

 */
public class MiniPlayerFragment extends Fragment {
    boolean isPlaying = true;
    int position;
    TextView songView;
    TextView artistView;
    ImageView coverView;
    static ImageView playPause;
    SeekBar seekBar;
    RelativeLayout miniPlayer;
    static MediaPlayer mediaPlayer;
    static Uri uri;
    Thread playPauseThread;
    Thread relThread;
    Bitmap bitmap;
    NotificationManager notificationManager;
    private Handler handler = new Handler();




    public MiniPlayerFragment(int position) {
        this.position = position;
        // Required empty public constructor
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        songView  =  view.findViewById(R.id.mini_songName);
        artistView = view.findViewById(R.id.mini_artistName);
        coverView =  view.findViewById(R.id.mini_player_cover);
        playPause =  view.findViewById(R.id.mini_player_control);
        seekBar = view.findViewById(R.id.mini_seekbar);
        miniPlayer = view.findViewById(R.id.mini_rel);

        startMusic();
        createChannel();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("Tunes"));
        getActivity().startService(new Intent(getActivity().getBaseContext(), OnClearFromRecentService.class));
        songView.setText(mFiles.get(position).getTitle());
        artistView.setText(mFiles.get(position).getArtist());


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPosition;
                if (mediaPlayer != null){
                    currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);

            }
        });
        byte [] image = getAlbumArt(mFiles.get(position).getPath());

        if (image != null){
            Glide.with(getContext()).asBitmap()
                    .load(image).into(coverView);
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                }
        else {
            Glide.with(getContext())
                    .load(R.drawable.sample).into(coverView);
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
                }



        return view;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            if (CreateNotification.PLAY.equals(action)){
                playBtnClicked();
            }
        }
    };



    private void createChannel(){

        NotificationChannel channel = new NotificationChannel(CreateNotification.ID,
                "Audio", NotificationManager.IMPORTANCE_LOW);

        notificationManager = getContext().getSystemService(NotificationManager.class);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        relThread();
        miniPlayerThread();
    }


    private void relThread(){
        relThread = new Thread(new Runnable() {
            @Override
            public void run() {
                miniPlayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), PlayerActivity.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        }
                });
            }
        });

        relThread.start();
    }


    private void miniPlayerThread(){
        playPauseThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playBtnClicked();
                    }
                });
            }
        };
        playPauseThread.start();
    }







    private void playBtnClicked(){
        if (isPlaying) {
            isPlaying = false;
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            CreateNotification.notiCreate(getContext(), mFiles.get(position), bitmap, R.drawable.ic_play_arrow);
            playPause.setImageResource(R.drawable.playwhite);
        } else {
            isPlaying = true;
            mediaPlayer.start();
            CreateNotification.notiCreate(getContext(), mFiles.get(position), bitmap, R.drawable.ic_pause_24px);
            playPause.setImageResource(R.drawable.pausewhite);
                }
            }

    public static void setPlayPauseStatus(boolean isPlaying){
        if (isPlaying){
            playPause.setImageResource(R.drawable.playwhite);
        }
        else {
            playPause.setImageResource(R.drawable.pausewhite);
        }
    }


    private void startMusic(){
        if (mFiles != null){
            playPause.setImageResource(R.drawable.pausewhite);
            uri = Uri.parse(mFiles.get(position).getPath());
        }
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);

    }







    private byte[] getAlbumArt(String uri){
        byte[] art;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
        getContext().unregisterReceiver(broadcastReceiver);
    }

}