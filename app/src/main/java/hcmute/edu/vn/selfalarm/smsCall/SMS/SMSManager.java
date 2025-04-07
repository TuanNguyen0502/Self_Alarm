package hcmute.edu.vn.selfalarm.smsCall.SMS;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Telephony;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SMSManager {
    private static SMSManager instance;
    private List<SMSUpdateListener> listeners;
    private SimpleDateFormat dateFormat;

    private SMSManager() {
        listeners = new ArrayList<>();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public static synchronized SMSManager getInstance() {
        if (instance == null) {
            instance = new SMSManager();
        }
        return instance;
    }

    public void registerListener(SMSUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(SMSUpdateListener listener) {
        listeners.remove(listener);
    }

    public void notifySMSReceived() {
        for (SMSUpdateListener listener : listeners) {
            listener.onSMSReceived();
        }
    }

    public List<SmsItem> getMessages() {
        List<SmsItem> messages = new ArrayList<>();
        Context context = SmsCallActivity.getInstance();

        if (context == null || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return messages;
        }

        String[] projection = new String[] {
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        };

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = Telephony.Sms.DATE + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder)) {

            if (cursor != null) {
                int addressColumn = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                int bodyColumn = cursor.getColumnIndex(Telephony.Sms.BODY);
                int dateColumn = cursor.getColumnIndex(Telephony.Sms.DATE);

                while (cursor.moveToNext()) {
                    String address = cursor.getString(addressColumn);
                    String body = cursor.getString(bodyColumn);
                    long date = cursor.getLong(dateColumn);

                    SmsItem message = new SmsItem(address, body, date);
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    public String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
} 