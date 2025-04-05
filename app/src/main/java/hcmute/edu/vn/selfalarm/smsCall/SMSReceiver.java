package hcmute.edu.vn.selfalarm.smsCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    private static SMSUpdateListener smsUpdateListener;

    public static void setSMSUpdateListener(SMSUpdateListener listener) {
        smsUpdateListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = message.getOriginatingAddress();
                        String content = message.getMessageBody();
                        
                        // Check if sender is blacklisted
                        if (BlacklistService.isBlacklisted(sender)) {
                            // Delete the message
                            BlacklistService.deleteSMS(context, sender);
                            Toast.makeText(context, "Blocked SMS from: " + sender, Toast.LENGTH_SHORT).show();
                        } else {
                            // Show notification for non-blacklisted senders
                            Toast.makeText(context, "New SMS from: " + sender, Toast.LENGTH_SHORT).show();
                            
                            // Notify listener to update UI
                            if (smsUpdateListener != null) {
                                smsUpdateListener.onSMSReceived();
                            }
                        }
                    }
                }
            }
        }
    }
} 