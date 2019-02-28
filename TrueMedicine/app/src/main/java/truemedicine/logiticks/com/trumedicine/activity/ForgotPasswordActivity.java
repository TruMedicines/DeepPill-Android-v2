package truemedicine.logiticks.com.trumedicine.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.utils.EmailValidator;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener, NetworkManager.OnNetWorkListener {

    private EditText mEmailEditText = null;
    private Button mResetPasswordButton;
    private NetworkManager networkManager;
    private String email;
    private SweetAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Reset Password");
        setSupportActionBar(toolbar);

        setUI();
        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
    }


    private void setUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mEmailEditText = (EditText) findViewById(R.id.usernameEditText);
        mResetPasswordButton = (Button) findViewById(R.id.resetPasswordButton);
        mResetPasswordButton.setOnClickListener(this);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetPasswordButton: {
                if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                    email = mEmailEditText.getText().toString();
                    if (email.equalsIgnoreCase("")) {
                        mEmailEditText.setError("Enter Email");
                    } else if (!EmailValidator.isValidEmail(email)) {
                        mEmailEditText.setError("Enter Valid Email");
                    } else {
                        resetPassword();

                    }
                }
            }
            break;

        }
    }

    private void resetPassword() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Resetting password...");
        pDialog.setCancelable(false);
        pDialog.show();
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.RESET_PASSWORD_URL, jsonObject, Urls.RESET_PASSWORD_URL_TAG,false);


    }


    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setTitleText("Error!")
                    .setContentText(AppUtils.getNetworkError(error))
                    .setConfirmText("Ok");
        }

    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Success!")
                .setContentText("New password is sent to your Email!")
                .setConfirmText("Ok").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.dismiss();
                Intent intent = new Intent(ForgotPasswordActivity.this, SigninActivity.class);
                startActivity(intent);
                finish();
            }
        })
        ;

    }
}
