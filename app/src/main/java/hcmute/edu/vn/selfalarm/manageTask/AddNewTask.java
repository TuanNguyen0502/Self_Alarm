package hcmute.edu.vn.selfalarm.manageTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.selfalarm.R;

public class AddNewTask extends BottomSheetDialogFragment {
    public static final String TAG = "AddNewTask";
    private EditText editText_title, editText_description;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button button_save;
    private DataBaseHelper dataBaseHelper;
    private ManageTaskActivity mainActivity;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task, container, false);
        return v;
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (ManageTaskActivity) getActivity();
        dataBaseHelper = new DataBaseHelper(getActivity());

        editText_title = view.findViewById(R.id.editText_title);
        editText_description = view.findViewById(R.id.editText_description);
        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);
        button_save = view.findViewById(R.id.button_save);

        boolean isUpdate = false;

        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String title = bundle.getString("title");
            String description = bundle.getString("description");
            String date = bundle.getString("date");
            // Split date into parts (date and time)
            String[] dateParts = date.split(" ");
            String[] dateParts1 = dateParts[0].split("-");
            int year = Integer.parseInt(dateParts1[0]);
            int month = Integer.parseInt(dateParts1[1]) - 1;
            int day = Integer.parseInt(dateParts1[2]);
            String[] dateParts2 = dateParts[1].split(":");
            int hour = Integer.parseInt(dateParts2[0]);
            int minute = Integer.parseInt(dateParts2[1]);
            // Set EditText to this text
            editText_title.setText(title);
            editText_description.setText(description);
            // Set DatePicker to this date
            datePicker.updateDate(year, month, day);
            // Set TimePicker to this time
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }

        boolean finalIsUpdate = isUpdate;
        button_save.setOnClickListener(v -> {
            TaskModel taskModel = new TaskModel();
            taskModel.setTitle(editText_title.getText().toString());
            taskModel.setDescription(editText_description.getText().toString());
            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d:00",
                    datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
            taskModel.setDue_time(formattedDate);

            if (finalIsUpdate) {
                taskModel.setId(bundle.getLong("id"));
                dataBaseHelper.updateTask(taskModel.getId(), taskModel.getTitle(), taskModel.getDescription(), taskModel.getDue_time());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, datePicker.getYear());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0); // Set seconds to 0
                // Convert to milliseconds
                long dueTimeMillis = calendar.getTimeInMillis();
                setTaskReminder(mainActivity, dueTimeMillis, taskModel.getId(), taskModel.getTitle());
            } else {
                taskModel.setId(System.currentTimeMillis());
                taskModel.setStatus(0);
                dataBaseHelper.addTask(taskModel);
                taskModel.setId(System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, datePicker.getYear());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0); // Set seconds to 0
                // Convert to milliseconds
                long dueTimeMillis = calendar.getTimeInMillis();
                setTaskReminder(mainActivity, dueTimeMillis, taskModel.getId(), taskModel.getTitle());
            }
            dismiss();
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setTaskReminder(Context context, long dueTimeMillis, long taskId, String taskTitle) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (int) taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            // Cancel old alarm before setting a new one
            alarmManager.cancel(pendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTimeMillis, pendingIntent);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
