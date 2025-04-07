package hcmute.edu.vn.selfalarm.optimizeBattery;

import static android.content.ContentValues.TAG;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class BatteryOptimizerService extends Service {
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver screenReceiver;

//    Lưu trạng thái pin trước đó, giúp tránh xử lí trùng lặp
    private int lastBatteryLevel = -1;
    private boolean lastChargingState = false;

    @Override
    public void onCreate() {
        super.onCreate();

        registerBatteryReceiver();
        registerScreenReceiver();
    }

//    Nhận sự kiện pin
    private void registerBatteryReceiver(){
//        Khởi tạo batteryReceiver để lắng nghe sự kiện thay đổi pin
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                EXTRA_LEVEL: Lấy mức pin hiện tại(0-100)
//                EXTRA_STATUS: Trạng thái sạc(Đang sạc, đầy, không sạc,...)
//                isCharging: Kiểm tra trạng thái sạc có đầy hay không
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_FULL;

//  Lấy mức pin hiện taại và trạng thái từ Intent
//                Xử lí nếu mức pin bị thay đổi/ trạng thái sạc bị thay đổ
                if (level != lastBatteryLevel || isCharging != lastChargingState) {
                    if(level <= 20){
                        Intent intent1 = new Intent("show_dialog");
                        intent1.putExtra("level", level);       // ví dụ: 20
                        intent1.putExtra("charging", isCharging);      // ví dụ: true hoặc false
                        sendBroadcast(intent1);
                    }

                    lastBatteryLevel = level;
                    lastChargingState = isCharging;
                }
            }
        };

//        Tạo ra một Intent để lắng nghe sự kiện thay đổi pin
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

//    Định nghĩa phương thức riêng để cấu hình và
//    đăng kí BroadcastReceiver các sự kiện liên quan
//    đến màn hình
    private void registerScreenReceiver(){
//        Khởi tạo screenReceiver để xử lí bật/tắt màn hình
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Khi màn hình tắt -> giảm sử dụng tài nguyên
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    reduceResourceUsage();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//                    Khi màn hình bật -> khôi phục cài đặt
                    restoreSettings();
                }
            }
        };

//        Đăng kí các sự kiện ON/OFF màn hình
//        Tạo một IntentFilter trống để thêm các loại sự kiện muốn lắng nghe
        IntentFilter filter = new IntentFilter();
//        ACTION_SCREEN_ON: Khi màn hình bật
//        ACTION_SCREEN_OFF: Khi màn hình tắt
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        Nhận các sự kiện theo filter đã tạo
        registerReceiver(screenReceiver, filter);
    }

//    Nếu phần trăm pin <= 20% và đang không sạc ->
//    giảm độ sáng và tắt đồng bộ tự động
    public void adjustSettings(int level, boolean isCharging) {
        if (level <= 20 && !isCharging) {
            int brightness = getScreenBrightness();
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness/2);
            // Disable auto-sync
            toggleAutoSync(false);
        }
    }

    public int getScreenBrightness() {
        return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }

    private void reduceResourceUsage() {
        // Add your resource reduction logic here
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 30);
        // Disable window animations
        Settings.Global.putFloat(getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE, 0.5f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 0.5f);
        Settings.Global.putFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f);
    }

    private void restoreSettings() {
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
        if (intent != null && intent.hasExtra("action") && "adjust_settings".equals(intent.getStringExtra("action"))){
            int level = intent.getIntExtra("level", -1);
            boolean isCharging = intent.getBooleanExtra("charging", false);
            adjustSettings(level, isCharging);
        }

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
