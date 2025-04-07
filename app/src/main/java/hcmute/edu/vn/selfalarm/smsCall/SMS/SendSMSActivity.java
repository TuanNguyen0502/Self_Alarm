package hcmute.edu.vn.selfalarm.smsCall.SMS;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import hcmute.edu.vn.selfalarm.R;

public class SendSMSActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 123;
    private TextInputEditText phoneNumberInput;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        // Initialize views
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Set click listener for send button
        sendButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                phoneNumberInput.setError("Phone number is required");
                return;
            }

            if (message.isEmpty()) {
                messageInput.setError("Message is required");
                return;
            }

            // Check SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
                sendSMS(phoneNumber, message);
            } else {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
            }
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SMSSender.sendSMS(this, phoneNumber, message);
            // Clear inputs after sending
            phoneNumberInput.setText("");
            messageInput.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String phoneNumber = phoneNumberInput.getText().toString().trim();
                String message = messageInput.getText().toString().trim();
                sendSMS(phoneNumber, message);
            } else {
                Toast.makeText(this, "SMS permission is required to send messages",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
} 