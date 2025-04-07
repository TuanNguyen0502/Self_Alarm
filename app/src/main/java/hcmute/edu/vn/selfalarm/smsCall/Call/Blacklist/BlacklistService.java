package hcmute.edu.vn.selfalarm.smsCall.Call.Blacklist;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class BlacklistService extends Service {
    private static final String TAG = "BlacklistService";
    private static BlacklistDatabaseHelper dbHelper;
    private BroadcastReceiver smsReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BlacklistService onCreate");

        // Tạo database mới nếu chưa tồn tại
        dbHelper = new BlacklistDatabaseHelper(this);


        // Register sms receiver
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        if (pdus != null && pdus.length > 0) {
                            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
                            String phoneNumber = message.getDisplayOriginatingAddress();

                            if (phoneNumber != null && isBlacklisted(phoneNumber)) {
                                Log.d(TAG, "Deleting SMS from blacklisted number: " + phoneNumber);
                                deleteSMS(context, phoneNumber);
                                abortBroadcast(); // Ngăn không cho app mặc định nhận
                            }
                        }
                    }
                }
            }
        };

        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, smsFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BlacklistService onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isBlacklisted(String phoneNumber) {
        if (dbHelper == null) {
            Log.e(TAG, "Database helper is null");
            return false;
        }
        boolean isBlacklisted = dbHelper.isBlacklisted(phoneNumber);
        Log.d(TAG, "Checking if " + phoneNumber + " is blacklisted: " + isBlacklisted);
        return isBlacklisted;
    }

    public static boolean addToBlacklist(String phoneNumber) {
        if (dbHelper == null) {
            Log.e(TAG, "Database helper is null");
            return false;
        }
        boolean result = dbHelper.addToBlacklist(phoneNumber);
        Log.d(TAG, "Adding " + phoneNumber + " to blacklist: " + result);
        return result;
    }

    public static boolean removeFromBlacklist(String phoneNumber) {
        if (dbHelper == null) {
            Log.e(TAG, "Database helper is null");
            return false;
        }
        boolean result = dbHelper.removeFromBlacklist(phoneNumber);
        Log.d(TAG, "Removing " + phoneNumber + " from blacklist: " + result);
        return result;
    }

    public static List<String> getAllBlacklistedNumbers() {
        if (dbHelper == null) {
            Log.e(TAG, "Database helper is null");
            return null;
        }
        List<String> numbers = dbHelper.getAllBlacklistedNumbers();
        Log.d(TAG, "Getting all blacklisted numbers: " + (numbers != null ? numbers.size() : 0));
        return numbers;
    }

    public static void deleteSMS(Context context, String phoneNumber) {
        try {
            Log.d(TAG, "Attempting to delete SMS from: " + phoneNumber);
            context.getContentResolver().delete(
                    android.provider.Telephony.Sms.CONTENT_URI,
                    "address = ?",
                    new String[]{phoneNumber}
            );
            Log.d(TAG, "SMS deletion completed");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting SMS: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BlacklistService onDestroy");
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }
}
