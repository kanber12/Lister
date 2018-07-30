package by.kanber.lister;

import android.app.NotificationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ListerLog";
    public static final int EDIT_PERMISSION_GALLERY = 1;
    public static final int EDIT_PERMISSION_CAMERA = 2;

    public static MainActivity instance;
    private DBHelper helper;

    private int currTheme;
    private String currLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", 0);
        helper = new DBHelper(this);
        setTheme(Utils.currentTheme(currTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager != null)
            manager.cancelAll();

        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        NotesListFragment fragment = new NotesListFragment();
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
            if (requestCode == EDIT_PERMISSION_GALLERY || requestCode == EDIT_PERMISSION_CAMERA) {
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

    public DBHelper getHelper() {
        return helper;
    }

    @Override
    protected void onPause() {
        super.onPause();
        instance = null;
    }
}