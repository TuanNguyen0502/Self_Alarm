package hcmute.edu.vn.selfalarm.smsCall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import hcmute.edu.vn.selfalarm.R;

public class SmsCallActivity extends AppCompatActivity {
    private static SmsCallActivity instance;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_call);
        instance = this;

        // Start BlacklistService
        Intent serviceIntent = new Intent(this, BlacklistService.class);
        startService(serviceIntent);

        // Initialize ViewPager and TabLayout
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up ViewPager with adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Calls");
                        tab.setIcon(R.drawable.ic_calls);
                        break;
                    case 1:
                        tab.setText("Messages");
                        tab.setIcon(R.drawable.ic_messages);
                        break;
                    case 2:
                        tab.setText("Blacklist");
                        tab.setIcon(R.drawable.ic_blacklist);
                        break;
                }
            }
        ).attach();

        // Check and request permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ANSWER_PHONE_CALLS
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                break;
            }
        }
    }

    public static SmsCallActivity getInstance() {
        return instance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}