package se.christianwiedel.timerandroid;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service implements Handler.Callback{

    private static final int NOTIFICATION_ID = 1000;
    private static final int COUNTDOWN_TIMER_MSG = 2000;
    private static final String TAG = "TimerService";
    private static final int ALARM_TIMER_MSG = 2342;
    public static long TIMER_START_VALUE = 3000;
    private final LocalBinder mLocalBinder = new LocalBinder();
    private TimerCallback mTimerCallback;
    private NotificationCompat.Builder mNotification;
    private NotificationManager mNotificationManager;
    private long mTimerMsLeft;
    private Handler mHandler;
    private long mStartTime;
    private boolean mTimerIsRunning = false;
    private Ringtone mRingtone;

    public TimerService() {
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == COUNTDOWN_TIMER_MSG) {
            Log.d(TAG, "ms left: " +mTimerMsLeft);
            long now = SystemClock.elapsedRealtime();
            mTimerMsLeft = TIMER_START_VALUE - (now - mStartTime);
            if (mTimerMsLeft >= 0 && mTimerIsRunning) {
                notifyTimerCallback();
                mHandler.sendEmptyMessageDelayed(COUNTDOWN_TIMER_MSG, 100);
            } else if(mTimerMsLeft <= 500) {
                triggerAlarm();
            }
        }
        if(msg.what == ALARM_TIMER_MSG) {
            if (mRingtone.isPlaying()) {
                mRingtone.stop();
            }
        }
        return false;
    }


    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper(), this);
    }

    public void startTimer() {
        startService(new Intent(this, getClass()));
        startTimeInForeground();
        mTimerIsRunning = true;
    }

    public boolean isTimerRunning() {
        if(mTimerIsRunning){
            return true;
        }else {
            return false;
        }
    }

    private void startTimeInForeground() {

      //  mNotification = NewMessageNotification.buildNotification(this, "The timer is running", getTimerValue(), "Timer has started", 1);
       // startForeground(NOTIFICATION_ID, mNotification);

        mNotification = NewMessageNotification.buildNotification(this, "The timer is running",
                mTimerMsLeft/1000, "Timer has started!", 1);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("sectionNumber", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        mNotification.setContentIntent(notifyIntent);
        mNotification.setWhen(System.currentTimeMillis() + TIMER_START_VALUE);

        startForeground(NOTIFICATION_ID, mNotification.build());

        mStartTime = SystemClock.elapsedRealtime();
        mTimerMsLeft = TIMER_START_VALUE;
        mHandler.sendEmptyMessage(COUNTDOWN_TIMER_MSG);
    }

    public void triggerAlarm() {

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        mRingtone.play();

        mHandler.sendEmptyMessageDelayed(ALARM_TIMER_MSG, 30000);

      //  Toast.makeText(this, "TIMER", Toast.LENGTH_SHORT).show();

        // ALARM MANAGER
        // PENDING INTENT
        // BROADCAST _> MAIN ACTIVITY

        Intent alarmIntent = new Intent(this, MainActivity.class);
        alarmIntent.putExtra("alertDialog", 20);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmIntent);

    }

    public void stopAlarm() {
        if(mRingtone != null) {
            mRingtone.stop();
        }
    }


    public void stopTimer() {
        stopForeground(true);
        mTimerIsRunning = false;
        TIMER_START_VALUE = mTimerMsLeft;
    }

    public void resetTimer() {
        TIMER_START_VALUE = 0;
        mTimerIsRunning = false;
    }

    public void setTimerValue(long newTimerValue) {
        TIMER_START_VALUE = newTimerValue;
    }

    public long getTimerValue() {
        return mTimerMsLeft;
    }

    private void notifyTimerCallback() {
        if(mTimerMsLeft < 0) {
            mTimerMsLeft = 0;
        }
        if(mTimerCallback != null) {
            mTimerCallback.onTimerValueChanged(mTimerMsLeft);
        }
    }

    public void setTimerCallback(TimerCallback timerCallback) {
        mTimerCallback = timerCallback;
    }

    public interface TimerCallback {
        void onTimerValueChanged(long timerValue);
    }
}
