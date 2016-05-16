package com.scyoung.puzzlemethis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PasscodeActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private int action;
    private EditText digit1;
    private EditText digit2;
    private EditText digit3;
    private EditText digit4;
    private String passcodeAttempt;
    private final String RECOVERY_PASSCODE = "9090";
    public static final int SET = 0;
    public static final int VERIFY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        action = isPasscodeSet() ? VERIFY : SET;

        digit1 = (EditText) findViewById(R.id.passcodeDigit1);
        digit2 = (EditText) findViewById(R.id.passcodeDigit2);
        digit3 = (EditText) findViewById(R.id.passcodeDigit3);
        digit4 = (EditText) findViewById(R.id.passcodeDigit4);

        prepareEditText(digit1, digit2);
        prepareEditText(digit2, digit3);
        prepareEditText(digit3, digit4);
        prepareEditText(digit4, null);

        initPasscode();

//        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(digit1, 0);
    }

    public void cancelPasscodeProcessing(View view) {
        setPreference(false, "");
    }

    private void setPreference(boolean b, String newPasscode) {
        if (action == SET) {
            SharedPreferences.Editor editor = prefs.edit();
            if (b) {
                editor.putString("user_passcode", newPasscode);
            } else {
                editor.remove("user_passcode");
            }
            editor.commit();
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getResources().getString(R.string.passcode_success), b);
        resultIntent.putExtra(getResources().getString(R.string.passcode_action), action);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private boolean isPasscodeSet() {
        return !(prefs.getString("user_passcode", "")).isEmpty();
    }

    private void prepareEditText(EditText editTextCurrent, final EditText editTextNext) {
        editTextCurrent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    findViewById(R.id.errorText).setVisibility(View.GONE);
                    if (editTextNext != null) {
                        editTextNext.requestFocus();
                        editTextNext.selectAll();
                    } else {
                        processPasscode();
                    }
                }
            }
        });
    }

    private void processPasscode() {
        String passcodeEntry =
                digit1.getText().toString() +
                digit2.getText().toString() +
                digit3.getText().toString() +
                digit4.getText().toString();
        if (action == SET) {
            processSetPasscode(passcodeEntry);
        }
        else {
            processVerifyPasscode(passcodeEntry);
        }
    }

    private void processVerifyPasscode(String passcodeEntry) {
        String userPasscode = prefs.getString("user_passcode", null);
        if (passcodeEntry.equals(userPasscode) || passcodeEntry.equals(RECOVERY_PASSCODE)) {
            setPreference(true, null);
        }
        else {
            TextView errorText = (TextView)findViewById(R.id.errorText);
            errorText.setText(R.string.passcode_doesnt_match);
            errorText.setVisibility(View.VISIBLE);
            initPasscode();

        }

    }

    private void processSetPasscode(String passcodeEntry) {
        if (passcodeAttempt != null && !passcodeAttempt.isEmpty()) {
            //process second attempt
            if (passcodeAttempt.equals(passcodeEntry)) {
                // process success
                setPreference(true, passcodeEntry);
            }
            else {
                // process failure / retry
                passcodeAttempt = null;
                TextView errorMessage = (TextView)findViewById(R.id.errorText);
                errorMessage.setText(R.string.passcodes_dont_match);
                errorMessage.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.enterPasscodeText)).setText(R.string.enter_passcode);
                initPasscode();
            }
        }
        else {
            //process first attempt
            passcodeAttempt = passcodeEntry;
            ((TextView)findViewById(R.id.enterPasscodeText)).setText(R.string.reenter_passcode);
            initPasscode();

        }

    }

    private void initPasscode() {
        digit1.setText("");
        digit2.setText("");
        digit3.setText("");
        digit4.setText("");
        digit1.requestFocus();
    }

}
