package truemedicine.logiticks.com.trumedicine.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;


public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences = null;
    private String mTextToSpeechStatus = null;
    private boolean mCropEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        checkTextToSpeechStatus();

        setUI();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Switch mTxtEnableSwitch = (Switch) findViewById(R.id.default_switch);
        Switch mCropEnableSwitch = (Switch) findViewById(R.id.crop_switch);
        if(AppUtils.getStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_USERNAME).contentEquals(""))
        {
            ((LinearLayout) mCropEnableSwitch.getParent()).setVisibility(View.GONE);
        }
        mCropEnableSwitch.setChecked(AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_CROP_ENABLE));
        if (mTextToSpeechStatus.equalsIgnoreCase("true")) {
            mTxtEnableSwitch.setChecked(true);
        } else {
            mTxtEnableSwitch.setChecked(false);
        }
        mTxtEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                //SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(AppConstants.TEXT_TO_SPEECH_STATUS, "true");

                    editor.commit();
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(AppConstants.TEXT_TO_SPEECH_STATUS, "false");

                    editor.commit();
                }

            }
        });
        LinearLayout mTxtToSpeechEnableLinearLayout = (LinearLayout) findViewById(R.id.txt_settings_linear_layout);
        mTxtToSpeechEnableLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });
        mCropEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_CROP_ENABLE, isChecked);
            }
        });
    }

    private void checkTextToSpeechStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        mTextToSpeechStatus = preferences.getString(AppConstants.TEXT_TO_SPEECH_STATUS, "");
        Log.d("preference", "mtextToSpeech value " + mTextToSpeechStatus);
    }


    @Override
    public void onBackPressed() {
        finish();

    }
}
