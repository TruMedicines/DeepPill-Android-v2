package truemedicine.logiticks.com.trumedicine.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.db.DBManager;
import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.model.LoginResponseModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.utils.EmailValidator;

public class SigninActivity extends AppCompatActivity implements View.OnClickListener, NetworkManager.OnNetWorkListener {

    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private TextView mForgotPasswordTextView = null;
    private Button mSignUpButton = null;
    private Button mTryAppButton = null;
    private Button mSignInButton;
    private CheckBox mRememberMeCheckBox;

    private SharedPreferences preferences;
    private NetworkManager networkManager;
    private String email;
    private String password;
    private SweetAlertDialog pDialog;
    private DBManager dbManager;
    private int count;//variable keep the position of image in the "images" array.
    private ArrayList<ImageListModel> images;
    private boolean mRememberMe = false;
    private String mTextToSpeechStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);

        setUI();
        checkTextToSpeechStatus();
        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
    }

    private void checkTextToSpeechStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SigninActivity.this);
        mTextToSpeechStatus = preferences.getString(AppConstants.TEXT_TO_SPEECH_STATUS, "");
        Log.d("preference", "mtextToSpeech value " + mTextToSpeechStatus);
        //SharedPreferences.Editor editor = preferences.edit();
        if (mTextToSpeechStatus.contentEquals("")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(AppConstants.TEXT_TO_SPEECH_STATUS, "true");
            editor.commit();
        }
    }

    private void setUI() {
        mUsernameEditText = (EditText) findViewById(R.id.usernameEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password);
        mTryAppButton = (Button) findViewById(R.id.try_app);
        mSignUpButton = (Button) findViewById(R.id.sign_up);
        mRememberMeCheckBox = (CheckBox) findViewById(R.id.remember);

        mSignInButton = (Button) findViewById(R.id.signinButton);
        mForgotPasswordTextView.setOnClickListener(this);
        mTryAppButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);
        mRememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRememberMe = isChecked;
            }
        });
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signinButton: {
                if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                    email = mUsernameEditText.getText().toString();
                    password = mPasswordEditText.getText().toString();
                    if (email.equalsIgnoreCase("")) {
                        mUsernameEditText.setError("Enter Email");
                    } else if (!EmailValidator.isValidEmail(email)) {
                        mUsernameEditText.setError("Enter Valid Email");
                    } else if (password.equalsIgnoreCase("")) {
                        mPasswordEditText.setError("Enter Password");
                    } else {
                        preferences = PreferenceManager.getDefaultSharedPreferences(SigninActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.apply();
                        signIn();

                    }
                }
            }
            break;
            case R.id.forgot_password: {
                startActivity(new Intent(SigninActivity.this, ForgotPasswordActivity.class));

            }
            break;
            case R.id.try_app: {
                AppUtils.cleardata(getApplicationContext());
                startActivity(new Intent(SigninActivity.this, MainActivity.class));
            }
            break;
            case R.id.sign_up: {
                startActivity(new Intent(SigninActivity.this, SignupActivity.class));
            }
            break;

        }
    }

    private void signIn() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Signing in..");
        pDialog.setCancelable(false);
        pDialog.show();
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("grant_type", "password");
        jsonObject.addProperty("username", email);
        jsonObject.addProperty("password", password);

        networkManager.postUrlencodedRequest(NetworkOptions.POST_REQUEST, Urls.LOGIN_URL, AppUtils.jsonToUrlEncodedString(jsonObject, ""), Urls.LOGIN_URL_TAG);

    }


    @Override
    public void onErrorResponse(VolleyError error, int requestId) {

        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Sign in error!")
                .setContentText(AppUtils.getNetworkError(error))
                .setConfirmText("Ok")
        ;
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        if (requestId == Urls.LOGIN_URL_TAG) {
            Gson gson = new Gson();
            LoginResponseModel loginResponseModel = gson.fromJson(object.toString(), LoginResponseModel.class);
            AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_REMEMBER_ME, mRememberMe);
            AppUtils.setStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_USERNAME, email);
            AppUtils.setStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_PASSWORD, password);
            AppUtils.setStringSharedPreference(getApplicationContext(), AppConstants.KEY_ACCESS_TOKEN, loginResponseModel.accessToken);
            AppUtils.setStringSharedPreference(getApplicationContext(), AppConstants.KEY_TOKEN_EXPIRES, loginResponseModel.expires);
            pDialog.dismiss();

            startActivity(new Intent(SigninActivity.this, MainActivity.class));
            if (mRememberMe) {
                finish();
            }
        }
    }
}
