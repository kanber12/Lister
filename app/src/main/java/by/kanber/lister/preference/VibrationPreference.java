package by.kanber.lister.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import by.kanber.lister.R;

public class VibrationPreference extends DialogPreference {
    private int selectedVibration;

    public VibrationPreference(Context context) {
        this(context, null);
    }

    public VibrationPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public VibrationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public VibrationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setDialogLayoutResource(R.layout.vibration_picker_pref_dialog);
        setTitle(context.getString(R.string.settings_vibration));
        setDialogTitle(context.getString(R.string.settings_vibration));
        setPositiveButtonText(context.getString(R.string.set));
        setNegativeButtonText(context.getString(R.string.cancel));
    }

    public int getVibration() {
        return selectedVibration;
    }

    public void setSelectedVibration(int selected) {
        selectedVibration = selected;
        persistInt(selected);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setSelectedVibration(restorePersistedValue ? getPersistedInt(selectedVibration) : (int) defaultValue);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.vibration_picker_pref_dialog;
    }
}
