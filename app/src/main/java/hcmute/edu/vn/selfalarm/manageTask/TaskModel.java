package hcmute.edu.vn.selfalarm.manageTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskModel {
    private long id;
    private String title;
    private String description;
    private String due_time;
    private int status;

    public TaskModel() {
    }

    // Convert selected date & time to milliseconds (INTEGER for SQLite)
    private int convertToTimestamp(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return (int) calendar.getTimeInMillis();  // Store this in SQLite as INTEGER
    }

    // Convert timestamp (INTEGER) back to Date & Time
    private String convertToDateTimeString(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp)); // Format the timestamp
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDue_time() {
        return due_time;
    }

    public void setDue_time(String due_time) {
        this.due_time = due_time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
