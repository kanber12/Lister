package by.kanber.lister;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int currTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", 0);
        setTheme(Utils.currentTheme(currTheme));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(getString(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        transaction.replace(R.id.settings_container, fragment, "settingsFragment");
        transaction.commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("language", "def");
        Locale locale = Utils.initLang(lang);
        newBase = ContextWrapper.wrap(newBase, locale);

        super.attachBaseContext(newBase);
    }

    public void recreateActivity() {
        recreate();
    }
}
