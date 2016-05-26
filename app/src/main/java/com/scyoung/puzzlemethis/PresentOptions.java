package com.scyoung.puzzlemethis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.scyoung.puzzlemethis.Util.ImageUtil;

import java.util.ArrayList;
import java.util.Date;

public class PresentOptions extends AppCompatActivity {
    private static String SCREEN_ORIENTATION;
    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private boolean shouldRedraw = false;
    private SharedPreferences prefs;
    private Resources res;
    RelativeLayout container;
    ImageButton[] viewButtons = new ImageButton[6];
    int margin = 10;
    private ArrayList<String> optionsToPresent = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present_options);
        int rotation = this.getWindow().getWindowManager().getDefaultDisplay().getRotation();
        SCREEN_ORIENTATION = ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) ? "LANDSCAPE" : "PORTRAIT");
        setDisplayDimensions();
        Log.d("CA", "onCreate started: " + SCREEN_ORIENTATION);
        if (savedInstanceState != null) {
            optionsToPresent = savedInstanceState.getStringArrayList(getResources().getString(R.string.options_to_present));
        }
        else {
            optionsToPresent = getIntent().getStringArrayListExtra(getResources().getString(R.string.options_to_present));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.puzzleme_logo_no_background_wider);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shouldRedraw = false;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        res = getResources();
        container = (RelativeLayout) findViewById(R.id.options_container);
        final ViewTreeObserver vto = container.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (shouldRedraw) {
                        redrawButtons();
                        shouldRedraw = false;
                        container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        buildButtons();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d("CatBuild", "onWindowFocusChanged");
        super.onWindowFocusChanged(hasFocus);
        shouldRedraw = hasFocus && (viewButtons[0].getWidth() == 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(getResources().getString(R.string.options_to_present), optionsToPresent);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buildButtons() {
        initButtonArray();
        int maxButtonSide = getMinimumButtonDimension();
        Log.d("buildButton", "starting at " + (new Date()).toString());

        for (int i = 0; i < optionsToPresent.size(); i++) {
            viewButtons[i].setOnTouchListener(aButtonTouchEffect);
            viewButtons[i].setOnClickListener(buttonSelectedClickListener);
            viewButtons[i].setVisibility(View.VISIBLE);
            viewButtons[i].setTag(optionsToPresent.get(i));
            String imageLocation = prefs.getString(optionsToPresent.get(i), null);
            Uri imageUri = imageLocation != null ? Uri.parse(imageLocation) : null;

            Log.d("buildButton", "before setScaled for loop " + i +  " at " + (new Date()).toString());
            viewButtons[i].setImageBitmap(
                    ImageUtil.getScaledBitmapFromStorage(imageUri, maxButtonSide, maxButtonSide));
            viewButtons[i].setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void initButtonArray() {
        viewButtons[0] = (ImageButton) findViewById(R.id.categoryButton0);
        viewButtons[1] = (ImageButton) findViewById(R.id.categoryButton1);
        viewButtons[2] = (ImageButton) findViewById(R.id.categoryButton2);
        viewButtons[3] = (ImageButton) findViewById(R.id.categoryButton3);
        viewButtons[4] = (ImageButton) findViewById(R.id.categoryButton4);
        viewButtons[5] = (ImageButton) findViewById(R.id.categoryButton5);
        initButtonVisibility();
    }

    private void initButtonVisibility() {
        int numInitialButtons = optionsToPresent.size();
        for (int i=0; i<viewButtons.length; i++) {
            if (i+1 > numInitialButtons) {
                viewButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private void redrawButtons() {
        Log.d("CA", "redrawButtons started");
        int lvi = getLastVisibleIndex();
        int dimension = getButtonDimension(getNumColumns(lvi), getNumRows(lvi));
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewButtons[i].getLayoutParams();
            params.width = dimension;
            params.height = dimension;
            params.rightMargin = margin;
            params.bottomMargin=margin;
            viewButtons[i].setLayoutParams(params);
        }
        Log.d("CA", "redrawButtons with dimension: " + dimension);
        Log.d("CA", "redrawButtons ended");
    }

    public void buttonSelected(View view) {
        Intent intent = new Intent(this, ShowSelection.class);
        String imageKey = (String)view.getTag();
        String audioKey = imageKey.split("IMAGE")[0] + "AUDIO";
        String picSelected = prefs.getString(imageKey, null);
        String assocAudio = prefs.getString(audioKey, "");
        intent.putExtra("picSelected", picSelected);
        intent.putExtra("assocAudio", assocAudio);
        String transition = view.getTransitionName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    view,
                    transition);
            startActivity(intent, options.toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private void setDisplayDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DISPLAY_WIDTH = size.y;
        DISPLAY_HEIGHT = size.x;
    }

    Button.OnTouchListener aButtonTouchEffect = new Button.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ((ImageView)v).setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    ((ImageView)v).clearColorFilter();
                    v.invalidate();
                    break;
                }
            }
            return false;
        }
    };

    ImageButton.OnClickListener buttonSelectedClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonSelected(v);
        }
    };

    private int getMinimumButtonDimension() {
        int maxButtonIndex = optionsToPresent.size() - 1;
        return Math.min(DISPLAY_HEIGHT / getNumRows(maxButtonIndex), DISPLAY_WIDTH / getNumColumns(maxButtonIndex));
    }

    public int getLastVisibleIndex() {
        for (int i = viewButtons.length - 1; i >= 0; i--) {
            if (viewButtons[i].getVisibility() != View.GONE) {
                return i;      //previous index which would have passed the visibility test
            }
        }
        return 0;
    }

    private int getButtonDimension(int numColumns, int numRows) {
        int width = container.getWidth();
        int height = container.getHeight();
        int vFreeSpace = getVerticalFreeSpace(height, numRows);
        int hFreeSpace = getHorizontalFreeSpace(width, numColumns);
        return Math.min(vFreeSpace / numRows, hFreeSpace / numColumns);
    }

    private int getHorizontalFreeSpace(int containerWidth, int numColumns) {
        Log.d("CA", "containerWidth: " + containerWidth);
        Log.d("CA", "numColumns: " + numColumns);
        int cPadding = (numColumns + 1) * margin;
        Log.d("CA", "cPadding: " + cPadding);
        return containerWidth - cPadding;
    }

    private int getVerticalFreeSpace(int containerHeight, int numRows) {
        Log.d("CA", "containerHeight: " + containerHeight);
        Log.d("CA", "numRows: " + numRows);
        int rPadding = (numRows + 1) * margin;
        Log.d("CA", "rPadding: " + rPadding);
        return containerHeight - rPadding;
    }

    private int getNumRows(int currentIndex) {
        if (currentIndex > 2) {
            return 2;
        }
        else {
            return 1;
        }
    }

    private int getNumColumns(int currentIndex) {
        switch (currentIndex) {
            case 0:
                return 1;
            case 1:
                return 2;
            default:
                return 3;
        }
    }
}
