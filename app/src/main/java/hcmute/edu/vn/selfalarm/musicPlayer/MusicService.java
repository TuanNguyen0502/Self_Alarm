package hcmute.edu.vn.selfalarm.musicPlayer;

import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_CANCEL;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_NEXT;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_PLAY;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_PREVIOUS;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.CHANNEL_ID_2;
import static hcmute.edu.vn.selfalarm.musicPlayer.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import hcmute.edu.vn.selfalarm.R;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private IBinder myBinder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    private Uri uri;
    private int position = -1;
    private ActionPlaying actionPlaying;
    private MediaSessionCompat mediaSessionCompat;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, HeadphoneReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio", null, pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1) {
            playMedia(myPosition);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    if (actionPlaying != null) {
                        actionPlaying.playPauseBtnClick();
                    }
                    break;
                case "next":
                    if (actionPlaying != null) {
                        actionPlaying.nextBtnClick();
                    }
                    break;
                case "previous":
                    if (actionPlaying != null) {
                        actionPlaying.prevBtnClick();
                    }
                    break;
                case "cancel":
                    if (actionPlaying != null) {
                        stopMusicService();
                    }
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public void start() {
        mediaPlayer.start();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Handle completion
        });
    }

    public void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClick();
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                start();
                OnCompleted();
            }
        }
    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn) throws IOException {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_CANCEL);
        PendingIntent cancelPending = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_prev, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .addAction(R.drawable.baseline_cancel_24, "Cancel", cancelPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)  // ⬅ Prevents user from dismissing the notification
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)  // ⬅ Ensures visibility
                .build();
        startForeground(2, notification);
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public void stopMusicService() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true); // Remove the foreground notification
        stopSelf(); // Stop the service
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);  // Remove notification
        stopSelf();            // Stop the service
        super.onTaskRemoved(rootIntent);
    }
}