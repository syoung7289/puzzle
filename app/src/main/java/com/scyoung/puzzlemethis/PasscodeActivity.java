package com.scyoung.puzzlemethis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class PasscodeActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void setPreferenceTrue(View view) {
        setPreference(true);
    }

    public void setPreferenceFalse(View view) {
        setPreference(false);
    }
    private void setPreference(boolean b) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("admin_restriction", b);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getResources().getString(R.string.passcode_set_result), b);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
