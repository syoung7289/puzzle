package com.scyoung.puzzlemethis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import com.scyoung.puzzlemethis.Util.ImageUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

public class PuzzleHome extends AppCompatActivity implements CategoryFragment.OnFragmentInteractionListener, MixAndMatchFragment.OnFragmentInteractionListener {

    private SharedPreferences prefs;
    private ImageView container;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private static boolean showMixAndMatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.puzzleme_logo_no_background_wider);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        container = (ImageView)findViewById(R.id.image_container);
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);

        if (savedInstanceState != null) {
            showMixAndMatch = savedInstanceState.getBoolean("showMixAndMatch", false);
        }

        // Load first fragment
        if (showMixAndMatch) {
            showMixAndMatch(findViewById(R.id.mixAndMatchButton));
        }
        else {
            showCategories(findViewById(R.id.categoriesButton));
        }

//        Map<String, ?> allEntries = prefs.getAll();
//        SharedPreferences.Editor editor = prefs.edit();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            if (!entry.getKey().contains("pref")) {
//                deleteSharedPreferenceFile((String) entry.getValue());
//            }
//            editor.remove(entry.getKey());
//        }
//        editor.commit();
//        Log.d("Flushed Preferences: ", "complete");

        initPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap background = ImageUtil.getScaledBitmapFromResources(R.drawable.puzzle_pieces_white_corner, size.x, size.y, this);
        container.setImageBitmap(background);
    }

    @Override
    protected void onStop() {
        super.onStop();
        container.setImageResource(0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("showMixAndMatch", findViewById(R.id.mixAndMatchButton).isActivated());
        super.onSaveInstanceState(outState);
    }

    /**
     * This will seed the preferences with the encoded no image for default button usage
     */
    private void initPreferences() {
        SharedPreferences.Editor editor;

//        editor = prefs.edit();
//        editor.putInt("pref_num_button_default", 3);
//        editor.commit();

        //write no image pic to preferences as encoded string
        if (prefs.getString(getString(R.string.no_image_key), null) == null) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.noimage_large);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

            editor = prefs.edit();
            editor.putString(getString(R.string.no_image_key), imageEncoded);
            editor.commit();
        }

        // write no image pic to internal storage and set in preferences with new filename
        if (prefs.getString(getString(R.string.no_image_uri_key), null) == null) {
            String fileName = getString(R.string.no_image_uri_key);
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.noimage_large);
            File newInternalFile = ImageUtil.writeBitmapToInternalStorage(this, fileName, image);
            if (newInternalFile != null) {
                editor = prefs.edit();
                editor.putString(fileName, newInternalFile.toString());
                editor.commit();
            }
        }

        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onCategorySelected(String categoryName) {
        Log.d("PuzAct", "Inside onCategorySelected and " + categoryName + " was passed");
        Intent intent = new Intent(this, CategoryBuilder.class);
        intent.putExtra(getResources().getString(R.string.category_to_pass), categoryName);
        startActivity(intent);
    }

    public void showCategories(View view) {
        view.setActivated(true);
        findViewById(R.id.mixAndMatchButton).setActivated(false);
        CategoryFragment categoryFragment = new CategoryFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, categoryFragment)
                .addToBackStack("mixAndMatch")
                .commit();
    }

    public void showMixAndMatch(View view) {
        view.setActivated(true);
        findViewById(R.id.categoriesButton).setActivated(false);
        MixAndMatchFragment mixAndMatchFragment = new MixAndMatchFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mixAndMatchFragment)
                .addToBackStack("categories")
                .commit();
    }

    public void setPreferences(View view) {

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
