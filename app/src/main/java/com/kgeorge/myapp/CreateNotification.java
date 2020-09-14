package com.kgeorge.myapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import androidx.core.app.NotificationManagerCompat;


public class CreateNotification {

    static Notification notification;
    public static final String ID = "channel1";
    public static final String PREV = "previous";
    public static final String PLAY = "play";
    public static final String NEXT = "next";



    public static void notiCreate(Context context, MusicFiles mFiles, Bitmap bitmap, int playPause){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        MediaSession mediaSession = new MediaSession(context,"tag");

        PendingIntent pendingIntentPrev;

        Intent intentPrev = new Intent(context, NotificationActionService.class)
                .setAction(PREV);
        pendingIntentPrev = PendingIntent.getBroadcast(context,0 ,intentPrev,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(context, NotificationActionService.class)
                .setAction(PLAY);

        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentNext = new Intent(context,  NotificationActionService.class)
                .setAction(NEXT);

        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context,0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action playAction = new Notification.Action.Builder(Icon.createWithResource
                (context, playPause),"play",pendingIntentPlay).build();

        Notification.Action nextAction = new Notification.Action.Builder(Icon.createWithResource
                (context, R.drawable.ic_skip_next),"next",pendingIntentNext).build();

        Notification.Action prevAction = new Notification.Action.Builder(Icon.createWithResource
                (context, R.drawable.ic_skip_previous),"previous",pendingIntentPrev).build();


        notification = new Notification.Builder(context, ID)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentTitle(mFiles.getTitle())
                .setContentText(mFiles.getArtist())
                .addAction(prevAction)
                .addAction(playAction)
                .addAction(nextAction)
                .setStyle(new Notification.MediaStyle()
                .setShowActionsInCompactView(0,1,2)
                .setMediaSession(mediaSession.getSessionToken()))
                .setLargeIcon(bitmap)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .build();

        notificationManagerCompat.notify(1, notification);





    }

}
