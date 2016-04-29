package com.scyoung.puzzlemethis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
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

import com.scyoung.puzzlemethis.Util.AudioUtil;
import com.scyoung.puzzlemethis.Util.DateUtil;
import com.scyoung.puzzlemethis.Util.ImageUtil;
import com.scyoung.puzzlemethis.Util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class CategoryBuilder extends AppCompatActivity {

    private static String SCREEN_ORIENTATION;
    private boolean shouldRedraw = false;
    private SharedPreferences prefs;
    private Resources res;
    RelativeLayout container;
    ImageButton[] viewButtons = new ImageButton[6];
    ImageView[] buttonIndicators = new ImageView[6];
    private final int DEFAULT = 0;
    private final int DEFAULT_IMAGE = 1;
    private final int DEFAULT_AUDIO = 2;
    private final int DEFAULT_IMAGE_AUDIO = 3;
    private final String IMAGE_TYPE = "_IMAGE";
    private final String AUDIO_TYPE = "_AUDIO";
    private static MediaPlayer aMediaPlayer = null;
    private MediaRecorder myAudioRecorder;
    int margin = 10;
    private String CURRENT_BUTTON_ABSOLUTE_NAME;
    private String CURRENT_BUTTON_NAME = "name_to_change";
    private int CURRENT_BUTTON_ID;
    private ImageButton CURRENT_BUTTON = null;
    private final int SELECT_PHOTO = 1;
    private final int SELECT_AUDIO = 2;
    private String passedCategory;
    private String categoryTitleCase;
    private String categoryUpperCase;
    private String CURRENT_BUTTON_OUTPUT_FILE = null;
    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int rotation = this.getWindow().getWindowManager().getDefaultDisplay().getRotation();
        SCREEN_ORIENTATION = ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) ? "LANDSCAPE" : "PORTRAIT");
        setDisplayDimensions();
        Log.d("CA", "onCreate started: " + SCREEN_ORIENTATION);
        setContentView(R.layout.activity_category_builder);

        if (savedInstanceState != null) {
            passedCategory = savedInstanceState.getString("passedCategory", "");
        }
        else {
            passedCategory = getIntent().getStringExtra(getResources().getString(R.string.category_to_pass));
        }
        categoryTitleCase = StringUtil.convertToTitleCase(passedCategory);
        categoryUpperCase = StringUtil.convertToCondensedUpperCase(categoryTitleCase);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.puzzleme_logo_no_background_wider);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Category: " + categoryTitleCase);

        shouldRedraw = false;
        prefs = getSharedPreferences(getString(R.string.preference_file), MODE_PRIVATE);
        res = getResources();
        container = (RelativeLayout) findViewById(R.id.category_container);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("passedCategory", passedCategory);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d("CatBuild", "onWindowFocusChanged");
        super.onWindowFocusChanged(hasFocus);
        shouldRedraw = hasFocus && (viewButtons[0].getWidth() == 0);
    }

    public void buildButtons() {
        initButtonArray();
        Log.d("buildButton", "starting at " + (new Date()).toString());

        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }

        boolean firstVisibleFound = false;
        for (int i = viewButtons.length - 1; i >= 0; i--) {
            viewButtons[i].setOnTouchListener(aButtonTouchEffect);
            String buttonName = res.getResourceName(viewButtons[i].getId());
            String replaceImageKey = getCategoryButtonName(buttonName, IMAGE_TYPE);
            boolean isImageSelected = prefs.getString(replaceImageKey, null) != null;
            String imageLocation = prefs.getString(replaceImageKey, (prefs.getString(getString(R.string.no_image_uri_key), null)));
            Uri imageUri = Uri.parse(imageLocation);
            if (isImageSelected) {
                firstVisibleFound = true;
                viewButtons[i].setOnClickListener(buttonSelectedClickListener);
                viewButtons[i].setVisibility(View.VISIBLE);
                viewButtons[i].setTag(DEFAULT_IMAGE);
                String imageKey = getCategoryButtonName(buttonName, AUDIO_TYPE);
                boolean isAudioSelected = prefs.getString(imageKey, null) != null;
                if (isAudioSelected) {
                    viewButtons[i].setTag(DEFAULT_IMAGE_AUDIO);
                }
            }
            else {
                if (firstVisibleFound) {
                    viewButtons[i].setVisibility(View.VISIBLE);
                }
                viewButtons[i].setOnClickListener(findImageClickListener);
            }
            if (imageLocation != null) {
                int maxButtonSide = getMinimumButtonDimension();
                Log.d("buildButton", "before setScaled for loop " + i +  " at " + (new Date()).toString());
                viewButtons[i].setImageBitmap(
                        ImageUtil.getScaledBitmap(imageUri, maxButtonSide));
                viewButtons[i].setBackgroundColor(Color.TRANSPARENT);
                registerForContextMenu(viewButtons[i]);
            }
        }
    }

    private void initButtonArray() {
        viewButtons[0] = (ImageButton) findViewById(R.id.categoryButton0);
        viewButtons[0].setTag(DEFAULT);
        viewButtons[1] = (ImageButton) findViewById(R.id.categoryButton1);
        viewButtons[1].setTag(DEFAULT);
        viewButtons[2] = (ImageButton) findViewById(R.id.categoryButton2);
        viewButtons[2].setTag(DEFAULT);
        viewButtons[3] = (ImageButton) findViewById(R.id.categoryButton3);
        viewButtons[3].setTag(DEFAULT);
        viewButtons[4] = (ImageButton) findViewById(R.id.categoryButton4);
        viewButtons[4].setTag(DEFAULT);
        viewButtons[5] = (ImageButton) findViewById(R.id.categoryButton5);
        viewButtons[5].setTag(DEFAULT);
        initButtonVisibility();
        buttonIndicators[0] = (ImageView) findViewById(R.id.categoryButton0_indicator);
        buttonIndicators[1] = (ImageView) findViewById(R.id.categoryButton1_indicator);
        buttonIndicators[2] = (ImageView) findViewById(R.id.categoryButton2_indicator);
        buttonIndicators[3] = (ImageView) findViewById(R.id.categoryButton3_indicator);
        buttonIndicators[4] = (ImageView) findViewById(R.id.categoryButton4_indicator);
        buttonIndicators[5] = (ImageView) findViewById(R.id.categoryButton5_indicator);
    }


    private void initButtonVisibility() {
        int numInitialButtons = prefs.getInt("pref_num_button_default", 2);
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

    public void findImage(View v) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        setAsCurrentButton((ImageButton) v, IMAGE_TYPE);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, SELECT_PHOTO);
    }

    private void findSound(View view) {
        Intent soundPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        soundPickerIntent.setType("audio/*");
        setAsCurrentButton((ImageButton) view, AUDIO_TYPE);
        startActivityForResult(soundPickerIntent, SELECT_AUDIO);
    }

    private void playSound(View view) {
        final ImageButton playingButton = (ImageButton) view;
        String key = getCategoryButtonName(res.getResourceName(view.getId()), AUDIO_TYPE);
        final String buttonSoundLocation = prefs.getString(key, null);
        if (buttonSoundLocation != null) {
            Uri buttonSoundUri = Uri.parse(buttonSoundLocation);
            playAudioForButton(buttonSoundUri, playingButton);
        }
    }

    public void buttonSelected(View view) {
        Intent intent = new Intent(this, ShowSelection.class);
        String imageKey =getCategoryButtonName(res.getResourceName(view.getId()), IMAGE_TYPE);
        String audioKey =getCategoryButtonName(res.getResourceName(view.getId()), AUDIO_TYPE);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode){
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = returnedIntent.getData();
                    int maxButtonSide = getButtonDimension(2,1);
                    Bitmap selectedImage = ImageUtil.getScaledBitmap(imageUri, maxButtonSide);
                    if (selectedImage != null) {
                        File internalFile = ImageUtil.saveBitmapToInternalStorage(
                                getCategoryButtonName(CURRENT_BUTTON_NAME),
                                selectedImage,
                                this);
                        if (internalFile != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(
                                    getCategoryButtonName(CURRENT_BUTTON_ABSOLUTE_NAME),
                                    internalFile.toString());
                            editor.commit();

                            CURRENT_BUTTON.setImageBitmap(selectedImage);
                            CURRENT_BUTTON.setBackgroundColor(Color.TRANSPARENT);
                            CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                            addButtonAttribute(CURRENT_BUTTON, SELECT_PHOTO);
                            registerForContextMenu(CURRENT_BUTTON);
                        }
                    }
                }
                break;
            case SELECT_AUDIO:
                if (resultCode == RESULT_OK) {
                    final Uri audioUri = returnedIntent.getData();
                    File buttonAudioFile = AudioUtil.saveAudioToInternalStorage(
                            getCategoryButtonName(CURRENT_BUTTON_NAME),
                            audioUri,
                            this);
                    if (buttonAudioFile.exists()) {
                        playAudioForButton(audioUri, CURRENT_BUTTON);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(CURRENT_BUTTON_ABSOLUTE_NAME, buttonAudioFile.toString());
                        editor.commit();

                        CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                        addButtonAttribute(CURRENT_BUTTON, SELECT_AUDIO);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void playingIndicator(ImageButton button, boolean on) {
        String buttonName = res.getResourceEntryName(button.getId());
        int id = res.getIdentifier(buttonName + "_indicator", "id", this.getPackageName());
        ImageView indicator = (ImageView)findViewById(id);
        if (on) {
            indicator.setBackgroundResource(R.drawable.play_animation);
            AnimationDrawable background = (AnimationDrawable) indicator.getBackground();
            background.start();
        }
        else {
            indicator.setBackgroundResource(0);
        }
    }

    private void recordingIndicator(ImageButton button, boolean on) {
        ImageView indicator = buttonIndicators[getButtonIndex(button.getId())];
        if (on) {
            button.setAlpha(0.5f);
            indicator.setBackgroundResource(R.drawable.rec_animation);
            AnimationDrawable background = (AnimationDrawable) indicator.getBackground();
            background.start();
        }
        else {
            button.setAlpha(1f);
            indicator.setBackgroundResource(0);
        }
    }

/*****
     * Context Menu handling
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (preparedForAnotherEvent()) {
            switch ((int) v.getTag()) {
                case DEFAULT:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_record_sound);
                    menu.add(1, R.id.preview_sound, 4, R.string.menu_title_preview_sound);
                    menu.add(1, R.id.up_vote_action, 5, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 6, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 7, R.string.menu_title_remove_button);
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(false);
                    menu.getItem(3).setVisible(false);
                    break;
                case DEFAULT_IMAGE:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_add_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_record_sound);
                    menu.add(1, R.id.preview_sound, 4, R.string.menu_title_preview_sound);
                    menu.add(1, R.id.up_vote_action, 5, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 6, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 7, R.string.menu_title_remove_button);
                    menu.getItem(3).setVisible(false);
                    break;
                case DEFAULT_AUDIO:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_add_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_replace_recording);
                    menu.add(1, R.id.preview_sound, 4, R.string.menu_title_preview_sound);
                    menu.add(1, R.id.up_vote_action, 5, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 6, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 7, R.string.menu_title_remove_button);
                    break;
                case DEFAULT_IMAGE_AUDIO:
                    menu.add(1, R.id.image_action, 1, R.string.menu_title_replace_image);
                    menu.add(1, R.id.sound_action, 2, R.string.menu_title_replace_sound);
                    menu.add(1, R.id.record_action, 3, R.string.menu_title_replace_recording);
                    menu.add(1, R.id.preview_sound, 4, R.string.menu_title_preview_sound);
                    menu.add(1, R.id.up_vote_action, 5, R.string.menu_title_up_vote);
                    menu.add(1, R.id.down_vote_action, 6, R.string.menu_title_down_vote);
                    menu.add(1, R.id.remove_button_action, 7, R.string.menu_title_remove_button);
                    break;
                default:
                    break;
            }
            CURRENT_BUTTON_ID = v.getId();

            int viewButtonIndex = getButtonIndex(CURRENT_BUTTON_ID);
            menu.getItem(5).setEnabled(viewButtonIndex < getLastVisibleIndex());    //not upper bounds
            menu.getItem(4).setEnabled(viewButtonIndex > 0);                        //not lower bounds
            menu.getItem(6).setEnabled(getLastVisibleIndex() > 1);                  //not lower bounds
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        ImageButton activeButton = (ImageButton) findViewById(CURRENT_BUTTON_ID);
        switch (item.getItemId()) {
            case R.id.image_action:
                manageImageState(activeButton);
                return true;
            case R.id.sound_action:
                manageSoundState(activeButton);
                return true;
            case R.id.record_action:
                manageRecording(activeButton);
                return true;
            case R.id.preview_sound:
                playSound(activeButton);
                return true;
            case R.id.remove_button_action:
                manageButtonRemoval(activeButton);
                return true;
            case R.id.up_vote_action:
                upVote(activeButton);
                return true;
            case R.id.down_vote_action:
                downVote(activeButton);
                return true;
            default:
                return true;
        }
    }

/***** END: Context Menu handling */

/*****
     * Menu Actions
     */
    private void downVote(ImageButton selectedButton) {
        for (int i=0; i<getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i + 1);
                break;
            }
        }
    }

    private void upVote(ImageButton selectedButton) {
        for (int i=0; i<=getLastVisibleIndex(); i++) {
            if (selectedButton == viewButtons[i]) {
                exchangeButtonContent(i, i - 1);
                break;
            }
        }
    }

    private void manageSoundState(ImageButton button) {
        findSound(button);
    }

    private void manageImageState(ImageButton button) {
        findImage(button);
    }

    private void manageButtonRemoval(ImageButton button) {
        boolean matched = false;
        int buttonID = button.getId();
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getId() == buttonID) {
                matched = true;
                deleteFilesAssociatedWithButton(buttonID);
                replaceButtonContent(i, i+1);
            }
            else if (matched && viewButtons[i].getVisibility() != View.GONE) {
                replaceButtonContent(i, i+1);
            }
        }
        redrawButtons();
    }

    public void addButton(View view) {
        Log.d("CA", "addButton started");
        if (preparedForAnotherEvent()) {
            for (int i = 0; i < viewButtons.length; i++) {
                if (viewButtons[i].getVisibility() == View.GONE) {
                    enableButton(viewButtons[i]);
                    setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
                    redrawButtons();
                    break;
                }
            }
        }
        Log.d("CA", "addButton end");
    }

    private void manageRecording(ImageButton activeButton) {
        setAsCurrentButton(activeButton, AUDIO_TYPE);
        recordingIndicator(activeButton, true);

        CURRENT_BUTTON_OUTPUT_FILE = this.getFilesDir() +
                "/" + getCategoryButtonName(CURRENT_BUTTON_NAME) + DateUtil.getDateString();
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(CURRENT_BUTTON_OUTPUT_FILE);
        myAudioRecorder.setMaxDuration(10000);

        try {
            myAudioRecorder.prepare();
            CURRENT_BUTTON.setOnClickListener(recordingCompleteListener);
            myAudioRecorder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

/***** END: Menu Actions */

/*****
     * Audio Utils
     */
    private void playAudioForButton(Uri audioUri, ImageButton activeButton) {
        if (aMediaPlayer != null) {
            aMediaPlayer.reset();
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
        try {
            aMediaPlayer = new MediaPlayer();
            aMediaPlayer.setDataSource(this, audioUri);
            aMediaPlayer.setOnPreparedListener(aPreparedListener);
            aMediaPlayer.setLooping(false);
            aMediaPlayer.setOnCompletionListener(aCompletionListener);
            playingIndicator(activeButton, true);
            setAsCurrentButton(activeButton, AUDIO_TYPE);
            aMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/***** END: Audio Utils */

/*****
     * Button Utils
     */
    private int getButtonDimension(int numColumns, int numRows) {
        int width = container.getWidth();
        int height = container.getHeight();
        int vFreeSpace = getVerticalFreeSpace(height, numRows);
        int hFreeSpace = getHorizontalFreeSpace(width, numColumns);
        return Math.min(vFreeSpace / numRows, hFreeSpace / numColumns);
    }

    public int getLastVisibleIndex() {
        for (int i = viewButtons.length - 1; i >= 0; i--) {
            if (viewButtons[i].getVisibility() != View.GONE) {
                return i;      //previous index which would have passed the visibility test
            }
        }
        return 0;
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

    public void setAsCurrentButton(ImageButton currentButton, String mediaType) {
        CURRENT_BUTTON = currentButton;
        CURRENT_BUTTON_ID = CURRENT_BUTTON.getId();
        CURRENT_BUTTON_ABSOLUTE_NAME = res.getResourceName(CURRENT_BUTTON_ID) + mediaType;
        CURRENT_BUTTON_NAME = "categoryButton" + mediaType;
    }

    private int getButtonIndex(int selected_button_id) {
        int index = 0;
        for (int i=0; i<viewButtons.length; i++) {
            if (viewButtons[i].getId() == selected_button_id) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void addButtonAttribute(ImageButton button, int button_attribute) {
        if (button_attribute == SELECT_PHOTO && !hasImageAttribute(button) ||
                button_attribute == SELECT_AUDIO && !hasAudioAttribute(button)) {
            button.setTag(((int) button.getTag()) + button_attribute);
        }
    }

    private boolean hasAudioAttribute(ImageButton button) {
        return (int)button.getTag() >= DEFAULT_AUDIO;
    }

    private boolean hasImageAttribute(ImageButton button) {
        int tag = (int)button.getTag();
        return tag == DEFAULT_IMAGE_AUDIO || tag == DEFAULT_IMAGE;
    }

    private String getCategoryButtonName(String current_button_name) {
        return categoryUpperCase + "~" + current_button_name;
    }

    private String getCategoryButtonName(String resourceName, String audio_type) {
        return categoryUpperCase + "~" + resourceName + audio_type;
    }

    private void exchangeButtonContent(int index1, int index2) {
        if (index1 >= 0
                && index1 < viewButtons.length
                && index2 >= 0
                && index2 < viewButtons.length) {
            int tmpVis = viewButtons[index1].getVisibility();
            int tmpTag = (int)viewButtons[index1].getTag();
            Bitmap tmpImg = ((BitmapDrawable)viewButtons[index1].getDrawable()).getBitmap();
            viewButtons[index1].setOnClickListener(determineClickListener((int)viewButtons[index2].getTag()));
            viewButtons[index1].setVisibility(viewButtons[index2].getVisibility());
            viewButtons[index1].setTag(viewButtons[index2].getTag());
            viewButtons[index1].setImageBitmap(((BitmapDrawable) viewButtons[index2].getDrawable()).getBitmap());
            viewButtons[index2].setOnClickListener(determineClickListener(tmpTag));
            viewButtons[index2].setVisibility(tmpVis);
            viewButtons[index2].setTag(tmpTag);
            viewButtons[index2].setImageBitmap(tmpImg);
            exchangeFileNames(viewButtons[index1], viewButtons[index2]);
        }
    }

    private void exchangeFileNames(ImageButton button1, ImageButton button2) {
        int button1Id = button1.getId();
        int button2Id = button2.getId();
        String absName1 = res.getResourceName(button1Id);
        String absName2 = res.getResourceName(button2Id);

        // get existing keys
        String imageKey1 = getCategoryButtonName(absName1, IMAGE_TYPE);
        String imageKey2 = getCategoryButtonName(absName2, IMAGE_TYPE);
        String audioKey1 = getCategoryButtonName(absName1, AUDIO_TYPE);
        String audioKey2 = getCategoryButtonName(absName2, AUDIO_TYPE);

        // get existing values
        String imageValue1 = prefs.getString(imageKey1, null);
        String imageValue2 = prefs.getString(imageKey2, null);
        String audioValue1 = prefs.getString(audioKey1, null);
        String audioValue2 = prefs.getString(audioKey2, null);

        // remove all keys from SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(imageKey1);
        editor.remove(imageKey2);
        editor.remove(audioKey1);
        editor.remove(audioKey2);

        // switch key value pairs if they existed
        if (imageValue1 != null) {
            editor.putString(imageKey2, imageValue1);
        }
        if (imageValue2 != null) {
            editor.putString(imageKey1, imageValue2);
        }
        if (audioValue1 != null) {
            editor.putString(audioKey2, audioValue1);
        }
        if (audioValue2 != null) {
            editor.putString(audioKey1, audioValue2);
        }

        editor.commit();
    }

    private void replaceFileNames(ImageButton toButton, ImageButton fromButton) {
        SharedPreferences.Editor editor = prefs.edit();
        String toAudioKey = getCategoryButtonName(res.getResourceName(toButton.getId()), AUDIO_TYPE);
        String toImageKey = getCategoryButtonName(res.getResourceName(toButton.getId()), IMAGE_TYPE);
        String fromAudioKey = getCategoryButtonName(res.getResourceName(fromButton.getId()), AUDIO_TYPE);
        String fromImageKey = getCategoryButtonName(res.getResourceName(fromButton.getId()), IMAGE_TYPE);
        String fromAudioValue = prefs.getString(fromAudioKey, null);
        String fromImageValue = prefs.getString(fromImageKey, null);
        if (fromAudioValue != null) {
            editor.putString(toAudioKey, fromAudioValue);
        }
        else {
            editor.remove(toAudioKey);
        }
        if (fromImageValue != null) {
            editor.putString(toImageKey, fromImageValue);
        }
        else {
            editor.remove(toImageKey);
        }
        editor.remove(fromAudioKey);
        editor.remove(fromImageKey);
        editor.commit();

    }
    private void replaceButtonContent(int toIndex, int fromIndex) {
        if (fromIndex < viewButtons.length) {
            int fromIndexTag = (int)viewButtons[fromIndex].getTag();
            viewButtons[toIndex].setOnClickListener(determineClickListener(fromIndexTag));
            viewButtons[toIndex].setTag(fromIndexTag);
            viewButtons[toIndex].setImageBitmap(((BitmapDrawable) viewButtons[fromIndex].getDrawable()).getBitmap());
            viewButtons[toIndex].setVisibility(viewButtons[fromIndex].getVisibility());
            replaceFileNames(viewButtons[toIndex], viewButtons[fromIndex]);
        }
        else if (toIndex >= 0 && toIndex < viewButtons.length) {
            disableButton(viewButtons[toIndex]);
        }
        setButtonEnablePlusUI((Button) findViewById(R.id.addButton), !buttonSetFull());
    }

    private void enableButton(ImageButton button) {
        button.setTag(DEFAULT);
        button.setVisibility(View.VISIBLE);
    }

    private void disableButton(ImageButton button) {
        button.setTag(DEFAULT);
        button.setVisibility(View.GONE);
    }

    private boolean buttonSetFull() {
        return viewButtons[viewButtons.length-1].getVisibility() != View.GONE;
    }

    private void setButtonEnablePlusUI(Button button, boolean enabled) {
        button.setEnabled(enabled);
        if (!enabled) {
            button.setTextColor(Color.GRAY);
        }
        else {
            button.setTextColor(Color.WHITE);
        }
    }

    /***** END: Button Utils */

    ImageButton.OnClickListener findImageClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (preparedForAnotherEvent()) {
                findImage(v);
            }
        }
    };

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
            if (preparedForAnotherEvent()) {
                buttonSelected(v);
            }
        }
    };

    private ImageButton.OnClickListener determineClickListener(int tmpTag) {
        if (tmpTag == DEFAULT) {
            return findImageClickListener;
        }
        else {
            return buttonSelectedClickListener;
        }
    }

    MediaPlayer.OnPreparedListener aPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            aMediaPlayer.start();
        }
    };

    MediaPlayer.OnCompletionListener aCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            playingIndicator(CURRENT_BUTTON, false);
            if (aMediaPlayer != null) {
                CURRENT_BUTTON = null;
                aMediaPlayer.release();
                aMediaPlayer = null;
            }
        }
    };

    ImageButton.OnClickListener recordingCompleteListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myAudioRecorder != null) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
                recordingIndicator((ImageButton) v, false);
                File newRecording = new File(CURRENT_BUTTON_OUTPUT_FILE);
                if (newRecording != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(
                            getCategoryButtonName(CURRENT_BUTTON_ABSOLUTE_NAME),
                            newRecording.toString());
                    editor.commit();
                    addButtonAttribute(CURRENT_BUTTON, SELECT_AUDIO);
                    CURRENT_BUTTON.setOnClickListener(buttonSelectedClickListener);
                }
            }
        }
    };

    public boolean preparedForAnotherEvent() {
        for (int i=0; i<buttonIndicators.length; i++) {
            viewButtons[i].setAlpha(1f);
            viewButtons[i].setOnClickListener(determineClickListener((int)viewButtons[i].getTag()));
            buttonIndicators[i].setBackgroundResource(0);
        }
        if (myAudioRecorder != null) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
        }
        if (aMediaPlayer != null) {
            aMediaPlayer.release();
            aMediaPlayer = null;
        }
        return true;
    }

    private void deleteFilesAssociatedWithButton(int buttonID) {
        String imageKey = getCategoryButtonName(res.getResourceName(buttonID), IMAGE_TYPE);
        String audioKey = getCategoryButtonName(res.getResourceName(buttonID), AUDIO_TYPE);
        String imageFileName = prefs.getString(imageKey, null);
        String audioFileName = prefs.getString(audioKey, null);
        if (imageFileName != null) {
            try {
                File imageFile = new File(imageFileName);
                imageFile.delete();
            }
            catch (Exception e) {
                // didn't exist keep going
            }
        }
        if (audioFileName != null) {
            try {
                File audioFile = new File(audioFileName);
                audioFile.delete();
            }
            catch (Exception e) {
                // didn't exist keep going
            }
        }
    }

    private void setDisplayDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DISPLAY_WIDTH = size.y;
        DISPLAY_HEIGHT = size.x;
    }

    private int getMinimumButtonDimension() {
        int numInitialButtons = prefs.getInt("pref_num_button_default", 2);
        return Math.min(DISPLAY_HEIGHT / numInitialButtons, DISPLAY_WIDTH / numInitialButtons);
    }
}
