package hcmute.edu.vn.selfalarm.manageTask;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarm.manageTask.TaskModel;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "todo";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STATUS = "status";
    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_STATUS + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addTask(TaskModel todoModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, todoModel.getTitle());
        contentValues.put(COLUMN_DESCRIPTION, todoModel.getDescription());
        contentValues.put(COLUMN_DATE, todoModel.getDue_time());
        contentValues.put(COLUMN_STATUS, todoModel.getStatus());
        db.insert(TABLE_NAME, null, contentValues);
    }

    public void updateTask(long id, String title, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_DATE, date);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateStatus(long id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    @SuppressLint("Range")
    public List<TaskModel> getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        List<TaskModel> todoModelList = new ArrayList<>();
        db.beginTransaction();
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        TaskModel todoModel = new TaskModel();
                        todoModel.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                        todoModel.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                        todoModel.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                        todoModel.setDue_time(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                        todoModel.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));
                        todoModelList.add(todoModel);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            db.endTransaction();
            assert cursor != null;
            cursor.close();
        }
        return todoModelList;
    }
}
