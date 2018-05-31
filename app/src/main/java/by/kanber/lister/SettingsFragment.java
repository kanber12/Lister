package by.kanber.lister;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;

import static android.app.Activity.RESULT_OK;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int SETTINGS_PERMISSIONS = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        initSummary(getPreferenceScreen());

        Preference preference = findPreference("ring");
        preference.setSummary(Utils.getRingtoneTitle(getActivity()));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, SETTINGS_PERMISSIONS);
                } else {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_ringtone));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Utils.getSelectedRingtoneUri(getActivity()));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);

                    startActivityForResult(intent, RingtonePreference.RINGTONE_PICKER_REQUEST);
                }
                return true;
            }
        });
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;

        if (preference instanceof ThemePreference)
            dialogFragment = ThemePreferenceFragmentCompat.newInstance("theme");

        if (preference instanceof VibrationPreference)
            dialogFragment = VibrationPreferenceFragmentCompat.newInstance("vibration");

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RingtonePreference.RINGTONE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                if (uri != null)
                    ((RingtonePreference) getPreferenceScreen().findPreference("ring")).setSelectedRingtone(uri.toString());

                Log.d(MainActivity.TAG, "Title: " + Utils.getRingtoneTitle(getActivity()));

                updatePreferenceSummary(findPreference("ring"));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "theme": case "language": ((SettingsActivity) getActivity()).recreateActivity(); break;
            default: updatePreferenceSummary(findPreference(key));
        }
    }

    private void initSummary(Preference preference) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = (PreferenceGroup) preference;

            for (int i = 0; i < group.getPreferenceCount(); i++)
                initSummary(group.getPreference(i));
        } else
            updatePreferenceSummary(preference);
    }

    private void updatePreferenceSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }

        if (preference instanceof RingtonePreference)
            preference.setSummary(Utils.getRingtoneTitle(getActivity()));

        if (preference instanceof VibrationPreference)
            preference.setSummary(Utils.getVibrationType(getActivity()));
    }
}
