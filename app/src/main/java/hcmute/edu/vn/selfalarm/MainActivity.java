package hcmute.edu.vn.selfalarm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.selfalarm.musicPlayer.MusicPlayer;
import hcmute.edu.vn.selfalarm.optimizeBattery.BatteryOptimizerService;

public class MainActivity extends AppCompatActivity {
    private Button button_music;

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
}