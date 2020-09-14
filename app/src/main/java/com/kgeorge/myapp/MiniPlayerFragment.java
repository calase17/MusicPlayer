package com.kgeorge.myapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
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
    ImageView playPause;
    SeekBar seekBar;
    static MediaPlayer mediaPlayer;
    static Uri uri;
    Thread playPauseThread;
    Bitmap bitmap;
    private Handler handler = new Handler();




    public MiniPlayerFragment(int position) {
        this.position = position;
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        songView  =  view.findViewById(R.id.mini_songName);
        artistView = view.findViewById(R.id.mini_artistName);
        coverView =  view.findViewById(R.id.mini_player_cover);
        playPause =  view.findViewById(R.id.mini_player_control);
        seekBar = view.findViewById(R.id.mini_seekbar);

        startMusic();

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

    @Override
    public void onResume() {
        super.onResume();
        miniPlayerThread();
    }

    private void miniPlayerThread(){
        playPauseThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isPlaying){
                            isPlaying = false;
                            mediaPlayer.pause();
                            seekBar.setMax(mediaPlayer.getDuration() / 1000);
                            CreateNotification.notiCreate(getContext(), mFiles.get(position), bitmap, R.drawable.ic_play_arrow );
                            playPause.setImageResource(R.drawable.playwhite);
                        }
                        else {
                            isPlaying = true;
                            mediaPlayer.start();
                            CreateNotification.notiCreate(getContext(), mFiles.get(position), bitmap, R.drawable.ic_pause_24px );
                            playPause.setImageResource(R.drawable.pausewhite);
                        }
                    }
                });
            }
        };
        playPauseThread.start();

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
            System.out.println("hello");
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

}