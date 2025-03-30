package hcmute.edu.vn.selfalarm.musicPlayer;

import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_CANCEL;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_NEXT;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_PLAY;
import static hcmute.edu.vn.selfalarm.musicPlayer.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action_name = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);
        if (action_name != null) {
            switch (action_name) {
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    break;
                case ACTION_CANCEL:
                    serviceIntent.putExtra("ActionName", "cancel");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}