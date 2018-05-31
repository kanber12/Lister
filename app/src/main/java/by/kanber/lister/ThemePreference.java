package by.kanber.lister;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

public class ThemePreference extends DialogPreference {
    private int selectedTheme;

    public ThemePreference(Context context) {
        this(context, null);
    }

    public ThemePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setDialogLayoutResource(R.layout.theme_picker_pref_dialog);
        setTitle(context.getString(R.string.theme));
        setDialogTitle(context.getString(R.string.choose_theme_text));
        setPositiveButtonText(context.getString(R.string.set));
        setNegativeButtonText(context.getString(R.string.cancel));
    }

    public int getTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(int selected) {
        selectedTheme = selected;
        persistInt(selected);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setSelectedTheme(restorePersistedValue ? getPersistedInt(selectedTheme) : (int) defaultValue);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.theme_picker_pref_dialog;
    }
}