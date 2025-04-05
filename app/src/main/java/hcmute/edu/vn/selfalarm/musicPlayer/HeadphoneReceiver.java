package hcmute.edu.vn.selfalarm.musicPlayer;

import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_NEXT;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

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
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
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
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
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
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        if (musicService != null) {
                            Log.d(TAG, "onReceive: Next music button pressed");
                            musicService.stop();
                            musicService.release();
                            Intent serviceIntent = new Intent(context, MusicService.class);
                            serviceIntent.putExtra("ActionName", ACTION_NEXT);
                            context.startService(serviceIntent);
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        if (musicService != null) {
                            Log.d(TAG, "onReceive: Previous music button pressed");
                            musicService.stop();
                            musicService.release();
                            Intent serviceIntent = new Intent(context, MusicService.class);
                            serviceIntent.putExtra("ActionName", ACTION_PREVIOUS);
                            context.startService(serviceIntent);
                        }
                        break;
                }
            }
        }
    }
}