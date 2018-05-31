package by.kanber.lister;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ListerLog";
    public static final int ADD_PERMISSION_GALLERY = 1;
    public static final int ADD_PERMISSION_CAMERA = 2;
    public static final int EDIT_PERMISSION_GALLERY = 3;
    public static final int EDIT_PERMISSION_CAMERA = 4;

    public static MainActivity instance;

    private SharedPreferences sPref;
    private DBHelper helper;

    private ArrayList<Note> notesList;
    private int currentSortType, currTheme;
    private String currLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", 0);
        setTheme(Utils.currentTheme(currTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesList = new ArrayList<>();
        helper = new DBHelper(this);
        instance = this;
        load();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager != null)
            manager.cancelAll();

        notesList = Note.getNotes(helper);

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        NotesListFragment fragment = NotesListFragment.newInstance(notesList, currentSortType);
        fTrans.replace(R.id.container, fragment, "notesListFragment");
        fTrans.commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        currLang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("language", "def");
        Locale locale = Utils.initLang(currLang);
        newBase = ContextWrapper.wrap(newBase, locale);

        super.attachBaseContext(newBase);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && Utils.allPermissionsGranted(grantResults)) {
            if (requestCode == ADD_PERMISSION_GALLERY || requestCode == ADD_PERMISSION_CAMERA) {
                AddNoteFragment fragment = (AddNoteFragment) getSupportFragmentManager().findFragmentByTag("addNoteFragment");

                switch (requestCode) {
                    case ADD_PERMISSION_GALLERY: fragment.chooseFromGallery(); break;
                    case ADD_PERMISSION_CAMERA: fragment.takePhoto(); break;
                }
            } else {
                EditNoteFragment fragment = (EditNoteFragment) getSupportFragmentManager().findFragmentByTag("editNoteFragment");

                switch (requestCode) {
                    case EDIT_PERMISSION_GALLERY: fragment.chooseFromGallery(); break;
                    case EDIT_PERMISSION_CAMERA: fragment.takePhoto(); break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        instance = this;
        int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", 0);
        String lang = PreferenceManager.getDefaultSharedPreferences(this).getString("language", "def");

        if (theme != currTheme || !lang.equals(currLang))
            recreate();
    }

    public void changeReminderStatus() {
        NotesListFragment fragment = (NotesListFragment) getSupportFragmentManager().findFragmentByTag("notesListFragment");
        fragment.checkReminderIsOut();
    }

    private void save() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("sortType", currentSortType);
        editor.apply();
    }

    private void load() {
        sPref = getPreferences(MODE_PRIVATE);
        currentSortType = sPref.getInt("sortType", NotesListFragment.SORT_TYPE_CREATED_TIME_REVERSED);
    }

    public void showCenteredToast(String msg) {
        Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        ((TextView) t.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        t.show();
    }

    public void closeKeyboard() {
        View view = MainActivity.this.getCurrentFocus();

        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null)
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void setCurrentSortType(int currentSortType) {
        this.currentSortType = currentSortType;
    }

    public DBHelper getHelper() {
        return helper;
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        instance = null;
    }
}