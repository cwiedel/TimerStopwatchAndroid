package se.christianwiedel.timerandroid;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Helper class for showing and canceling new message
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class NewMessageNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "NewMessage";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of new message notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     */
    public static NotificationCompat.Builder buildNotification(final Context context,
                                                 final String exampleString,
                                                 final long timeValue,
                                                 final String ticker,
                                                 final int number) {
        final Resources res = context.getResources();


        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.example_picture);
        final String timeValueString = String.valueOf(timeValue);

        final String title = res.getString(
                R.string.new_message_notification_title_template, exampleString);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                .setSmallIcon(R.drawable.ic_stat_new_message)
                .setContentTitle(title)
                .setContentText(timeValueString)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLargeIcon(picture)
                .setTicker(ticker)
                .setNumber(number)
                .setUsesChronometer(true)
                .setAutoCancel(true);

        return builder;
    }
}