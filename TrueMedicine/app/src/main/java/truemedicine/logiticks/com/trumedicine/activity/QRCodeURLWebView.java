package truemedicine.logiticks.com.trumedicine.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;

public class QRCodeURLWebView extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Toolbar toolbar;
    private String mUrl = "";
    private String mTextToSpeechStatus = "";
    private static final int MY_DATA_CHECK_CODE = 101;
    private TextToSpeech textToSpeech = null;
    private WebView webView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUrl = getIntent().getStringExtra("url");
        getSupportActionBar().setTitle(mUrl);
        checkTextToSpeechStatus();
        webView = (WebView) findViewById(R.id.webView);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /* An instance of this class will be registered as a JavaScript interface */
        class MyJavaScriptInterface {


            public MyJavaScriptInterface() {
            }

            @SuppressWarnings("unused")
            @JavascriptInterface
            public void processContent(String aContent) {
                final String content = aContent;

                String[] split = split(content,4000);
                for (String s : split) {
                    play(s);
                }

            }
        }

        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new MyJavaScriptInterface(), "INTERFACE");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
            }
        });

        webView.loadUrl(mUrl);
    }
    public static String[] split(String src, int len) {
        String[] result = new String[(int)Math.ceil((double)src.length()/(double)len)];
        for (int i=0; i<result.length; i++)
            result[i] = src.substring(i*len, Math.min(src.length(), (i+1)*len));
        return result;
    }

    private void checkTextToSpeechStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(QRCodeURLWebView.this);
        mTextToSpeechStatus = preferences.getString(AppConstants.TEXT_TO_SPEECH_STATUS, "");
        Log.d("preference", "mTextToSpeech value " + mTextToSpeechStatus);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MY_DATA_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    textToSpeech = new TextToSpeech(QRCodeURLWebView.this, this);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
            }
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void play(String name) {
        if (mTextToSpeechStatus.equalsIgnoreCase(getResources().getString(R.string.trues))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                int maxSpeechInputLength = textToSpeech.getMaxSpeechInputLength();
                Log.d("tts", "inside "+maxSpeechInputLength);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("tts", "inside");
                textToSpeech.speak(name, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                textToSpeech.speak(name, TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            Toast.makeText(getApplicationContext(), "TextToSpeech functionality is disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.US);
        } else {
            Toast.makeText(this, getResources().getString(R.string.text_to_speech_failed), Toast.LENGTH_LONG).show();
        }
    }
}