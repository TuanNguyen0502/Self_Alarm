package hcmute.edu.vn.selfalarm.smsCall.SMS;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarm.R;
import hcmute.edu.vn.selfalarm.smsCall.NotificationHelper;

public class SmsActivity extends AppCompatActivity implements SMSUpdateListener {

    private static final int SMS_PERMISSION_CODE = 123;
    private RecyclerView recyclerView;
    private SMSAdapter adapter;
    private List<SmsItem> smsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Setup Material Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsList = new ArrayList<>();
        adapter = new SMSAdapter(smsList);
        recyclerView.setAdapter(adapter);

        // Set SMS update listener
        SMSReceiver.setSMSUpdateListener(this);

        // Check and request SMS permissions
        checkSMSPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload messages when activity resumes
        loadSMSMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove SMS update listener
        SMSReceiver.setSMSUpdateListener(null);
    }

    private void checkSMSPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            };
        }

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, SMS_PERMISSION_CODE);
        } else {
            loadSMSMessages();
        }
    }

    public void loadSMSMessages() {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, null, null, null, "date DESC");

        if (cursor != null && cursor.moveToFirst()) {
            smsList.clear();
            do {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                SmsItem sms = new SmsItem(address, body, date);
                smsList.add(sms);
            } while (cursor.moveToNext());
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                loadSMSMessages();
            } else {
                Toast.makeText(this, "All permissions are required for the app to function properly", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSMSReceived() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadSMSMessages();
            }
        });
    }
}