package com.kgeorge.myapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;


public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles;
    private AdapterToFragment af;
    int previousItemClicked =-1;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles, AdapterToFragment af){
        this.mContext = mContext;
        this.mFiles = mFiles;
        this.af = af;



    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.fileName.setText(mFiles.get(position).getTitle());
        holder.artistName.setText(mFiles.get(position).getArtist());
        final byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image !=null){
            Glide.with(mContext).asBitmap()
                    .load(image).into(holder.artwork);
        }
        else {
            Glide.with(mContext)
                    .load(R.drawable.sample).into(holder.artwork);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap;
                previousItemClicked = position;
                notifyDataSetChanged();
                af.miniPlayer(position);
                if (image != null){
                    bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                }
                else {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sample);
                }
                CreateNotification.notiCreate(mContext, mFiles.get(position), bitmap, R.drawable.ic_pause_24px);
            }
        });
        if (previousItemClicked == position){
            holder.fileName.setTextColor(mContext.getColor(R.color.colorSecondary));
        }
        else {
            holder.fileName.setTextColor(mContext.getColor(R.color.colorAccent));
        }
        holder.moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.delete:
                                deleteFile(position, view);
                                break;
                        }
                        return true;

                    }
                });
            }
        });



    }



    private void deleteFile(int position, View view){
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getId()));
        File file = new File(mFiles.get(position).getPath());
        boolean fileDeleted = file.delete();
        if (fileDeleted){
            mContext.getContentResolver().delete(uri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view , "File has been deleted", Snackbar.LENGTH_LONG).show();
        }
        else{
            Snackbar.make(view,"File can't be deleted" , Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView fileName;
        TextView artistName;
        ImageView artwork;
        ImageView moreMenu;

        public MyViewHolder(View itemView){
            super(itemView);
            artistName = itemView.findViewById(R.id.artist_name);
            fileName = itemView.findViewById(R.id.song_title);
            artwork = itemView.findViewById(R.id.music_img);
            moreMenu = itemView.findViewById(R.id.menuMore);

        }

        }

        private byte[] getAlbumArt(String uri){
            byte[] art;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
    }

    public void updateList(ArrayList<MusicFiles> returnedSongs){
        mFiles = new ArrayList<>();
        mFiles.addAll(returnedSongs);
        notifyDataSetChanged();
    }
}
