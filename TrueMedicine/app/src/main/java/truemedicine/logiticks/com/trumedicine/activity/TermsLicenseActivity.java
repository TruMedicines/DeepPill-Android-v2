package truemedicine.logiticks.com.trumedicine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import truemedicine.logiticks.com.trumedicine.BuildConfig;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

public class TermsLicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("About");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button upgradeButton = (Button) findViewById(R.id.upgradeButtonTwo);
        if (AppUtils.getStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_USERNAME).contentEquals("")) {
            upgradeButton.setVisibility(View.VISIBLE);
        } else {
            upgradeButton.setVisibility(View.GONE);
        }


        upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsLicenseActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView vesrion = (TextView) findViewById(R.id.version);
        vesrion.setText("Version: " + BuildConfig.VERSION_NAME);
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
