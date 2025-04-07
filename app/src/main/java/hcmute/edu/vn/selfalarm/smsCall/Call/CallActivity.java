package hcmute.edu.vn.selfalarm.smsCall.Call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.selfalarm.R;

public class CallActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE
    };

    private TextView currentCallStatus;
    private RecyclerView callHistoryRecyclerView;
    private CallHistoryAdapter callHistoryAdapter;
    private FloatingActionButton fabMakeCall;
    private BroadcastReceiver callStatusReceiver;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Initialize views
        currentCallStatus = findViewById(R.id.currentCallStatus);
        callHistoryRecyclerView = findViewById(R.id.callHistoryRecyclerView);
        fabMakeCall = findViewById(R.id.fabMakeCall);

        // Setup RecyclerView
        callHistoryAdapter = new CallHistoryAdapter();
        callHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        callHistoryRecyclerView.setAdapter(callHistoryAdapter);

        // Setup FAB
        fabMakeCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
        });

        // Setup BroadcastReceiver for call status updates
        callStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String number = intent.getStringExtra("number");
                String state = intent.getStringExtra("state");
                updateCallStatus(number, state);
            }
        };
        registerReceiver(callStatusReceiver, new IntentFilter("hcmute.edu.vn.managecall.UPDATE_CALL_STATUS"));

        // Check and request permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            loadCallHistory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadCallHistory();
            } else {
                Toast.makeText(this, "Permissions required for app functionality", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadCallHistory() {
        List<CallItem> callHistoryItems = new ArrayList<>();
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
        };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                String callTime = sdf.format(new Date(date));

                callHistoryItems.add(new CallItem(number, callTime, type));
            }
            cursor.close();
        }
        callHistoryAdapter.setCallHistoryItems(callHistoryItems);
    }

    private void updateCallStatus(String number, String state) {
        String statusText = "";
        switch (state) {
            case "RINGING":
                statusText = "Incoming call from: " + number;
                break;
            case "OFFHOOK":
                statusText = "Call in progress";
                break;
            case "IDLE":
                statusText = "Call ended";
                break;
        }
        currentCallStatus.setText(statusText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callStatusReceiver != null) {
            unregisterReceiver(callStatusReceiver);
        }
    }
}