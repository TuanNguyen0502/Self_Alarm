package hcmute.edu.vn.selfalarm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.selfalarm.manageTask.ManageTaskActivity;
import hcmute.edu.vn.selfalarm.musicPlayer.MusicPlayer;
import hcmute.edu.vn.selfalarm.smsCall.SmsCallActivity;

public class MainActivity extends AppCompatActivity {
    private Button button_music, button_smsCall, button_tasks;
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

        button_music = findViewById(R.id.button_music);
        button_music.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
            startActivity(intent);
        });

        button_smsCall = findViewById(R.id.button_sms);
        button_smsCall.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SmsCallActivity.class);
            startActivity(intent);
        });

        button_tasks = findViewById(R.id.button_task);
        button_tasks.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageTaskActivity.class);
            startActivity(intent);
        });
    }
}