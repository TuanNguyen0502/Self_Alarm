package hcmute.edu.vn.selfalarm.smsCall.Call.Blacklist;

import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.annotation.NonNull;

public class ScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        Uri handle = callDetails.getHandle();
        if (handle == null) return;

        String phoneNumber = handle.getSchemeSpecificPart();
        if (phoneNumber == null) return;

        BlacklistDatabaseHelper dbHelper = new BlacklistDatabaseHelper(this);
        boolean isBlocked = dbHelper.isBlacklisted(phoneNumber);
        dbHelper.close();

        CallResponse.Builder responseBuilder = new CallResponse.Builder();

        if (isBlocked) {
            Log.d("ScreeningService", "Blocking call from: " + phoneNumber);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBuilder
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSilenceCall(true)
                        .setSkipCallLog(true)
                        .setSkipNotification(true);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBuilder
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSilenceCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false);
            }
        }

        respondToCall(callDetails, responseBuilder.build());
    }
}

