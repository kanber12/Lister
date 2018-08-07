package by.kanber.lister.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Objects;

import by.kanber.lister.database.DBHelper;

public class Note implements Parcelable
{
    private String title, body, password, picture;
    private long addTime, notificationTime;
    private int id = -1, index;
    private boolean reminderSet, passwordSet, show = true;

    private Note(int index, String title, String body, String password, long time, boolean reminderSet, boolean passwordSet, long notificationTime, String picture) {
        this.index = index;
        this.title = title;
        this.body = body;
        this.password = password;
        this.addTime = time;
        this.reminderSet = reminderSet;
        this.passwordSet = passwordSet;
        this.notificationTime = notificationTime;
        this.picture = picture;
    }

    public Note(Note note) {
        id = note.getId();
        show = note.isShow();
        title = note.getTitle();
        body = note.getBody();
        password = note.getPassword();
        addTime = note.getAddTime();
        reminderSet = note.isReminderSet();
        passwordSet = note.isPasswordSet();
        notificationTime = note.getNotificationTime();
        picture = note.getPicture();
    }

    public Note() {
        index = 0;
        title = "";
        body = "";
        password = "";
        addTime = 0;
        reminderSet = false;
        passwordSet = false;
        notificationTime = 0;
        picture = "";
    }

    public static ArrayList<Note> getNotes(DBHelper helper) {
        int tId, tIndex;
        String tTitle, tBody, tPassword, tPicture;
        long tAddTime, tNotifTime;
        boolean tReminderSet, tPasswordSet;
        ArrayList<Note> notes = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indIndex = cursor.getColumnIndex(DBHelper.KEY_INDEX);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int bodyIndex = cursor.getColumnIndex(DBHelper.KEY_BODY);
            int passIndex = cursor.getColumnIndex(DBHelper.KEY_PASSWORD);
            int picIndex = cursor.getColumnIndex(DBHelper.KEY_PICTURE);
            int addIndex = cursor.getColumnIndex(DBHelper.KEY_ADD_TIME);
            int notifIndex = cursor.getColumnIndex(DBHelper.KEY_NOTIF_TIME);
            int passSetIndex = cursor.getColumnIndex(DBHelper.KEY_PASSWORD_SET);
            int reminderSetIndex = cursor.getColumnIndex(DBHelper.KEY_REMINDER_SET);

            do {
                tId = cursor.getInt(idIndex);
                tIndex = cursor.getInt(indIndex);
                tTitle = cursor.getString(titleIndex);
                tBody = cursor.getString(bodyIndex);
                tPassword = cursor.getString(passIndex);
                tPicture = cursor.getString(picIndex);
                tAddTime = cursor.getLong(addIndex);
                tNotifTime = cursor.getLong(notifIndex);
                tPasswordSet = cursor.getInt(passSetIndex) != 0 ;
                tReminderSet = cursor.getInt(reminderSetIndex) != 0;
                Note note = new Note(tIndex, tTitle, tBody, tPassword, tAddTime, tReminderSet, tPasswordSet, tNotifTime, tPicture);
                note.setId(tId);
                notes.add(note);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return notes;
    }

    public static Note getLastNote(DBHelper helper) {
        int tId, tIndex;
        String tTitle, tBody, tPassword, tPicture;
        long tAddTime, tNotifTime;
        boolean tReminderSet, tPasswordSet;
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        cursor.moveToLast();

        int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
        int indIndex = cursor.getColumnIndex(DBHelper.KEY_INDEX);
        int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
        int bodyIndex = cursor.getColumnIndex(DBHelper.KEY_BODY);
        int passIndex = cursor.getColumnIndex(DBHelper.KEY_PASSWORD);
        int picIndex = cursor.getColumnIndex(DBHelper.KEY_PICTURE);
        int addIndex = cursor.getColumnIndex(DBHelper.KEY_ADD_TIME);
        int notifIndex = cursor.getColumnIndex(DBHelper.KEY_NOTIF_TIME);
        int passSetIndex = cursor.getColumnIndex(DBHelper.KEY_PASSWORD_SET);
        int reminderSetIndex = cursor.getColumnIndex(DBHelper.KEY_REMINDER_SET);

        tId = cursor.getInt(idIndex);
        tIndex = cursor.getInt(indIndex);
        tTitle = cursor.getString(titleIndex);
        tBody = cursor.getString(bodyIndex);
        tPassword = cursor.getString(passIndex);
        tPicture = cursor.getString(picIndex);
        tAddTime = cursor.getLong(addIndex);
        tNotifTime = cursor.getLong(notifIndex);
        tPasswordSet = cursor.getInt(passSetIndex) != 0 ;
        tReminderSet = cursor.getInt(reminderSetIndex) != 0;

        Note note = new Note(tIndex, tTitle, tBody, tPassword, tAddTime, tReminderSet, tPasswordSet, tNotifTime, tPicture);
        note.setId(tId);

        cursor.close();

        return note;
    }

    public static void insertOrUpdateDB(DBHelper helper, Note note) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase database = helper.getReadableDatabase();

        cv.put(DBHelper.KEY_INDEX, note.getIndex());
        cv.put(DBHelper.KEY_TITLE, note.getTitle());
        cv.put(DBHelper.KEY_BODY, note.getBody());
        cv.put(DBHelper.KEY_PASSWORD, note.getPassword());
        cv.put(DBHelper.KEY_PICTURE, note.getPicture());
        cv.put(DBHelper.KEY_ADD_TIME, note.getAddTime());
        cv.put(DBHelper.KEY_NOTIF_TIME, note.getNotificationTime());
        cv.put(DBHelper.KEY_PASSWORD_SET, note.isPasswordSet());
        cv.put(DBHelper.KEY_REMINDER_SET, note.isReminderSet());

        int count = database.update(DBHelper.TABLE_NAME, cv, DBHelper.KEY_ID + "=" + note.getId(), null);

        if (count == 0)
            database.insertWithOnConflict(DBHelper.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void deleteFromDB(DBHelper helper, Note note) {
        SQLiteDatabase database = helper.getReadableDatabase();
        database.delete(DBHelper.TABLE_NAME, DBHelper.KEY_ID + "=" + note.getId(), null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public boolean isReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }

    public void setPasswordSet(boolean passwordSet) {
        this.passwordSet = passwordSet;
    }

    public long getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(long notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;

        return getNotificationTime() == note.getNotificationTime() &&
                isReminderSet() == note.isReminderSet() &&
                isPasswordSet() == note.isPasswordSet() &&
                titleEquals(getTitle(), note.getTitle()) &&
                bodyEquals(getBody(), note.getBody()) &&
                Objects.equals(getPassword(), note.getPassword()) &&
                Objects.equals(getPicture(), note.getPicture());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getBody(), getPassword(), getPicture(), getNotificationTime(), isReminderSet(), isPasswordSet());
    }

    private boolean titleEquals(String title1, String title2) {
        String[] array1 = title1.split(" "), array2 = title2.split(" ");

        if (array1.length < array2.length || array2.length < array1.length)
            return false;

        for (int i = 0; i < array1.length; i++)
            if (!array1[i].equals(array2[i]))
                return false;

        return true;
    }

    @Override
    public String toString() {
        return "Note{" +
                "index=" + index + "\'" +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", password='" + password + '\'' +
                ", picture='" + picture + '\'' +
                ", addTime=" + addTime +
                ", notificationTime=" + notificationTime +
                ", id=" + id +
                ", reminderSet=" + reminderSet +
                ", passwordSet=" + passwordSet +
                ", show=" + show +
                '}';
    }

    private boolean bodyEquals(String body1, String body2) {
        String[] array1 = body1.split(" "), array2 = body2.split(" ");

        if (array1.length < array2.length || array2.length < array1.length)
            return false;

        for (int i = 0; i < array1.length; i++)
            if (!array1[i].equals(array2[i]))
                return false;

        return true;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(password);
        parcel.writeLong(addTime);
        parcel.writeLong(notificationTime);
        parcel.writeString(String.valueOf(reminderSet));
        parcel.writeString(String.valueOf(passwordSet));
        parcel.writeString(picture);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel parcel) {
            return new Note(parcel);
        }

        @Override
        public Note[] newArray(int i) {
            return new Note[i];
        }
    };

    private Note(Parcel parcel) {
        id = parcel.readInt();
        title = parcel.readString();
        body = parcel.readString();
        password = parcel.readString();
        addTime = parcel.readLong();
        notificationTime = parcel.readLong();
        reminderSet = Boolean.parseBoolean(parcel.readString());
        passwordSet = Boolean.parseBoolean(parcel.readString());
        picture = parcel.readString();
    }
}