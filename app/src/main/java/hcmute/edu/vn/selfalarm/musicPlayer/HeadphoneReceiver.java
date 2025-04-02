package hcmute.edu.vn.selfalarm.musicPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.io.IOException;

import hcmute.edu.vn.selfalarm.R;

public class HeadphoneReceiver extends BroadcastReceiver {

    private static final String TAG = "HeadphoneReceiver";
    private MusicService musicService;

    public HeadphoneReceiver(MusicService musicService) {
        this.musicService = musicService;
    }

    public HeadphoneReceiver() {
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Intent received: " + intent.getAction());
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.d(TAG, "onReceive: Headphones disconnected");
            if (musicService != null && musicService.isPlaying()) {
                Log.d(TAG, "onReceive: Pausing music");
                musicService.pause();
                try {
                    musicService.showNotification(R.drawable.ic_play);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            Log.d(TAG, "onReceive: Headset state: " + state);
            switch (state) {
                case 0:
                    Log.d(TAG, "onReceive: Headset unplugged");
                    if (musicService != null && musicService.isPlaying()) {
                        Log.d(TAG, "onReceive: Pausing music");
                        musicService.pause();
                        try {
                            musicService.showNotification(R.drawable.ic_play);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    Log.d(TAG, "onReceive: Headset plugged");
                    if (musicService != null && !musicService.isPlaying()) {
                        Log.d(TAG, "onReceive: Resuming music");
                        musicService.start();
                        try {
                            musicService.showNotification(R.drawable.ic_pause);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}