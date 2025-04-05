package hcmute.edu.vn.selfalarm.optimizeBattery;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class BatteryOptimizerService extends Service {
    private static final String TAG = "BatteryOptimizerService";
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver screenReceiver;

    private int lastBatteryLevel = -1;
    private boolean lastChargingState = false;
    private boolean isChange = false;

    @Override
    public void onCreate() {
        super.onCreate();

        registerBatteryReceiver();
        registerScreenReceiver();
    }

    private void registerBatteryReceiver(){
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_FULL;

                if (level != lastBatteryLevel || isCharging != lastChargingState) {
                    Log.d(TAG, "Battery level changed: " + level + "%, Charging: " + isCharging);
                    adjustSettings(level, isCharging);

                    lastBatteryLevel = level;
                    lastChargingState = isCharging;
                } else {
                    Log.d(TAG, "Battery state unchanged, skipping adjustSettings()");
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    private void registerScreenReceiver(){
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.d(TAG, "Screen turned off");
                    reduceResourceUsage();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    Log.d(TAG, "Screen turned on");
                    restoreSettings();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    private void adjustSettings(int level, boolean isCharging) {
        if (level <= 20 && !isCharging) {
            Log.d(TAG, "Low battery - Lowering brightness");
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
            // Disable auto-sync
            toggleAutoSync(false);
            isCharging = true;
        }
    }

    private void reduceResourceUsage() {
        Log.d(TAG, "Reducing resource usage");
        // Add your resource reduction logic here
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 30);
        // Disable window animations
        Settings.Global.putFloat(getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE, 0.5f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 0.5f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f);
    }

    private void restoreSettings() {
        Log.d(TAG, "Restoring original settings");
        // Restore your original settings here
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
//        Enable auto-sync
        toggleAutoSync(true);
//      Restore window animations
        Settings.Global.putFloat(getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 1.0f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
    }

    private void toggleAutoSync(boolean enable) {
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts) {
            ContentResolver.setMasterSyncAutomatically(enable);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(screenReceiver);
    }
}
