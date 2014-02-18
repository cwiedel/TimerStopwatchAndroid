package se.christianwiedel.timerandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;


public class StopwatchService extends Service implements Handler.Callback {

    private static final int NOTIFICATION_ID = 1001;
    private static final int STOPWATCH_MSG = 2000;
    private static final int UPDATE_FREQ = 10;

    private final LocalBinder mLocalBinder = new LocalBinder();
    private StopwatchCallback mStopwatchCallback;
    private Handler mHandler;
    private long mStopwatchStartTime;
    private boolean mStopwatchIsRunning = false;
    private long mLastStopWatchTime;
    private long mElapsedStopwatchTime;
    private NotificationCompat.Builder mNotification;
    private NotificationManager mNotificationManager;
    public long mCurrLapTime = 0;
    private long mStopwatchLapTimeValue;

    public StopwatchService() {
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == STOPWATCH_MSG) {
            long now = SystemClock.elapsedRealtime();
            mElapsedStopwatchTime = now - mStopwatchStartTime;
            mStopwatchLapTimeValue = mElapsedStopwatchTime - mCurrLapTime;

            if (mStopwatchIsRunning) {
                notifyStopwatchCallback();
                mHandler.sendEmptyMessageDelayed(STOPWATCH_MSG, UPDATE_FREQ);

            }
        }
        return false;
    }


    public class LocalBinder extends Binder {
        public StopwatchService getService() {
            return StopwatchService.this;
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

    public void startStopwatch() {
        startService(new Intent(this, getClass()));
        startTimeInForeground();
    }

    public boolean isStopwatchRunning() {
        if(mStopwatchIsRunning){
            return true;
        }else {
            return false;
        }
    }

    private void startTimeInForeground() {
        // TODO Kontrollera ifall notification redan är satt!!!

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(!mStopwatchIsRunning) {
            mStopwatchIsRunning = true;

            mNotification = NewMessageNotification.buildNotification(this, "The stopwatch is running",
                    mElapsedStopwatchTime/1000, "Stopwatch has started!", 1);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("sectionNumber", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Creates the PendingIntent
            PendingIntent notifyIntent =
                    PendingIntent.getActivity(
                            this,
                            1,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // Puts the PendingIntent into the notification builder
            mNotification.setContentIntent(notifyIntent);
            mNotification.setWhen(System.currentTimeMillis());

            startForeground(NOTIFICATION_ID, mNotification.build());

            if (mStopwatchStartTime == 0) {
                mStopwatchStartTime = SystemClock.elapsedRealtime();
            } else {
                mStopwatchStartTime = SystemClock.elapsedRealtime() - mLastStopWatchTime;
            }
            mHandler.sendEmptyMessage(STOPWATCH_MSG);
        }
    }

    public void stopStopwatch() {
        stopForeground(true);
        mStopwatchIsRunning = false;
        mLastStopWatchTime = SystemClock.elapsedRealtime() - mStopwatchStartTime;

    }

    public void resetStopwatch() {
        mStopwatchStartTime = SystemClock.elapsedRealtime();
        if(!mStopwatchIsRunning) {
            mLastStopWatchTime = 0;
        }
    }

    public void setStopwatchValue(long newTimerValue) {
        mStopwatchStartTime = newTimerValue;
    }

    public long getStopwatchValue() {
        return mElapsedStopwatchTime;
    }

    private void notifyStopwatchCallback() {
        if(mStopwatchCallback != null) {
            mStopwatchCallback.onStopwatchValueChanged(mElapsedStopwatchTime, mStopwatchLapTimeValue);
        }
    }

    public void setStopwatchCallback(StopwatchCallback stopwatchCallback) {
        mStopwatchCallback = stopwatchCallback;
    }

    public interface StopwatchCallback {
        void onStopwatchValueChanged(long timerValue, long stopwatchLapValue);
    }


}
