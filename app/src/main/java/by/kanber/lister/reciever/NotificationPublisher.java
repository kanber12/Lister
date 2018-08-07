package by.kanber.lister.reciever;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import by.kanber.lister.R;
import by.kanber.lister.activity.MainActivity;
import by.kanber.lister.util.Utils;

public class NotificationPublisher extends BroadcastReceiver {
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_BODY = "note_body";
    public static final String NOTE_PICTURE = "note_picture";
    public static final String NOTE_TIME = "note_time";
    public static final String NOTE_IS_PASS = "note_is_pass";

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        this.context = context;

        int id = intent.getIntExtra(NOTE_ID, 0);
        String title = intent.getStringExtra(NOTE_TITLE);
        String text = intent.getStringExtra(NOTE_BODY);
        String imageUri = intent.getStringExtra(NOTE_PICTURE);
        boolean isPass = intent.getBooleanExtra(NOTE_IS_PASS, false);
        long time = intent.getLongExtra(NOTE_TIME, System.currentTimeMillis());

        Notification notification = createNotification(id, title, text, imageUri, isPass, time);
        manager.notify(id, notification);

        if (MainActivity.instance != null)
            MainActivity.instance.changeReminderStatus();
    }

    private Notification createNotification(int id, String title, String text, String imageUri, boolean isPass, long time) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("from_notification", true);
        notificationIntent.putExtra("notification_id", id);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(Utils.getTextForNotif(context, text, imageUri, isPass))
                .setLargeIcon(Utils.getImageBitmap(context, imageUri))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Utils.getTextForNotif(context, text, imageUri, isPass)))
                .setSmallIcon(R.drawable.ic_notif_small)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setWhen(time)
                .setAutoCancel(true)
                .setSound(Utils.getSelectedRingtoneUri(context))
                .setLights(Utils.getLedColor(context), 800, 300)
                .setVibrate(Utils.getVibrationPattern(context));

        if (text.equals("") && !imageUri.equals(""))
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(Utils.getImageBitmap(context, imageUri)).bigLargeIcon(null));

        return builder.build();
    }
}