package by.kanber.lister.dialog;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import by.kanber.lister.activity.MainActivity;
import by.kanber.lister.R;


public class SetReminderDialog extends DialogFragment {
    private final String PATTERN = "d.M.yyyy.H.m";

    private OnDialogInteractionListener listener;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private AlertDialog reminderDialog;
    private MainActivity activity;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN, Locale.US);
    private boolean isTime = false;
    private int position = -1;
    private String time, date;

    public static SetReminderDialog newInstance(int position) {
        SetReminderDialog dialog = new SetReminderDialog();

        Bundle args = new Bundle();
        args.putInt("position", position);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

        if (getArguments() != null)
            position = getArguments().getInt("position", -1);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View reminderView = activity.getLayoutInflater().inflate(R.layout.set_reminder_dialog, null);
        timePicker = reminderView.findViewById(R.id.set_reminder_time);
        datePicker = reminderView.findViewById(R.id.set_reminder_date);

        builder.setView(reminderView)
                .setNeutralButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.next), null)
                .setNegativeButton(getString(R.string.back), null)
                .setCancelable(false);

        reminderDialog = builder.create();

        reminderDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button posButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                final Button negButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button neutButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);

                negButton.setVisibility(View.GONE);

                posButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isTime) {
                            isTime = true;
                            negButton.setVisibility(View.VISIBLE);
                            posButton.setText(getString(R.string.set));
                            datePicker.setVisibility(View.GONE);
                            timePicker.setVisibility(View.VISIBLE);
                        } else
                            setTime();
                    }
                });

                negButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTime) {
                            isTime = false;
                            negButton.setVisibility(View.GONE);
                            posButton.setText(getString(R.string.next));
                            datePicker.setVisibility(View.VISIBLE);
                            timePicker.setVisibility(View.GONE);
                        }
                    }
                });

                neutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderDialog.cancel();
                    }
                });
            }
        });

        Calendar calendar = Calendar.getInstance();
        datePicker.setMinDate(calendar.getTimeInMillis());
        timePicker.setIs24HourView(DateFormat.is24HourFormat(activity));
        date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR));
        time = String.valueOf("." + calendar.get(Calendar.HOUR_OF_DAY)) + "." + calendar.get(Calendar.MINUTE);

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time = "." + hourOfDay + "." + minute;
            }
        });

        return reminderDialog;
    }

    private void setTime() {
        Date tmp = new Date();

        try {
            tmp = simpleDateFormat.parse(date + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (tmp.getTime() <= Calendar.getInstance().getTimeInMillis())
            activity.showCenteredToast(getString(R.string.reminder_set_to_past_warning));
        else {
            listener.onSetReminderDialogInteraction(position, tmp.getTime());
            reminderDialog.cancel();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment1 = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("addNoteFragment");
        Fragment fragment2 = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("editNoteFragment");
        Fragment fragment3 = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("notesListFragment");

        if (fragment1 instanceof OnDialogInteractionListener)
            listener = (OnDialogInteractionListener) fragment1;
        else
            if (fragment2 instanceof OnDialogInteractionListener)
                listener = (OnDialogInteractionListener) fragment2;
            else
                if (fragment3 instanceof OnDialogInteractionListener)
                    listener = (OnDialogInteractionListener) fragment3;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnDialogInteractionListener {
        void onSetReminderDialogInteraction(int position, long time);
    }
}
