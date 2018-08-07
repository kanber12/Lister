package by.kanber.lister.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class RingtonePreference extends Preference {
    public static final int RINGTONE_PICKER_REQUEST = 1;

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSelectedRingtone(String selected) {
        persistString(selected);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setSelectedRingtone(restorePersistedValue ? getPersistedString("def_ring") : String.valueOf(defaultValue));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
