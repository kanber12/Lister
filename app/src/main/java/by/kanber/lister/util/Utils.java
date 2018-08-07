package by.kanber.lister.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.preference.PreferenceManager;
import android.util.TypedValue;

import org.joda.time.DateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import by.kanber.lister.R;

public class Utils {
    public static final int KEY_REMINDER_SET = 0;
    public static final int KEY_ADDED = 1;
    public static final int KEY_NONE = 2;

    private static SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy HH mm", Locale.US);

    private static String timeToString(long time) {
        return format.format(new Date(time));
    }

    public static String viewableTime(Context context, long longTime, int key) {
        String result = "";
        String[] timeArray = timeToString(longTime).split(" ");
        DateTime time = new DateTime(new Date(longTime)), currTime = new DateTime();

        if (isToday(currTime, time))
            result += context.getString(R.string.today);
        else {
            if (isYesterday(currTime, time))
                result += context.getString(R.string.yesterday);
            else {
                if (isTomorrow(currTime, time))
                    result += context.getString(R.string.tomorrow);
                else {
                    result += (timeArray[0] + "." + timeArray[1]);

                    if (time.getYear() < currTime.getYear() || time.getYear() > currTime.getYear())
                        result += timeArray[2];
                }
            }
        }

        return getKey(context, key) + " " + result + " " + context.getString(R.string.at) + " " + timeArray[3] + ":" + timeArray[4];
    }

    private static String getKey(Context context, int key) {
        switch (key) {
            case KEY_REMINDER_SET: return context.getString(R.string.key_reminder_set_to);
            case KEY_ADDED: return context.getString(R.string.key_added);
            default: return "";
        }
    }

    private static boolean isToday(DateTime currTime, DateTime time) {
        return currTime.getDayOfMonth() == time.getDayOfMonth() && currTime.getMonthOfYear() == time.getMonthOfYear() && currTime.getYear() == time.getYear();
    }

    private static boolean isYesterday(DateTime currTime, DateTime time) {
        return currTime.getYear() - time.getYear() == 1 && currTime.getDayOfMonth() == 1 && time.getDayOfMonth() == 31 && currTime.getMonthOfYear() == 1 && time.getMonthOfYear() == 12 ||
                currTime.getYear() == time.getYear() && currTime.getMonthOfYear() - time.getMonthOfYear() == 1 && currTime.getDayOfMonth() == 1 && time.getDayOfMonth() == getDaysInMonth(time.getMonthOfYear(), time.getYear()) ||
                currTime.getYear() == time.getYear() && currTime.getMonthOfYear() == time.getMonthOfYear() && currTime.getDayOfMonth() - time.getDayOfMonth() == 1;
    }

    private static boolean isTomorrow(DateTime currTime, DateTime time) {
        return time.getYear() - currTime.getYear() == 1 && currTime.getDayOfMonth() == 31 && time.getDayOfMonth() == 1 && currTime.getMonthOfYear() == 12 && time.getMonthOfYear() == 1 ||
                time.getYear() == currTime.getYear() && time.getMonthOfYear() - currTime.getMonthOfYear() == 1 && time.getDayOfMonth() == 1 && currTime.getDayOfMonth() == getDaysInMonth(currTime.getMonthOfYear(), currTime.getYear()) ||
                time.getYear() == currTime.getYear() && time.getMonthOfYear() == currTime.getMonthOfYear() && time.getDayOfMonth() - currTime.getDayOfMonth() == 1;
    }

    private static double getDaysInMonth(int month, int year) {
        return 28 + ((month + Math.floor(month / 8)) % 2) + 2 % month + Math.floor((1 + (1 - (year % 4 + 2) % (year % 4 + 1)) * ((year % 100 + 2) % (year % 100 + 1)) + (1 - (year % 400 + 2) % (year % 400 + 1))) / month) + Math.floor(1/month) - Math.floor(((1 - (year % 4 + 2) % (year % 4 + 1)) * ((year % 100 + 2) % (year % 100 + 1)) + (1 - (year % 400 + 2) % (year % 400 + 1)))/month);
    }

    public static boolean isEmpty(String s) {
        return s.equals("") || s.trim().equals("") || s.split("\n").length == 0;
    }

    public static int getColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);

        return context.getResources().getColor(typedValue.resourceId);
    }

    public static CharSequence getTextForNotif(Context context, String text, String picture, boolean isPass) {
        boolean isHidden = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean("hidden", false);

        if (isHidden || isPass)
            return context.getText(R.string.text_hidden);

        if (!Utils.isEmpty(text)) {
            return text;
        } else {
            if (picture.equals(""))
                return context.getText(R.string.no_text);
            else
                return context.getText(R.string.picture);
        }
    }

    public static int getLedColor(Context context) {
        String currLed = PreferenceManager.getDefaultSharedPreferences(context).getString("led_color", "led_blue");

        switch (currLed) {
            case "led_blue": return 0x2979FF;
            case "led_yellow": return 0xFFEA00;
            case "led_white": return 0xFFFFFF;
            case "led_purple": return 0x7C4DFF;
            case "led_pink": return 0xFF4081;
        }

        return 0x2979FF;
    }

    public static int currentTheme(int currTheme) {
        switch (currTheme) {
            case 0: return R.style.ThemeGreen;
            case 1: return R.style.ThemeBlue;
            case 2: return R.style.ThemeOrange;
            case 3: return R.style.ThemeIndigo;
            case 4: return R.style.ThemePink;
            case 5: return R.style.ThemePurple;
            case 6: return R.style.ThemeRed;
            case 7: return R.style.ThemeTeal;
        }

        return R.style.ThemeGreen;
    }

    public static Locale initLang(String currLang) {
        Locale locale;

        switch (currLang) {
            case "ru": case "en": locale = new Locale(currLang); break;
            default: locale = Resources.getSystem().getConfiguration().locale;
        }

        return locale;
    }

    public static Uri getSelectedRingtoneUri(Context context) {
        String selected = PreferenceManager.getDefaultSharedPreferences(context).getString("ring", "def_ring");

        switch (selected) {
            case "def_ring": return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            default: return Uri.parse(selected);
        }
    }

    public static long[] getVibrationPattern(Context context) {
        int selected = PreferenceManager.getDefaultSharedPreferences(context).getInt("vibration ",0);

        return vibrationPatternSwitcher(selected);
    }

    public static long[] vibrationPatternSwitcher(int selected) {
        switch (selected) {
            case 1: return new long[] {0, 800, 200, 800, 200, 800};
            case 2: return new long[] {0, 500, 200, 500, 200, 1500};
            case 3: return new long[] {0, 1000, 300, 300, 300, 800};
            case 4: return new long[] {0, };
            case 0: default: return new long[] {0, 1000, 300, 400, 300, 400, 300, 1000};
        }
    }

    public static String getRingtoneTitle(Context context) {
        Uri uri = getSelectedRingtoneUri(context);
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);

        return ringtone.getTitle(context);
    }

    public static String getVibrationType(Context context) {
        int type = PreferenceManager.getDefaultSharedPreferences(context).getInt("vibration", 0);

        switch (type) {
            case 0: return context.getString(R.string.vibration_1);
            case 1: return context.getString(R.string.vibration_2);
            case 2: return context.getString(R.string.vibration_3);
            case 3: return context.getString(R.string.vibration_4);
            case 4: return context.getString(R.string.vibration_5);
        }

        return context.getString(R.string.vibration_1);
    }

    public static boolean allPermissionsGranted(int[] grantResults) {
        for (int i : grantResults)
            if (i != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static Bitmap getImageBitmap(Context context, String uri) {
        Bitmap bitmap = null;

        try {
            if (!uri.equals(""))
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}