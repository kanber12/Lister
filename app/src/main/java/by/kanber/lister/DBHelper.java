package by.kanber.lister;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "noteDB";
    public static final String TABLE_NAME = "notes";

    public static final String KEY_ID = "_id";
    public static final String KEY_INDEX = "note_index";
    public static final String KEY_TITLE = "note_title";
    public static final String KEY_BODY = "note_body";
    public static final String KEY_PASSWORD = "note_password";
    public static final String KEY_PICTURE = "note_picture";
    public static final String KEY_ADD_TIME = "note_add_time";
    public static final String KEY_NOTIF_TIME = "note_notif_time";
    public static final String KEY_REMINDER_SET = "note_is_reminder";
    public static final String KEY_PASSWORD_SET = "note_is_password";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_ID + " integer primary key autoincrement, " + KEY_INDEX + " integer," + KEY_TITLE + " text, " + KEY_BODY + " text, " + KEY_PASSWORD + " text, "
                + KEY_PICTURE + " text, " + KEY_ADD_TIME + " integer, " + KEY_NOTIF_TIME + " integer, " + KEY_REMINDER_SET + " integer, " + KEY_PASSWORD_SET + " integer" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}
