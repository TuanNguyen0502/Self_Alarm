package hcmute.edu.vn.selfalarm.smsCall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class BlacklistService extends Service {
    private static final String TAG = "BlacklistService";
    private static BlacklistDatabaseHelper dbHelper;
    private static BlacklistService instance;
    private BroadcastReceiver bootReceiver;
    private BroadcastReceiver callReceiver;
    private BroadcastReceiver smsReceiver;
    private TelephonyManager telephonyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BlacklistService onCreate");
        instance = this;
        
        // Tạo database mới nếu chưa tồn tại
        dbHelper = new BlacklistDatabaseHelper(this);
        
        // Initialize TelephonyManager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        
        // Register boot receiver
        bootReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                    Log.d(TAG, "Device boot completed, starting service");
                    context.startService(new Intent(context, BlacklistService.class));
                }
            }
        };
        
        // Register call receiver
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && phoneNumber != null) {
                        if (isBlacklisted(phoneNumber)) {
                            Log.d(TAG, "Rejecting call from blacklisted number: " + phoneNumber);
                            rejectCall(context);
                        }
                    }
                }
            }
        };

        // Register SMS receiver
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                    String phoneNumber = intent.getStringExtra("phone_number");
                    if (phoneNumber != null && isBlacklisted(phoneNumber)) {
                        Log.d(TAG, "Deleting SMS from blacklisted number: " + phoneNumber);
                        deleteSMS(context, phoneNumber);
                    }
                }
            }
        };
        
        // Register receivers
        IntentFilter bootFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootReceiver, bootFilter);
        
        IntentFilter callFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, callFilter);
        
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

    public static void rejectCall(Context context) {
        try {
            Log.d(TAG, "Attempting to reject call using ITelephony");
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
            Method getITelephonyMethod = telephonyClass.getDeclaredMethod("getITelephony");
            getITelephonyMethod.setAccessible(true);
            Object telephonyInterface = getITelephonyMethod.invoke(telephonyManager);
            Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method endCallMethod = telephonyInterfaceClass.getDeclaredMethod("endCall");
            endCallMethod.invoke(telephonyInterface);
            Log.d(TAG, "Call rejected successfully using ITelephony");
        } catch (Exception e) {
            Log.e(TAG, "Error rejecting call using ITelephony: " + e.getMessage());
            // Fallback to headset button method
            try {
                Log.d(TAG, "Trying fallback method using headset button");
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_HEADSETHOOK));
                context.sendOrderedBroadcast(intent, null);

                intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_HEADSETHOOK));
                context.sendOrderedBroadcast(intent, null);
                Log.d(TAG, "Call rejection broadcast sent using fallback method");
            } catch (Exception ex) {
                Log.e(TAG, "Error in fallback method: " + ex.getMessage());
            }
        }
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
        instance = null;
        if (bootReceiver != null) {
            unregisterReceiver(bootReceiver);
        }
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }
} 