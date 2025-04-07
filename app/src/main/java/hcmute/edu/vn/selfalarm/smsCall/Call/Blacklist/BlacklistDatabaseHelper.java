package hcmute.edu.vn.selfalarm.smsCall.Call.Blacklist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BlacklistDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BlacklistDBHelper";
    private static final String DATABASE_NAME = "blacklist.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_BLACKLIST = "blacklist";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_DATE_ADDED = "date_added";

    public BlacklistDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "BlacklistDatabaseHelper initialized");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating blacklist table");
        String createTable = "CREATE TABLE " + TABLE_BLACKLIST + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PHONE_NUMBER + " TEXT UNIQUE, " +
                COLUMN_DATE_ADDED + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLACKLIST);
        onCreate(db);
    }

    public boolean addToBlacklist(String phoneNumber) {
        Log.d(TAG, "Adding " + phoneNumber + " to blacklist");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(COLUMN_DATE_ADDED, System.currentTimeMillis());
        
        long result = db.insertWithOnConflict(TABLE_BLACKLIST, null, values, 
                SQLiteDatabase.CONFLICT_REPLACE);
        boolean success = result != -1;
        Log.d(TAG, "Add to blacklist result: " + success);
        return success;
    }

    public boolean removeFromBlacklist(String phoneNumber) {
        Log.d(TAG, "Removing " + phoneNumber + " from blacklist");
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_BLACKLIST, COLUMN_PHONE_NUMBER + " = ?",
                new String[]{phoneNumber});
        boolean success = rowsAffected > 0;
        Log.d(TAG, "Remove from blacklist result: " + success);
        return success;
    }

    public boolean isBlacklisted(String phoneNumber) {
        Log.d(TAG, "Checking if " + phoneNumber + " is blacklisted");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLACKLIST, new String[]{COLUMN_ID},
                COLUMN_PHONE_NUMBER + " = ?", new String[]{phoneNumber},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        Log.d(TAG, "Is blacklisted result: " + exists);
        return exists;
    }

    public List<String> getAllBlacklistedNumbers() {
        Log.d(TAG, "Getting all blacklisted numbers");
        List<String> numbers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLACKLIST, new String[]{COLUMN_PHONE_NUMBER},
                null, null, null, null, COLUMN_DATE_ADDED + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numbers.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Log.d(TAG, "Found " + numbers.size() + " blacklisted numbers");
        return numbers;
    }
} 