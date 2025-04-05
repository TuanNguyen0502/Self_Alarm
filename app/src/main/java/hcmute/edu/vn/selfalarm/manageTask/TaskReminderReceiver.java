package hcmute.edu.vn.selfalarm.manageTask;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.selfalarm.R;

public class TaskReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract task details
        String taskTitle = intent.getStringExtra("task_title");

        // Show a notification or a toast message
        if (taskTitle != null) {
            showNotification(context, taskTitle);
        } else {
            Toast.makeText(context, "Task reminder!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotification(Context context, String taskTitle) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("task_channel", "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}
