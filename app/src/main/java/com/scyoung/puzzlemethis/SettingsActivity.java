package com.scyoung.puzzlemethis;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.scyoung.puzzlemethis.preference.DialogExPreference;
import com.scyoung.puzzlemethis.preference.PasscodeSwitchPreference;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                CharSequence summary = index >= 0 ? listPreference.getEntries()[index] : null;
                CharSequence sanitizedSummary = null;
                for (int i = 0; i < summary.length(); i++) {
                    if ((summary.charAt(i) == '(')) {
                        sanitizedSummary = summary.subSequence(0, i-1);
                        break;
                    }
                }
                summary = sanitizedSummary != null ? sanitizedSummary : summary;
                if (summary != null) {
                    preference.setSummary(summary);
                }

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("Silent");

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setScreenOrientation();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.puzzleme_logo_no_background_wider);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AdminPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataManagementPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdminPreferenceFragment extends PreferenceFragment {
        private static final int PASSCODE_RESULT = 0;
        private SharedPreferences prefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_admin);
            setHasOptionsMenu(true);
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


            findPreference("admin_restriction").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent i = new Intent(getActivity(), PasscodeActivity.class);
                    startActivityForResult(i, PASSCODE_RESULT);
                    return true;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case (PASSCODE_RESULT) : {
                    if (resultCode == Activity.RESULT_OK) {
                        boolean passcodeSuccess = data.getBooleanExtra(getResources().getString(R.string.passcode_success), false);
                        int passcodeAction = data.getIntExtra(getResources().getString(R.string.passcode_action), 99);
                        PasscodeSwitchPreference psPref = (PasscodeSwitchPreference)findPreference("admin_restriction");
                        if (passcodeSuccess && passcodeAction == PasscodeActivity.SET) {
                            psPref.setChecked(true);
                        }
                        else if (passcodeSuccess && passcodeAction == PasscodeActivity.VERIFY) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove("user_passcode").commit();
                            psPref.setChecked(false);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        public static final int LOW = 0;
        public static final int MED = 1;
        public static final int HIGH = 2;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("screen_orient"));
            bindPreferenceSummaryToValue(findPreference("num_images"));
            bindPreferenceSummaryToValue(findPreference("audio_quality"));
            bindScreenPreferenceSummary(findPreference("screen_orient"));
        }

        private void bindScreenPreferenceSummary(Preference screenOrientPref) {
            ListPreference screenOrientListPref = (ListPreference)screenOrientPref;
            int index = screenOrientListPref.findIndexOfValue(screenOrientListPref.getValue().toString());
            screenOrientListPref.setSummary(
                    index >= 0
                            ? screenOrientListPref.getEntries()[index]
                            : null);

            screenOrientPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ("PORTRAIT".equals(newValue)) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else if ("LANDSCAPE".equals(newValue)){
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(newValue.toString());

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataManagementPreferenceFragment extends PreferenceFragment {
        private SharedPreferences prefs;
        private static final int PASSCODE_RESULT = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_mgmt);
            setHasOptionsMenu(true);
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            findPreference("factory_defaults").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean clickHandled = true;
                    if (isPasscodeSet() && !prefs.getBoolean("password_prompt", false)) {
                        ((DialogExPreference)preference).getDialog().dismiss();
                        Intent i = new Intent(getActivity(), PasscodeActivity.class);
                        startActivityForResult(i, PASSCODE_RESULT);
                        clickHandled = false;
                    }
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("password_prompt").commit();
                    return clickHandled;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case (PASSCODE_RESULT) : {
                    if (resultCode == Activity.RESULT_OK) {
                        boolean passcodeSuccess = data.getBooleanExtra(getResources().getString(R.string.passcode_success), false);
                        if (passcodeSuccess) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("password_prompt", true).commit();
                            PreferenceScreen screen = (PreferenceScreen)findPreference("data_management_screen");
                            int order = findPreference("factory_defaults").getOrder();
                            screen.onItemClick(null, null, order, 0);
                        }
                    }
                    break;
                }
            }
        }

        private boolean isPasscodeSet() {
            boolean isSet = false;
            if (prefs != null) {
                isSet = !(prefs.getString("user_passcode", "")).isEmpty();
            }
            return isSet;
        }
    }

    protected void setScreenOrientation() {
        String orientation = PreferenceManager.getDefaultSharedPreferences(this).getString("screen_orient", "BOTH");
        if ("PORTRAIT".equals(orientation)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if ("LANDSCAPE".equals(orientation)){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}
