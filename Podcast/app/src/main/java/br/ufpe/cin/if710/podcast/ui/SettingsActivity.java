package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import br.ufpe.cin.if710.podcast.R;

public class SettingsActivity extends Activity {
    public static final String FEED_LINK = "feedlink";
    public static final String JOBTIME = "jobTime";
    public static final String CANCEL = "cancel";
    public static final int JOBID = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class FeedPreferenceFragment extends PreferenceFragment {

        protected static final String TAG = "FeedPreferenceFragment";
        private SharedPreferences.OnSharedPreferenceChangeListener myListener;
        private Preference feedLinkPref;

        private SharedPreferences.OnSharedPreferenceChangeListener myListener2;
        private Preference periodoPref;
        private JobScheduler jobScheduler;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            feedLinkPref = (Preference) getPreferenceManager().findPreference(FEED_LINK);
            myListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    feedLinkPref.setSummary(sharedPreferences.getString(FEED_LINK, getActivity().getResources().getString(R.string.feed_link)));
                }
            };

            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(myListener);
            myListener.onSharedPreferenceChanged(prefs, FEED_LINK);
            periodoPref = (Preference) getPreferenceManager().findPreference(JOBTIME);
            myListener2 = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    String s = sharedPreferences.getString(JOBTIME, "10");
                    periodoPref.setSummary("10");
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(myListener2);
            myListener2.onSharedPreferenceChanged(prefs, JOBTIME);
            jobScheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);
            Preference button = getPreferenceManager().findPreference(CANCEL);
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(jobScheduler != null){
                        jobScheduler.cancel(JOBID);
                    }
                    return true;
                }
            });
        }
    }
}