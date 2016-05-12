package com.scyoung.puzzlemethis.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

import com.scyoung.puzzlemethis.Util.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by scyoung on 5/9/16.
 */
public class BulkDeleteListPreference extends MultiSelectListPreference {
    private SharedPreferences prefs;

    public BulkDeleteListPreference(Context context, AttributeSet attributes) {
        super(context, attributes);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setValues(new HashSet<String>());
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        SortedSet<String> uniqueCategories = new TreeSet<String>();
        SortedSet<String> uniqueCategoryKeys = new TreeSet<String>();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("~")) {
                String prefCategory = entry.getKey().split("~")[0];
                uniqueCategoryKeys.add(prefCategory);
                String displayCategory = StringUtil.convertFromCondensedUpperCase(prefCategory);
                uniqueCategories.add(displayCategory);
            }
        }
        Log.d("Bulk", "entries being set to " + uniqueCategories);
        Log.d("Bulk", "entryValues being set to " + uniqueCategoryKeys);
        setEntries(uniqueCategories.toArray(new CharSequence[]{}));
        setEntryValues(uniqueCategoryKeys.toArray(new CharSequence[]{}));
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SharedPreferences.Editor editor = prefs.edit();
            Map<String, ?> allEntries = prefs.getAll();
            for (String categoryToRemove : this.getValues()) {
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("~")) {
                        if (categoryToRemove.equals(key.split("~")[0])) {
                            editor.remove(key);
                            deleteSharedPreferenceFile((String) entry.getValue());
                        }
                    }
                }
            }
            editor.commit();
            Log.d("Bulk", "values: " + this.getValues());
            Log.d("Bulk", "Entries: " + Arrays.toString(this.getEntries()));
            Log.d("Bulk", "Entry Values: " + Arrays.toString(this.getEntryValues()));
            setValues(new HashSet<String>());
        }
        else {
            Log.d("Bulk", "inside negative results else");
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
