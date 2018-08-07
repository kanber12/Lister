package by.kanber.lister.fragment;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.ImageView;

import by.kanber.lister.R;
import by.kanber.lister.preference.ThemePreference;

public class ThemePreferenceFragmentCompat extends PreferenceDialogFragmentCompat {
    private ImageView img1, img2, img3, img4, img5, img6, img7, img8;
    private int selectedTheme;
    private ThemePreference preference;

    public static ThemePreferenceFragmentCompat newInstance(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        ThemePreferenceFragmentCompat fragment = new ThemePreferenceFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        img1 = view.findViewById(R.id.color1);
        img2 = view.findViewById(R.id.color2);
        img3 = view.findViewById(R.id.color3);
        img4 = view.findViewById(R.id.color4);
        img5 = view.findViewById(R.id.color5);
        img6 = view.findViewById(R.id.color6);
        img7 = view.findViewById(R.id.color7);
        img8 = view.findViewById(R.id.color8);
        DialogPreference pref = getPreference();

        if (pref instanceof ThemePreference) {
            preference = (ThemePreference) pref;
            selectedTheme = preference.getTheme();
        }

        init();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetChoice();
                switch (v.getId()) {
                    case R.id.color1: selectedTheme = 0; break;
                    case R.id.color2: selectedTheme = 1; break;
                    case R.id.color3: selectedTheme = 2; break;
                    case R.id.color4: selectedTheme = 3; break;
                    case R.id.color5: selectedTheme = 4; break;
                    case R.id.color6: selectedTheme = 5; break;
                    case R.id.color7: selectedTheme = 6; break;
                    case R.id.color8: selectedTheme = 7; break;
                }

                init();
            }
        };

        img1.setOnClickListener(listener);
        img2.setOnClickListener(listener);
        img3.setOnClickListener(listener);
        img4.setOnClickListener(listener);
        img5.setOnClickListener(listener);
        img6.setOnClickListener(listener);
        img7.setOnClickListener(listener);
        img8.setOnClickListener(listener);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult)
            if (preference.callChangeListener(selectedTheme))
                preference.setSelectedTheme(selectedTheme);
    }

    private void init() {
        switch (selectedTheme) {
            case 0: setChosen(img1); break;
            case 1: setChosen(img2); break;
            case 2: setChosen(img3); break;
            case 3: setChosen(img4); break;
            case 4: setChosen(img5); break;
            case 5: setChosen(img6); break;
            case 6: setChosen(img7); break;
            case 7: setChosen(img8); break;
        }
    }

    private void resetChoice() {
        img1.setImageResource(android.R.color.transparent);
        img2.setImageResource(android.R.color.transparent);
        img3.setImageResource(android.R.color.transparent);
        img4.setImageResource(android.R.color.transparent);
        img5.setImageResource(android.R.color.transparent);
        img6.setImageResource(android.R.color.transparent);
        img7.setImageResource(android.R.color.transparent);
        img8.setImageResource(android.R.color.transparent);
    }

    private void setChosen(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_done);
    }
}
