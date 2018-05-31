package by.kanber.lister;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.RadioButton;

public class VibrationPreferenceFragmentCompat extends PreferenceDialogFragmentCompat {
    private RadioButton btn1, btn2, btn3, btn4, btn5;
    private int selectedVibration;
    private VibrationPreference preference;

    public static VibrationPreferenceFragmentCompat newInstance(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        VibrationPreferenceFragmentCompat fragment = new VibrationPreferenceFragmentCompat();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        btn1 = view.findViewById(R.id.vibration_radio_btn_1);
        btn2 = view.findViewById(R.id.vibration_radio_btn_2);
        btn3 = view.findViewById(R.id.vibration_radio_btn_3);
        btn4 = view.findViewById(R.id.vibration_radio_btn_4);
        btn5 = view.findViewById(R.id.vibration_radio_btn_5);
        DialogPreference preference = getPreference();

        if (preference instanceof VibrationPreference) {
            this.preference = (VibrationPreference) preference;
            selectedVibration = this.preference.getVibration();
        }

        init();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.vibration_radio_btn_1: selectedVibration = 0; playVibration(0); break;
                    case R.id.vibration_radio_btn_2: selectedVibration = 1; playVibration(1); break;
                    case R.id.vibration_radio_btn_3: selectedVibration = 2; playVibration(2); break;
                    case R.id.vibration_radio_btn_4: selectedVibration = 3; playVibration(3); break;
                    case R.id.vibration_radio_btn_5: selectedVibration = 4; playVibration(4); break;
                }

                init();
            }
        };

        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);
        btn5.setOnClickListener(listener);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null)
            vibrator.cancel();

        if (positiveResult)
            if (preference.callChangeListener(selectedVibration))
                preference.setSelectedVibration(selectedVibration);
    }

    private void init() {
        switch (selectedVibration) {
            case 0: btn1.setChecked(true); break;
            case 1: btn2.setChecked(true); break;
            case 2: btn3.setChecked(true); break;
            case 3: btn4.setChecked(true); break;
            case 4: btn5.setChecked(true); break;
        }
    }

    private void playVibration(int id) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null)
            vibrator.vibrate(Utils.vibrationPatternSwitcher(id), -1);
    }
}