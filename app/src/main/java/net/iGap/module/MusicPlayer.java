/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.module;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import io.realm.Realm;
import io.realm.RealmResults;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import net.iGap.G;
import net.iGap.R;
import net.iGap.activities.ActivityMain;
import net.iGap.fragments.FragmentChat;
import net.iGap.helper.HelperCalander;
import net.iGap.helper.HelperDownloadFile;
import net.iGap.helper.HelperLog;
import net.iGap.interfaces.OnComplete;
import net.iGap.proto.ProtoFileDownload;
import net.iGap.proto.ProtoGlobal;
import net.iGap.realm.RealmRoomMessage;
import net.iGap.realm.RealmRoomMessageFields;

import static net.iGap.G.context;

public class MusicPlayer extends Service {

    private static SensorManager mSensorManager;
    private static Sensor mProximity;
    private static SensorEventListener sensorEventListener;
    private static final int SENSOR_SENSITIVITY = 4;
    public static boolean canDoAction = true;
    private static MediaSessionCompat mSession;
    //    private static Bitmap orginalWallPaper = null;
    //    private static boolean isGetOrginalWallpaper=false;

    public static final int notificationId = 19;
    public static String repeatMode = RepeatMode.noRepeat.toString();
    public static boolean isShuffelOn = false;
    public static TextView txt_music_time;
    public static TextView txt_music_time_counter;
    public static String musicTime = "";
    public static String roomName;
    public static String musicPath;
    public static String musicName;
    public static String musicInfo = "";
    public static String musicInfoTitle = "";
    public static long roomId = 0;
    public static Bitmap mediaThumpnail = null;
    public static MediaPlayer mp;
    public static OnComplete onComplete = null;
    public static OnComplete onCompleteChat = null;
    public static boolean isShowMediaPlayer = false;
    public static int musicProgress = 0;
    private static LinearLayout layoutTripMusic;
    private static TextView btnPlayMusic;
    private static TextView btnCloseMusic;
    private static TextView txt_music_name;
    private static RemoteViews remoteViews;
    private static NotificationManager notificationManager;
    private static Notification notification;
    public static boolean isPause = false;
    private static ArrayList<RealmRoomMessage> mediaList;
    private static int selectedMedia = 0;
    private static Timer mTimer, mTimeSecend;
    private static long time = 0;
    private static double amoungToupdate;
    public static String strTimer = "";
    public static String messageId = "";
    public static boolean isNearDistance = false;

    public static boolean isVoice = false;
    public static boolean pauseSoundFromIGapCall = false;
    public static boolean isSpeakerON = false;

    public static boolean pauseSoundFromCall = false;
    public static boolean isMusicPlyerEnable = false;
    private static int stateHedset = 0;

    public static boolean playNextMusic = false;

    public static String STARTFOREGROUND_ACTION = "STARTFOREGROUND_ACTION";
    public static String STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION";
    private static HeadsetPluginReciver headsetPluginReciver;

    private static RemoteControlClient remoteControlClient;
    private static ComponentName remoteComponentName;
    public static boolean downloadNewItem = false;

    public static LinearLayout mainLayout;
    public static LinearLayout chatLayout;
    public static LinearLayout shearedMediaLayout;

    private static Realm mRealm;
    private static boolean isRegisterSensore = false;

    private static Realm getmRealm() {
        if (mRealm == null || mRealm.isClosed()) {

            mRealm = Realm.getDefaultInstance();
        }

        return mRealm;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getExtras() == null) {
            stopForeground(true);
            stopSelf();
        } else {

            String action = intent.getExtras().getString("ACTION");
            if (action != null) {

                if (action.equals(STARTFOREGROUND_ACTION)) {

                    if (notification != null) {
                        startForeground(notificationId, notification);

                        initSensore();
                    }
                } else if (action.equals(STOPFOREGROUND_ACTION)) {

                    removeSensore();

                    stopForeground(true);
                    stopSelf();
                }
            }
        }

        return START_STICKY;
    }

    public static void setMusicPlayer(LinearLayout layoutTripMusic) {

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_layout_notification);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (layoutTripMusic != null) {
            layoutTripMusic.setVisibility(View.GONE);
        }

        initLayoutTripMusic(layoutTripMusic);

        getAtribuits();

        //  getOrginallWallpaper();

    }

    //
    //    private static  void getOrginallWallpaper(){
    //
    //        if(isGetOrginalWallpaper){
    //            return;
    //        }
    //
    //        isGetOrginalWallpaper=true;
    //
    //
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    //
    //            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(G.context);
    //
    //            if (myWallpaperManager.isSetWallpaperAllowed()) {
    //
    //                ParcelFileDescriptor pfd = myWallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
    //                if (pfd != null) {
    //                    orginalWallPaper = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
    //                    try {
    //                        pfd.close();
    //                    } catch (IOException e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            }
    //
    //        }
    //    }

    public static void repeatClick() {

        String str = "";
        if (repeatMode.equals(RepeatMode.noRepeat.toString())) {
            str = RepeatMode.repeatAll.toString();
        } else if (repeatMode.equals(RepeatMode.repeatAll.toString())) {
            str = RepeatMode.oneRpeat.toString();
        } else if (repeatMode.equals(RepeatMode.oneRpeat.toString())) {
            str = RepeatMode.noRepeat.toString();
        }

        repeatMode = str;

        SharedPreferences sharedPreferences = context.getSharedPreferences("MusicSetting", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("RepeatMode", str);
        editor.apply();

        if (onComplete != null) {
            onComplete.complete(true, "RepeatMode", "");
        }
    }

    public static void shuffelClick() {

        isShuffelOn = !isShuffelOn;
        SharedPreferences sharedPreferences = context.getSharedPreferences("MusicSetting", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("Shuffel", isShuffelOn);
        editor.apply();
        if (onComplete != null) {
            onComplete.complete(true, "Shuffel", "");
        }
    }

    public static void initLayoutTripMusic(LinearLayout layout) {

        MusicPlayer.layoutTripMusic = layout;

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ActivityMain.class);
                intent.putExtra(ActivityMain.openMediaPlyer, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
            }
        });

        txt_music_time = (TextView) layout.findViewById(R.id.mls_txt_music_time);

        txt_music_time_counter = (TextView) layout.findViewById(R.id.mls_txt_music_time_counter);
        txt_music_name = (TextView) layout.findViewById(R.id.mls_txt_music_name);

        btnPlayMusic = (TextView) layout.findViewById(R.id.mls_btn_play_music);
        btnPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAndPause();
            }
        });

        btnCloseMusic = (TextView) layout.findViewById(R.id.mls_btn_close);
        btnCloseMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLayoutMediaPlayer();
            }
        });

        if (MusicPlayer.mp != null) {
            layout.setVisibility(View.VISIBLE);
            txt_music_name.setText(MusicPlayer.musicName);

            txt_music_time.setText(musicTime);

            if (MusicPlayer.mp.isPlaying()) {
                btnPlayMusic.setText(context.getString(R.string.md_pause_button));
            } else {
                btnPlayMusic.setText(context.getString(R.string.md_play_arrow));
            }
        }

        if (HelperCalander.isLanguagePersian) {
            txt_music_time.setText(HelperCalander.convertToUnicodeFarsiNumber(txt_music_time.getText().toString()));
        }

        if (MusicPlayer.mp != null) {
            time = MusicPlayer.mp.getCurrentPosition() - 1;
            if (time >= 0) {
                updatePlayerTime();
            }
        }
    }

    public static void playAndPause() {

        if (mp != null) {
            if (mp.isPlaying()) {
                pauseSound();
            } else {
                playSound();
            }
        } else {
            playSound();
        }
    }

    public static void pauseSound() {

        try {
            remoteViews.setImageViewResource(R.id.mln_btn_play_music, R.mipmap.play_button);
            notificationManager.notify(notificationId, notification);
        } catch (RuntimeException e) {
        }

        try {
            stopTimer();

            if (btnPlayMusic != null) {
                btnPlayMusic.setText(context.getString(R.string.md_play_arrow));
            }

            if (!isShowMediaPlayer) {
                if (onCompleteChat != null) {
                    onCompleteChat.complete(true, "play", "");
                }
            } else if (onComplete != null) {
                onComplete.complete(true, "play", "");
            }
        } catch (Exception e) {
            HelperLog.setErrorLog("music player   pauseSound   aaa    " + e.toString());
        }

        try {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
                isPause = true;
            }
        } catch (Exception e) {
            HelperLog.setErrorLog("music player   pauseSound   bbb    " + e.toString());
        }
    }

    //**************************************************************************

    public static void playSound() {

        if (mp == null) {
            return;
        }

        if (mp.isPlaying()) {
            return;
        }

        try {
            remoteViews.setImageViewResource(R.id.mln_btn_play_music, R.mipmap.pause_button);
            notificationManager.notify(notificationId, notification);
        } catch (RuntimeException e) {
        }

        try {

            if (btnPlayMusic != null) {
                btnPlayMusic.setText(context.getString(R.string.md_pause_button));
            }

            if (!isShowMediaPlayer) {

                if (onCompleteChat != null) {
                    onCompleteChat.complete(true, "pause", "");
                }
            } else if (onComplete != null) {
                onComplete.complete(true, "pause", "");
            }
        } catch (Exception e) {
            HelperLog.setErrorLog("music player   playSound   aaa    " + e.toString());
        }

        try {
            if (mp != null && isPause) {
                mp.start();
                isPause = false;
                updateProgress();
            } else {
                startPlayer(musicName, musicPath, roomName, roomId, false, MusicPlayer.messageId);
            }
        } catch (Exception e) {
            HelperLog.setErrorLog("music player   playSound  bbb " + e.toString());
        }
    }

    public static void stopSound() {

        String zeroTime = "0:00";

        if (HelperCalander.isLanguagePersian) {
            zeroTime = HelperCalander.convertToUnicodeFarsiNumber(zeroTime);
        }

        if (txt_music_time_counter != null) {
            txt_music_time_counter.setText(zeroTime);
        }

        try {
            remoteViews.setImageViewResource(R.id.mln_btn_play_music, R.mipmap.play_button);
            notificationManager.notify(notificationId, notification);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        try {

            if (btnPlayMusic != null) {
                btnPlayMusic.setText(context.getString(R.string.md_play_arrow));
            }

            musicProgress = 0;

            if (!isShowMediaPlayer) {

                if (onCompleteChat != null) {
                    onCompleteChat.complete(true, "play", "");
                    onCompleteChat.complete(false, "updateTime", zeroTime);
                } else {
                    if (FragmentChat.onMusicListener != null) {
                        FragmentChat.onMusicListener.complete(true, MusicPlayer.messageId, "");
                    }
                }
            } else if (onComplete != null) {
                onComplete.complete(true, "play", "");
                onComplete.complete(true, "updateTime", zeroTime);
            }
            stopTimer();
        } catch (Exception e) {
        }

        if (mp != null) {
            mp.stop();
        }
    }

    public static void nextMusic() {

        if (!canDoAction) {
            return;
        }
        canDoAction = false;

        try {
            String beforMessageID = MusicPlayer.messageId;

            selectedMedia++;
            if (selectedMedia < mediaList.size()) {

                RealmRoomMessage _rm = mediaList.get(selectedMedia).getForwardMessage() != null ? mediaList.get(selectedMedia).getForwardMessage() : mediaList.get(selectedMedia);

                startPlayer(_rm.getAttachment().getName(), _rm.getAttachment().getLocalFilePath(), roomName, roomId, false, mediaList.get(selectedMedia).getMessageId() + "");
            } else {
                selectedMedia = 0;

                RealmRoomMessage _rm = mediaList.get(selectedMedia).getForwardMessage() != null ? mediaList.get(selectedMedia).getForwardMessage() : mediaList.get(selectedMedia);

                startPlayer(_rm.getAttachment().getName(), _rm.getAttachment().getLocalFilePath(), roomName, roomId, false, mediaList.get(selectedMedia).getMessageId() + "");
            }
            if (FragmentChat.onMusicListener != null) {
                FragmentChat.onMusicListener.complete(true, MusicPlayer.messageId, beforMessageID);
            }
        } catch (Exception e) {

            Log.e("dddd", "music player        nextMusic   " + e.toString());
        }
    }

    private static void nextRandomMusic() {

        try {

            String beforMessageID = MusicPlayer.messageId;
            Random r = new Random();
            selectedMedia = r.nextInt(mediaList.size() - 1);

            RealmRoomMessage _rm = mediaList.get(selectedMedia).getForwardMessage() != null ? mediaList.get(selectedMedia).getForwardMessage() : mediaList.get(selectedMedia);

            startPlayer(_rm.getAttachment().getName(), _rm.getAttachment().getLocalFilePath(), roomName, roomId, false, mediaList.get(selectedMedia).getMessageId() + "");

            if (FragmentChat.onMusicListener != null) {
                FragmentChat.onMusicListener.complete(true, MusicPlayer.messageId, beforMessageID);
            }
        } catch (Exception e) {

            Log.e("dddd", "music player        nextRandomMusic   " + e.toString());
        }
    }

    public static void previousMusic() {

        try {
            if (MusicPlayer.mp != null) {

                if (MusicPlayer.mp.getCurrentPosition() > 10000) {

                    musicProgress = 0;

                    MusicPlayer.mp.seekTo(0);
                    time = MusicPlayer.mp.getCurrentPosition();
                    updatePlayerTime();

                    return;
                }
            }
        } catch (IllegalStateException e) {
        }

        if (!canDoAction) {
            return;
        }
        canDoAction = false;

        try {
            selectedMedia--;

            String beforMessageID = MusicPlayer.messageId;

            if (selectedMedia >= 0) {

                RealmRoomMessage _rm = mediaList.get(selectedMedia).getForwardMessage() != null ? mediaList.get(selectedMedia).getForwardMessage() : mediaList.get(selectedMedia);
                startPlayer(_rm.getAttachment().getName(), _rm.getAttachment().getLocalFilePath(), roomName, roomId, false, mediaList.get(selectedMedia).getMessageId() + "");
            } else {
                int index = mediaList.size() - 1;
                if (index >= 0) {
                    selectedMedia = index;

                    RealmRoomMessage _rm = mediaList.get(selectedMedia).getForwardMessage() != null ? mediaList.get(selectedMedia).getForwardMessage() : mediaList.get(selectedMedia);

                    startPlayer(_rm.getAttachment().getName(), _rm.getAttachment().getLocalFilePath(), roomName, roomId, false, mediaList.get(selectedMedia).getMessageId() + "");
                }
            }

            if (FragmentChat.onMusicListener != null) {
                FragmentChat.onMusicListener.complete(true, MusicPlayer.messageId, beforMessageID);
            }
        } catch (Exception e) {

            Log.e("dddd", "music player        previousMusic   " + e.toString());
        }
    }

    private static void closeLayoutMediaPlayer() {

        try {

            isMusicPlyerEnable = false;

            if (layoutTripMusic != null) {
                layoutTripMusic.setVisibility(View.GONE);
            }

            if (onComplete != null) {
                onComplete.complete(true, "finish", "");
            }

            if (onCompleteChat != null) {
                onCompleteChat.complete(true, "pause", "");
            }

            stopSound();

            if (mp != null) {
                mp.release();
                mp = null;
            }
        } catch (Exception E) {

        }

        // clearWallpaperLockScrean();

        setMedaiInfoOnLockScreen(true);

        try {

            Intent intent = new Intent(context, MusicPlayer.class);
            intent.putExtra("ACTION", STOPFOREGROUND_ACTION);
            context.startService(intent);
        } catch (RuntimeException e) {

            if (notificationManager != null) {
                notificationManager.cancel(notificationId);
            }
        }

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
            mRealm = null;
        }
    }

    public static void startPlayer(String name, String musicPath, String roomName, long roomId, final boolean updateList, String messageID) {

        G.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                canDoAction = true;
            }
        }, 1000);

        isVoice = false;
        isPause = false;

        if (messageID != null && messageID.length() > 0) {

            try {

                RealmRoomMessage realmRoomMessage = getmRealm().where(RealmRoomMessage.class).equalTo(RealmRoomMessageFields.MESSAGE_ID, Long.parseLong(messageID)).findFirst();

                if (realmRoomMessage != null) {
                    String type = realmRoomMessage.getForwardMessage() != null ? realmRoomMessage.getForwardMessage().getMessageType().toString() : realmRoomMessage.getMessageType().toString();

                    if (type.equals("VOICE")) {
                        isVoice = true;
                    }
                }
            } catch (Exception e) {
                HelperLog.setErrorLog(" music plyer   startPlayer   setISVoice    " + messageID + "    " + e.toString());
            }
        }

        MusicPlayer.messageId = messageID;
        MusicPlayer.musicPath = musicPath;
        MusicPlayer.roomName = roomName;
        mediaThumpnail = null;
        MusicPlayer.roomId = roomId;

        if (layoutTripMusic != null) {
            layoutTripMusic.setVisibility(View.VISIBLE);
        }

        try {

            if (name == null) {
                name = "";
            }

            if (name.length() > 0) {
                musicName = name;
            } else if (musicPath != null && musicPath.length() > 0) {
                musicName = musicPath.substring(musicPath.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {

        }

        try {

            if (mp != null) {

                mp.setOnCompletionListener(null);

                mp.stop();
                mp.reset();
                mp.release();
            }

            mp = new MediaPlayer();
        } catch (Exception e) {

        }

        try {
            mp.setDataSource(musicPath);

            if (isVoice) {
                mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            } else {
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            mp.prepare();

            mp.start();

            musicTime = milliSecondsToTimer((long) mp.getDuration());
            txt_music_time.setText(musicTime);
            btnPlayMusic.setText(context.getString(R.string.md_pause_button));
            txt_music_name.setText(musicName);

            updateProgress();

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    OnCompleteMusic();
                }
            });

            if (onComplete != null) {
                onComplete.complete(true, "update", "");
            }
        } catch (Exception e) {
        }

        updateNotification();

        if (!isShowMediaPlayer) {

            if (onCompleteChat != null) {
                onCompleteChat.complete(true, "pause", "");
            }
        } else if (onComplete != null) {
            onComplete.complete(true, "pause", "");
        }

        if (updateList || downloadNewItem) {
            fillMediaList(true);
            downloadNewItem = false;
        }

        setSpeaker();

        if (HelperCalander.isLanguagePersian) {
            txt_music_time.setText(HelperCalander.convertToUnicodeFarsiNumber(txt_music_time.getText().toString()));
        }

        isMusicPlyerEnable = true;
    }

    private static void OnCompleteMusic() {
        try {
            if (repeatMode.equals(RepeatMode.noRepeat.toString())) {
                stopSound();
            } else if (repeatMode.equals(RepeatMode.repeatAll.toString())) {

                if (playNextMusic) {

                    fillMediaList(true);

                    nextMusic();
                    if (FragmentChat.onMusicListener != null) {
                        FragmentChat.onMusicListener.complete(false, MusicPlayer.messageId, "");
                    } else {
                        downloadNextMusic(MusicPlayer.messageId);
                    }
                } else {

                    if (isShuffelOn) {
                        nextRandomMusic();
                    } else {

                        nextMusic();
                    }
                }
            } else if (repeatMode.equals(RepeatMode.oneRpeat.toString())) {
                stopSound();
                playAndPause();
            }
        } catch (Exception e) {

        }
    }

    public static String milliSecondsToTimer(long milliseconds) {

        if (milliseconds == -1) return " ";

        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static void updateNotification() {

        getMusicInfo();

        Intent intentFragmentMusic = new Intent(context, ActivityMain.class);
        intentFragmentMusic.putExtra(ActivityMain.openMediaPlyer, true);

        PendingIntent pi = PendingIntent.getActivity(context, 555, intentFragmentMusic, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setTextViewText(R.id.mln_txt_music_name, MusicPlayer.musicName);
        remoteViews.setTextViewText(R.id.mln_txt_music_outher, MusicPlayer.musicInfoTitle);

        //if (mp != null) {
        //    if (mp.isPlaying()) {
        remoteViews.setImageViewResource(R.id.mln_btn_play_music, R.mipmap.pause_button);
        //    } else {
        //        remoteViews.setImageViewResource(R.id.mln_btn_play_music, R.mipmap.play_button);
        //    }
        //}

        Intent intentPrevious = new Intent(context, customButtonListener.class);
        intentPrevious.putExtra("mode", "previous");
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 1, intentPrevious, 0);
        remoteViews.setOnClickPendingIntent(R.id.mln_btn_Previous_music, pendingIntentPrevious);

        Intent intentPlayPause = new Intent(context, customButtonListener.class);
        intentPlayPause.putExtra("mode", "play");
        PendingIntent pendingIntentPlayPause = PendingIntent.getBroadcast(context, 2, intentPlayPause, 0);
        remoteViews.setOnClickPendingIntent(R.id.mln_btn_play_music, pendingIntentPlayPause);

        Intent intentforward = new Intent(context, customButtonListener.class);
        intentforward.putExtra("mode", "forward");
        PendingIntent pendingIntentforward = PendingIntent.getBroadcast(context, 3, intentforward, 0);
        remoteViews.setOnClickPendingIntent(R.id.mln_btn_forward_music, pendingIntentforward);

        Intent intentClose = new Intent(context, customButtonListener.class);
        intentClose.putExtra("mode", "close");
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 4, intentClose, 0);
        remoteViews.setOnClickPendingIntent(R.id.mln_btn_close, pendingIntentClose);

        notification = new NotificationCompat.Builder(context.getApplicationContext()).setTicker("music").setSmallIcon(R.mipmap.j_mp3).setContentTitle(musicName)
            //  .setContentText(place)
            .setContent(remoteViews).setContentIntent(pi).setDeleteIntent(pendingIntentClose).setAutoCancel(false).setOngoing(true).build();

        Intent intent = new Intent(context, MusicPlayer.class);
        intent.putExtra("ACTION", STARTFOREGROUND_ACTION);
        context.startService(intent);
    }

    public static void fillMediaList(boolean setSelectedItem) {

        mediaList = new ArrayList<>();

        RealmResults<RealmRoomMessage> roomMessages = getmRealm().where(RealmRoomMessage.class)
            .equalTo(RealmRoomMessageFields.ROOM_ID, roomId)
            .notEqualTo(RealmRoomMessageFields.CREATE_TIME, 0)
            .equalTo(RealmRoomMessageFields.DELETED, false)
            .equalTo(RealmRoomMessageFields.SHOW_MESSAGE, true)
            .findAllSorted(RealmRoomMessageFields.MESSAGE_ID);

        if (!roomMessages.isEmpty()) {
            for (RealmRoomMessage realmRoomMessage : roomMessages) {

                RealmRoomMessage _rm = realmRoomMessage.getForwardMessage() != null ? realmRoomMessage.getForwardMessage() : realmRoomMessage;

                if (_rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.VOICE.toString())
                    || _rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.AUDIO.toString())
                    || _rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.AUDIO_TEXT.toString())) {
                    try {

                        if (_rm.getAttachment().getLocalFilePath() != null) {
                            if (new File(_rm.getAttachment().getLocalFilePath()).exists()) {
                                mediaList.add(realmRoomMessage);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("dddd", "music player   fillMediaList " + e.toString());
                    }
                }
            }
        }

        if (setSelectedItem) {

            for (int i = mediaList.size() - 1; i >= 0; i--) {

                try {

                    RealmRoomMessage _rm = mediaList.get(i).getForwardMessage() != null ? mediaList.get(i).getForwardMessage() : mediaList.get(i);

                    if (_rm.getAttachment().getLocalFilePath().equals(musicPath)) {
                        selectedMedia = i;
                        break;
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    private static void updateProgress() {

        stopTimer();

        double duration = MusicPlayer.mp.getDuration();
        amoungToupdate = duration / 100;
        time = MusicPlayer.mp.getCurrentPosition();
        musicProgress = ((int) (time / amoungToupdate));

        mTimeSecend = new Timer();

        mTimeSecend.schedule(new TimerTask() {
            @Override
            public void run() {

                updatePlayerTime();
                time += 1000;
            }
        }, 0, 1000);

        if (amoungToupdate >= 1) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {

                    if (musicProgress < 100) {
                        musicProgress++;
                    } else {
                        stopTimer();
                    }
                }
            }, 0, (int) amoungToupdate);
        }
    }

    private static void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimeSecend != null) {
            mTimeSecend.cancel();
            mTimeSecend = null;
        }
    }

    private static void updatePlayerTime() {

        strTimer = MusicPlayer.milliSecondsToTimer(time);

        if (HelperCalander.isLanguagePersian) {
            strTimer = HelperCalander.convertToUnicodeFarsiNumber(strTimer);
        }

        if (txt_music_time_counter != null) {
            G.handler.post(new Runnable() {
                @Override
                public void run() {
                    txt_music_time_counter.setText(strTimer);
                }
            });
        }

        if (isShowMediaPlayer) {
            if (onComplete != null) {
                onComplete.complete(true, "updateTime", strTimer);
            }
        } else {
            if (onCompleteChat != null) {
                onCompleteChat.complete(true, "updateTime", strTimer);
            }
        }
    }

    public static void setMusicProgress(int percent) {
        try {
            musicProgress = percent;
            if (MusicPlayer.mp != null) {
                MusicPlayer.mp.seekTo((int) (musicProgress * amoungToupdate));
                time = MusicPlayer.mp.getCurrentPosition();
                updatePlayerTime();
            }
        } catch (IllegalStateException e) {
        }
    }

    private static void getMusicInfo() {

        musicInfo = "";
        musicInfoTitle = context.getString(R.string.unknown_artist);

        MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();

        Uri uri = null;

        if (MusicPlayer.musicPath != null) {
            uri = (Uri) Uri.fromFile(new File(MusicPlayer.musicPath));
        }

        if (uri != null) {

            try {

                mediaMetadataRetriever.setDataSource(context, uri);

                String title = (String) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                if (title != null) {
                    musicInfo += title + "       ";
                    musicInfoTitle = title;
                }

                String albumName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                if (albumName != null) {
                    musicInfo += albumName + "       ";
                    musicInfoTitle = albumName;
                }

                String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (artist != null) {
                    musicInfo += artist + "       ";
                    musicInfoTitle = artist;
                }

                byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
                if (data != null) {
                    mediaThumpnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //  setWallpaperLockScreen(mediaThumpnail);
                    G.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setMedaiInfoOnLockScreen(false);
                        }
                    }, 100);

                    int size = (int) context.getResources().getDimension(R.dimen.dp48);
                    remoteViews.setImageViewBitmap(R.id.mln_img_picture_music, Bitmap.createScaledBitmap(mediaThumpnail, size, size, false));
                } else {
                    remoteViews.setImageViewResource(R.id.mln_img_picture_music, R.mipmap.music_icon_green);
                    // clearWallpaperLockScrean();
                    setMedaiInfoOnLockScreen(true);
                }
            } catch (Exception e) {

                Log.e("debug", " music plyer   getMusicInfo    " + uri + "       " + e.toString());
            }
        }
    }

    private static void getAtribuits() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MusicSetting", context.MODE_PRIVATE);
        repeatMode = sharedPreferences.getString("RepeatMode", RepeatMode.noRepeat.toString());
        isShuffelOn = sharedPreferences.getBoolean("Shuffel", false);
    }

    public enum RepeatMode {
        noRepeat, oneRpeat, repeatAll;
    }

    public static class customButtonListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String str = intent.getExtras().getString("mode");

            if (str.equals("previous")) {
                previousMusic();
                MusicPlayer.canDoAction = false;
            } else if (str.equals("play")) {
                playAndPause();
            } else if (str.equals("forward")) {
                nextMusic();
                MusicPlayer.canDoAction = false;
            } else if (str.equals("close")) {
                closeLayoutMediaPlayer();
            }
        }
    }

    public static boolean downloadNextMusic(String messageId) {

        boolean result = false;

        RealmResults<RealmRoomMessage> roomMessages = getmRealm().where(RealmRoomMessage.class).equalTo(RealmRoomMessageFields.ROOM_ID, roomId).
            equalTo(RealmRoomMessageFields.DELETED, false).greaterThan(RealmRoomMessageFields.MESSAGE_ID, Long.parseLong(messageId)).findAllSorted(RealmRoomMessageFields.CREATE_TIME);

        if (!roomMessages.isEmpty()) {
            for (RealmRoomMessage rm : roomMessages) {

                if (rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.VOICE.toString())
                    || rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.AUDIO.toString())
                    || rm.getMessageType().toString().equals(ProtoGlobal.RoomMessageType.AUDIO_TEXT.toString())) {
                    try {

                        if (rm.getAttachment().getLocalFilePath() == null || !new File(rm.getAttachment().getLocalFilePath()).exists()) {

                            ProtoGlobal.RoomMessageType _messageType = rm.getForwardMessage() != null ? rm.getForwardMessage().getMessageType() : rm.getMessageType();
                            String _cashid = rm.getForwardMessage() != null ? rm.getForwardMessage().getAttachment().getCacheId() : rm.getAttachment().getCacheId();
                            String _name = rm.getForwardMessage() != null ? rm.getForwardMessage().getAttachment().getName() : rm.getAttachment().getName();
                            String _token = rm.getForwardMessage() != null ? rm.getForwardMessage().getAttachment().getToken() : rm.getAttachment().getToken();
                            Long _size = rm.getForwardMessage() != null ? rm.getForwardMessage().getAttachment().getSize() : rm.getAttachment().getSize();

                            if (_cashid == null) {
                                return result;
                            }

                            ProtoFileDownload.FileDownload.Selector selector = ProtoFileDownload.FileDownload.Selector.FILE;

                            final String _path = AndroidUtils.getFilePathWithCashId(_cashid, _name, _messageType);

                            if (_token != null && _token.length() > 0 && _size > 0) {

                                if (!new File(_path).exists()) {

                                    result = true;

                                    HelperDownloadFile.startDownload(rm.getMessageId() + "", _token, _cashid, _name, _size, selector, _path, 0, new HelperDownloadFile.UpdateListener() {
                                        @Override
                                        public void OnProgress(String path, int progress) {
                                            if (progress == 100) {
                                                downloadNewItem = true;
                                            }
                                        }

                                        @Override
                                        public void OnError(String token) {

                                        }
                                    });

                                    MusicPlayer.playNextMusic = true;
                                } else {
                                    MusicPlayer.playNextMusic = false;
                                }
                            }
                        }

                        break;
                    } catch (Exception e) {
                        Log.e("dddd", "music player   fillMediaList " + e.toString());
                    }
                }
            }
        }

        return result;
    }

    //***************************************************************************** sensor *********************************

    private static void initSensore() {

        if (!isRegisterSensore) {

            registerMediaBottom();

            headsetPluginReciver = new HeadsetPluginReciver();

            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                        if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                            // near
                            isNearDistance = true;
                            if (isVoice) {
                                setSpeaker();
                            }
                        } else {
                            //far
                            isNearDistance = false;
                            if (isVoice) {
                                setSpeaker();
                            }
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            context.registerReceiver(headsetPluginReciver, filter);
            registerDistanceSensor();

            isRegisterSensore = true;
        }
    }

    private static void removeSensore() {

        if (isRegisterSensore) {

            isRegisterSensore = false;

            AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);

            try {
                if (remoteComponentName != null) {
                    audioManager.unregisterMediaButtonEventReceiver(remoteComponentName);
                }
            } catch (Exception e) {
                Log.e("ddddd", "music plyer  removeSensore   audioManager   " + e.toString());
            }

            try {
                if (remoteControlClient != null) {
                    audioManager.unregisterRemoteControlClient(remoteControlClient);
                }
            } catch (Exception e) {
                Log.e("ddddd", "music plyer  removeSensore   remoteControlClient   " + e.toString());
            }

            try {

                context.unregisterReceiver(headsetPluginReciver);
            } catch (Exception e) {
                Log.e("ddddd", "music plyer  removeSensore    unregisterReceiver " + e.toString());
            }

            unRegisterDistanceSensore();

            remoteComponentName = null;
            remoteControlClient = null;

            // clearWallpaperLockScrean();

        }
    }

    private static void registerDistanceSensor() {

        try {

            mSensorManager.registerListener(sensorEventListener, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception e) {
            Log.e("dddd", "music player registerDistanceSensor   " + e.toString());
        }
    }

    private static void unRegisterDistanceSensore() {

        try {
            mSensorManager.unregisterListener(sensorEventListener);
        } catch (Exception e) {
            Log.e("dddd", "music player unRegisterDistanceSensore   " + e.toString());
        }
    }

    //*************************************************************************************** getPhoneState

    static class HeadsetPluginReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {

                int state = intent.getIntExtra("state", -1);

                if (state != stateHedset) {

                    stateHedset = state;

                    switch (state) {
                        case 0:

                            setSpeaker();

                            if (mp != null && mp.isPlaying()) {
                                pauseSound();
                            }

                            //  Log.d("dddddd", "Headset is unplugged");
                            break;
                        case 1:
                            setSpeaker();

                            //  Log.d("dddddd", "Headset is plugged");
                            break;
                        //default:
                        //    Log.d("dddddd", "I have no idea what the headset state is");
                    }
                }
            }
        }
    }

    private static void registerMediaBottom() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                try {

                    mSession = new MediaSessionCompat(context, context.getPackageName());
                    Intent intent = new Intent(context, MediaBottomReciver.class);
                    PendingIntent pintent = PendingIntent.getBroadcast(context, 50, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mSession.setMediaButtonReceiver(pintent);
                    mSession.setActive(true);

                    PlaybackStateCompat state = new PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1, SystemClock.elapsedRealtime()).build();
                    mSession.setPlaybackState(state);
                } catch (Exception e) {

                    HelperLog.setErrorLog(" music player   registerMediaBottom     " + e.toString());
                }
            } else {

                try {
                    remoteComponentName = new ComponentName(context, MediaBottomReciver.class.getName());
                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                } catch (Exception e) {
                    HelperLog.setErrorLog(" music plyer   registerMediaBottom    " + e.toString());
                }
            }

            if (remoteComponentName != null) {
                remoteComponentName = new ComponentName(context, MediaBottomReciver.class.getName());
            }

            try {

                if (remoteControlClient == null) {

                    Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    mediaButtonIntent.setComponent(remoteComponentName);
                    PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 55, mediaButtonIntent, 0);
                    remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    audioManager.registerRemoteControlClient(remoteControlClient);

                    remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                        | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                        | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                        | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                        | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                        | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
                }
            } catch (Exception e) {
                HelperLog.setErrorLog(" music plyer   setMediaControl    " + e.toString());
            }
        }
    }

    private static void setMedaiInfoOnLockScreen(boolean clear) {

        try {

            if (remoteControlClient != null) {

                RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);

                if (clear) {

                    metadataEditor.clear();
                } else {
                    metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, musicName + "");
                    metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, musicInfoTitle + "");
                    try {
                        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mediaThumpnail);
                        // seMediaSesionMetaData();

                    } catch (Throwable e) {
                    }
                }

                metadataEditor.apply();
            }
        } catch (Exception e) {
            HelperLog.setErrorLog(" music plyer   setMedoiInfoOnLockScreen    " + e.toString());
        }
    }

    //    private static void seMediaSesionMetaData() {
    //        if (mSession != null) {
    //
    //            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
    //            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "aaaaaaa");
    //            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "bbbbbbb");
    //            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "ccccccccc");
    //            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 1234);
    //            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mediaThumpnail);
    //            mSession.setMetadata(builder.build());
    //
    //
    //        }
    //    }


    /*private static void setWallpaperLockScreen(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(G.context);

            try {

                if (myWallpaperManager.isSetWallpaperAllowed()) {
                    myWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                }

            } catch (Exception e) {

            }
        }
    }

    private static void clearWallpaperLockScrean() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                WallpaperManager myWallpaperManager = WallpaperManager.getInstance(G.context);
                if (myWallpaperManager.isSetWallpaperAllowed()) {

                    if (orginalWallPaper != null) {
                        myWallpaperManager.setBitmap(orginalWallPaper, null, true, WallpaperManager.FLAG_LOCK);
                    } else {
                        myWallpaperManager.clear(WallpaperManager.FLAG_LOCK);
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private static void setSpeaker() {

        if (pauseSoundFromCall || pauseSoundFromIGapCall) {
            return;
        }

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (isVoice) {

            if (am.isWiredHeadsetOn()) {
                am.setSpeakerphoneOn(false);
            } else {

                if (isNearDistance) {
                    am.setSpeakerphoneOn(false);
                } else {
                    am.setSpeakerphoneOn(true);
                }
            }
        } else {
            am.setSpeakerphoneOn(false);
        }

        isSpeakerON = am.isSpeakerphoneOn();
    }
}

