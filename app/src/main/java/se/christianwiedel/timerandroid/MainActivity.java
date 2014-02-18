package se.christianwiedel.timerandroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivity extends Activity implements TimerService.TimerCallback, StopwatchService.StopwatchCallback {

    public static final String LAST_PAGE_KEY = "lastPage";
    public static final int TIMER_POSITION = 0;
    public static final int STOPWATCH_POSITION = 1;

    public static final int TIMER_RESETBTN_DISABLE = 0;
    public static final int TIMER_RESETBTN_ENABLE = 1;

    public static final int TIMER_STARTBTN_START = 0;
    public static final int TIMER_STARTBTN_PAUSE = 1;

    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private static TimerService mTimerService;
    private TimerServiceConnection mTimerServiceConnection;
    private StopwatchService mStopwatchService;
    private StopwatchServiceConnection mStopwatchServiceConnection;
    private PlaceholderFragment mTimerFragment;
    private PlaceholderFragment mStopwatchFragment;
    private int mSectionNumber = -1;
    private ActionBar mActionBar;
    private int mLastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBar = getActionBar();
        setActionBarTitle("onCreate");
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ffFEBB31"));
        mActionBar.setBackgroundDrawable(colorDrawable);
        mTimerFragment = PlaceholderFragment.newInstance(0);
        mStopwatchFragment = PlaceholderFragment.newInstance(1);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if(savedInstanceState != null) {
            mLastPage = savedInstanceState.getInt(LAST_PAGE_KEY, 0);
        }
        Log.d("onCreate", "mSectionNumber: " +mSectionNumber + "mLastPage: " +mLastPage);
    }

    public void setActionBarTitle(String title) {
        mActionBar.setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mSectionNumber = intent.getIntExtra("sectionNumber", -1);
        int foo = intent.getIntExtra("alertDialog", -1);
       // Log.d("OnNewIntent", "mSectionNumber: " +mSectionNumber);
       // Log.d("OnNewIntent", "foo: " +foo);

        // DIALOG FRAGMENT
        // ALERT DIALOG
        // ALARM RECEIVER BROADCAST RECEIVER

        if(foo > 0) {
            Log.d("onNewIntent", "alarm intent recevied");
            DialogFragment df = new AlarmDialogFragment();
            df.show(getFragmentManager(), "alarmDialog");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
//        mViewPager.setCurrentItem(mSectionNumber);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTimerServiceConnection = new TimerServiceConnection();
        bindService(new Intent(this, TimerService.class),
                mTimerServiceConnection, BIND_AUTO_CREATE);

        mStopwatchServiceConnection = new StopwatchServiceConnection();
        bindService(new Intent(this, StopwatchService.class),
                mStopwatchServiceConnection, BIND_AUTO_CREATE );

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mSectionNumber != -1) {
            mViewPager.setCurrentItem(mSectionNumber);
        } else {
            mViewPager.setCurrentItem(mLastPage);
        }
        Log.d("onResume", "mSectionNumber: " +mSectionNumber + ", mLastPage: " +mLastPage);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimerService.setTimerCallback(null);
        unbindService(mTimerServiceConnection);

        mStopwatchService.setStopwatchCallback(null);
        unbindService(mStopwatchServiceConnection);

        mLastPage = mViewPager.getCurrentItem();

        Log.d("onPause:", "mViewPager: " + mViewPager.getCurrentItem() );

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLastPage = mViewPager.getCurrentItem();
        outState.putInt(LAST_PAGE_KEY, mLastPage);
        Log.d("onSaveInstanceState:", "onSaveInstanceState: " + mViewPager.getCurrentItem() );
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLastPage = savedInstanceState.getInt(LAST_PAGE_KEY, 0);
        Log.d("onRestoreInstanceState:", "onRestoreInstanceState: " + mViewPager.getCurrentItem() );
    }

    class TimerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimerService = ((TimerService.LocalBinder) service).getService();
            mTimerService.setTimerCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
        }
    }

    class StopwatchServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mStopwatchService = ((StopwatchService.LocalBinder) service).getService();
            mStopwatchService.setStopwatchCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStopwatchService = null;
        }
    }


    @Override
    public void onTimerValueChanged(long timerValue) {
        mTimerFragment.updateTimerValue(timerValue);
    }

    @Override
    public void onStopwatchValueChanged(long stopwatchValue, long lapValue) {
        mStopwatchFragment.updateStopwatchValue(stopwatchValue);
        mStopwatchFragment.updateStopwatchLap(lapValue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***   STOPWATCH  ***/

    public void startStopWatch(View view){
        if(mStopwatchService != null) {

            if(!mStopwatchService.isStopwatchRunning()) {
                mStopwatchService.startStopwatch();
                mStopwatchFragment.mStopwatchResetButton.setText("Lap");
                mStopwatchFragment.mStopwatchStartButton.setText("Stop");
                mStopwatchFragment.mStopwatchStartButton.setBackgroundResource(R.drawable.round_red);
            }else {
                mStopwatchService.stopStopwatch();
                mStopwatchFragment.mStopwatchResetButton.setText("Reset");
                mStopwatchFragment.mStopwatchStartButton.setText("Start");
                mStopwatchFragment.mStopwatchStartButton.setBackgroundResource(R.drawable.round_green);
            }
        }
    }

    public void stopStopWatch(View view){
        if(mStopwatchService != null) {
            mStopwatchService.stopStopwatch();
          //  mStopwatchFragment.updateStopwatchValue(0);
            mStopwatchFragment.mStopwatchResetButton.setText(R.string.reset_button_title);
        }

    }

    public void resetStopWatch(View view){
        if(mStopwatchService != null) {
            if(mStopwatchService.isStopwatchRunning()){
                mStopwatchFragment.writeStopwatchLap();
                mStopwatchService.mCurrLapTime = mStopwatchService.getStopwatchValue();
            }else {
                mStopwatchService.resetStopwatch();
                mStopwatchFragment.updateStopwatchValue(0);
                mStopwatchFragment.updateStopwatchLap(0);
                mStopwatchService.mCurrLapTime = 0;
                mStopwatchFragment.mSavedStopWatchValues.clear();
                mStopwatchFragment.mAdapter.notifyDataSetChanged();

            }
        }
    }


    /***   TIMER  ***/

    public void onStartTimer(View view) {
        if(mTimerService != null && mTimerService.TIMER_START_VALUE != 0) {
            if(!mTimerService.isTimerRunning()) {
                mTimerService.startTimer();
                setTimerResetButton(TIMER_RESETBTN_ENABLE);
                setTimerStartButton(TIMER_STARTBTN_PAUSE);
                mTimerFragment.mTimePicker.setEnabled(false);
            }else {
                mTimerService.stopTimer();
               // setTimerResetButton(TIMER_RESETBTN_ENABLE);
                mTimerFragment.mTimePicker.setEnabled(true);
                setTimerStartButton(TIMER_STARTBTN_START);
            }
        }
    }

    public void onResetTimer(View view) {
        if (mTimerService != null) {
            setTimerResetButton(TIMER_RESETBTN_DISABLE);
            setTimerStartButton(TIMER_STARTBTN_START);
            mTimerService.resetTimer();
            mTimerFragment.updateTimerValue(0);
            mTimerFragment.mTimePicker.setEnabled(true);
            mTimerFragment.mTimePicker.setCurrentHour(0);
            mTimerFragment.mTimePicker.setCurrentMinute(0);
        }
    }

    public void setTimerResetButton(int state) {
        switch (state) {
            case TIMER_RESETBTN_DISABLE:
                mTimerFragment.mTimerResetButton.setBackgroundResource(R.drawable.round_inactive_gray);
                mTimerFragment.mTimerResetButton.setTextColor(getResources().getColor(R.color.inactive_gray));
                mTimerFragment.mTimerResetButton.setEnabled(false);
                break;
            case TIMER_RESETBTN_ENABLE:
                mTimerFragment.mTimerResetButton.setBackgroundResource(R.drawable.round_black);
                mTimerFragment.mTimerResetButton.setTextColor(getResources().getColor(R.color.button_font_color));
                mTimerFragment.mTimerResetButton.setEnabled(true);
                break;
        }
    }

    public void setTimerStartButton(int state) {
        switch (state) {
            case TIMER_STARTBTN_START:
                mTimerFragment.mTimerStartButton.setText("Start");
                mTimerFragment.mTimerStartButton.setBackgroundResource(R.drawable.round_green);
                break;
            case TIMER_STARTBTN_PAUSE:
                mTimerFragment.mTimerStartButton.setText("Pause");
                mTimerFragment.mTimerStartButton.setBackgroundResource(R.drawable.round_red);
                break;
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case TIMER_POSITION:

                    return mTimerFragment;
                case STOPWATCH_POSITION:

                    return mStopwatchFragment;
                default:
                    return null;
            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            //super.setPrimaryItem(container, position, object);
            //Log.d("SectionsPagerAdapter", "setPrimaryItem: " +position);

            switch (position) {
                case TIMER_POSITION:
                    getActionBar().setTitle("Timer");
                    break;
                case STOPWATCH_POSITION:
                    getActionBar().setTitle("Stopwatch");
                    break;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
/*
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.timer_title_section).toUpperCase(l);
                case 1:
                    return getString(R.string.stopwatch_title_section).toUpperCase(l);
            }
            return null;
        }
        */
    }

    public class AlarmDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_timer_alert)
                    .setPositiveButton(R.string.alarm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                            mTimerService.stopAlarm();
                            mTimerService.resetTimer();
                        }
                    });

            return builder.create();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String TAG = "PlaceholderFragment";
        public static ArrayList <String> mSavedStopWatchValues;
        public static ArrayAdapter <String> mAdapter;

        private static TextView mStopwatchTextView;
        private static TextView mTimerTextView;
        private static Button mStopwatchResetButton;
        private static Button mStopwatchStartButton;
        private static Button mTimerStartButton;
        private static Button mTimerResetButton;
        private static TextView mStopwatchLapView;
        private static TimePicker mTimePicker;

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            Bundle args = getArguments();
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            View rootView = null;

            switch (sectionNumber) {
                case TIMER_POSITION:
                    View timerView = inflater.inflate(R.layout.timer, container, false);
                    mTimerTextView = (TextView) timerView.findViewById(R.id.timer_textView);
                    mTimerStartButton = (Button) timerView.findViewById(R.id.timer_start_button);
                    mTimerResetButton = (Button) timerView.findViewById(R.id.timer_reset_button);
                    mTimePicker = (TimePicker) timerView.findViewById(R.id.timer_timePicker);
                    mTimePicker.setIs24HourView(true);
                    mTimePicker.setCurrentHour(0);
                    mTimePicker.setCurrentMinute(0);
                    mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

                        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                            updateDisplay(hourOfDay, minute);
                        }

                        private void updateDisplay(int hourOfDay, int minute) {

                            long pickedTimerValue = hourOfDay * 60 + minute;
                            pickedTimerValue *=60000;
                            updateTimerValue(pickedTimerValue);
                            mTimerService.TIMER_START_VALUE = pickedTimerValue;
                        }

                    });

                    ((MainActivity) getActivity()).setActionBarTitle("Timer");
                    return timerView;
                case STOPWATCH_POSITION:
                    mSavedStopWatchValues = new ArrayList<String>();
                    mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.stopwatch_item, R.id.stopwatch_item_value, mSavedStopWatchValues );
                    View stopwatchView = inflater.inflate(R.layout.stopwatch, container, false);
                    mStopwatchTextView = (TextView) stopwatchView.findViewById(R.id.stopwatch_textView);
                    mStopwatchLapView = (TextView) stopwatchView.findViewById(R.id.stopwatch_laptextView);
                    mStopwatchResetButton = (Button) stopwatchView.findViewById(R.id.stopwatch_reset_button);
                    mStopwatchStartButton = (Button) stopwatchView.findViewById(R.id.stopwatch_start_button);
                    ListView listView = (ListView) stopwatchView.findViewById(R.id.stopwatch_listview);
                    listView.setAdapter(mAdapter);
                    ((MainActivity) getActivity()).setActionBarTitle("Stopwatch");
                    return stopwatchView;
                }
            return null;
        }

        public void updateTimerValue(long timerValue) {
            if (mTimerTextView != null) {

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                CharSequence timeForTimer = simpleDateFormat.format(new Date(timerValue));
                mTimerTextView.setText(timeForTimer.subSequence(0,8));
            }
            Log.d(TAG, "timerValue: "+timerValue);
        }

        public void updateStopwatchValue(long stopwatchValue) {
            if(mStopwatchTextView != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS", Locale.ENGLISH);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                CharSequence timeForStopWatch = simpleDateFormat.format(new Date(stopwatchValue));

                mStopwatchTextView.setText(timeForStopWatch.subSequence(0,8));
            }
        }

        public void updateStopwatchLap(long stopwatchLap) {
            if(mStopwatchLapView != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS", Locale.ENGLISH);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                CharSequence timeForStopWatch = simpleDateFormat.format(new Date(stopwatchLap));
                mStopwatchLapView.setText(timeForStopWatch.subSequence(0,8));
            }
        }
        public void writeStopwatchLap() {
           String lap = mStopwatchLapView.getText().toString();
            int lapCount = mSavedStopWatchValues.size() +1;
            mSavedStopWatchValues.add("Lap " +lapCount + ": "+ lap);
            mAdapter.notifyDataSetChanged();
        }

        public void showAlarmDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                   // mTimerService.stopAlarm();
                }
            });

            AlertDialog dialog = builder.create();
        }
    }

}
