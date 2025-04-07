package hcmute.edu.vn.selfalarm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.selfalarm.musicPlayer.MusicPlayer;
import hcmute.edu.vn.selfalarm.optimizeBattery.BatteryOptimizerService;

public class MainActivity extends AppCompatActivity {
    private ImageButton button_music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkAndRequestWriteSettings(this);
        Intent serviceIntent = new Intent(this, BatteryOptimizerService.class);
        startService(serviceIntent);

        button_music = findViewById(R.id.button_music);
        button_music.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
            startActivity(intent);
        });

    }

    public void checkAndRequestWriteSettings(Context context) {
        if (!Settings.System.canWrite(context)) {
            Log.e("BatteryOptimizer", "App does not have WRITE_SETTINGS permission. Requesting...");
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Log.d("BatteryOptimizer", "WRITE_SETTINGS permission already granted");
        }
    }

    private final BroadcastReceiver showDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", -1);
            boolean isCharging = intent.getBooleanExtra("charging", false);
            openDialogActivity(level, isCharging);
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(showDialogReceiver, new IntentFilter("show_dialog"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(showDialogReceiver);
    }

    public void openDialogActivity(int level, boolean isCharging) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_activity);

        Window window = dialog.getWindow();
        if (window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);

        TextView txtContent = dialog.findViewById(R.id.txtContent);
        Button btnClose = dialog.findViewById(R.id.btnClose);
        Button btnOk = dialog.findViewById(R.id.btnOk);

        if (level <= 90){
            txtContent.setText("Pin của bạn đã giảm xuống dưới 20%. Bạn có muốn tiết kiệm pin?");
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BatteryOptimizerService.class);
            intent.putExtra("action", "adjust_settings");
            intent.putExtra("level", level);
            intent.putExtra("charging", isCharging);
            startService(intent);
            dialog.dismiss();
        });

        dialog.show();

    }
}