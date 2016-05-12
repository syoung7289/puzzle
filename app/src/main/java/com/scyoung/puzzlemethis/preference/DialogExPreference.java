package com.scyoung.puzzlemethis.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.io.File;
import java.util.Map;

/**
 * Created by scyoung on 5/10/16.
 */
public class DialogExPreference extends DialogPreference {
    private SharedPreferences prefs;

    public DialogExPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SharedPreferences.Editor editor = prefs.edit();
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                if (key.contains("~")) {
                    deleteSharedPreferenceFile((String) entry.getValue());
                }
                editor.remove(key);
            }
            editor.commit();
        }
    }

    private void deleteSharedPreferenceFile(String fileName) {
        try {
            File imageFile = new File(fileName);
            imageFile.delete();
        }
        catch (Exception e) {
            // didn't exist keep going
        }
    }
}
