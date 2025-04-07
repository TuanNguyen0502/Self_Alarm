package hcmute.edu.vn.selfalarm.smsCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = "IDLE";
    private static final String STATE_IDLE = "IDLE";
    private static final String STATE_RINGING = "RINGING";
    private static final String STATE_OFFHOOK = "OFFHOOK";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (state != null && phoneNumber != null) {
                switch (state) {
                    case STATE_RINGING:
                        lastState = state;
                        handleIncomingCall(context, phoneNumber);
                        break;
                    case STATE_OFFHOOK:
                        if (lastState.equals(STATE_RINGING)) {
                            // Call was answered
                            handleAnsweredCall(context, phoneNumber);
                        }
                        break;
                    case STATE_IDLE:
                        if (lastState.equals(STATE_RINGING)) {
                            // Call was rejected or missed
                            handleMissedCall(context, phoneNumber);
                        }
                        break;
                }
            }
        }
    }

    private void handleIncomingCall(Context context, String phoneNumber) {
        if (BlacklistService.isBlacklisted(phoneNumber)) {
            // Reject the call
            BlacklistService.rejectCall(context);
            showBlockedNotification(context, phoneNumber);
        } else {
            // Forward to CallActivity
            forwardToCallActivity(context, phoneNumber);
        }
    }

    private void handleAnsweredCall(Context context, String phoneNumber) {
        Log.d(TAG, "Call answered from: " + phoneNumber);
    }

    private void handleMissedCall(Context context, String phoneNumber) {
        Log.d(TAG, "Missed call from: " + phoneNumber);
    }

    private void showBlockedNotification(Context context, String phoneNumber) {
        Toast.makeText(context, "Blocked call from: " + phoneNumber, Toast.LENGTH_SHORT).show();
    }

    private void forwardToCallActivity(Context context, String phoneNumber) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
} 