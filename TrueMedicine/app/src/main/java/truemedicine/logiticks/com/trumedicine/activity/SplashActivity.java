package truemedicine.logiticks.com.trumedicine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
    private String mTextToSpeechStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();

            }
        }, AUTO_HIDE_DELAY_MILLIS);
    }



}
