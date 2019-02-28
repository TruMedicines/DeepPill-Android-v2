package truemedicine.logiticks.com.trumedicine.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import volley.VolleyError;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.utils.EmailValidator;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener, NetworkManager.OnNetWorkListener {

    private EditText mFirstNameEditText = null;
    private EditText mLastNameEditText = null;
    private EditText mEmailEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mConfirmPasswordEditText = null;

    String firstName;
    String lastName;
    String email;
    String password;
    String confirm;

    private NetworkManager networkManager;
    private SweetAlertDialog pDialog;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    // note that these credentials will differ between live & sandbox environments.


    private static final int REQUEST_CODE_PAYMENT = 1;

    /***
     * PayPal configurations
     * set paypal enviornment here
     * note:-> the credientials set here is default values provided by the paypal sdk
     * values have to be varied according to clients paypal account credientials
     */

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(AppConstants.CONFIG_CLIENT_ID)

            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName(AppConstants.MERCHANT_NAME)
            .merchantPrivacyPolicyUri(Uri.parse(AppConstants.MERCHANT_PRIVACY_POLICY_URL))
            .merchantUserAgreementUri(Uri.parse(AppConstants.MERCHANT_AGREEMENT_POLICY_URL));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Register");
        setSupportActionBar(toolbar);


        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout));

        setUI();
        networkManager = NetworkManager.getInstance(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void proceedToPayment() {
        PayPalPayment thingToBuy = getThingToBuy();
        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */
        Intent intent = new Intent(SignupActivity.this, PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }


    /************
     * > setting pro app prize
     **********************/

    private PayPalPayment getThingToBuy() {
        return new PayPalPayment(new BigDecimal("3.99"), "USD", "TruMedicine Pro version",
                PayPalPayment.PAYMENT_INTENT_SALE);
    }


    private void setUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        mLastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        mEmailEditText = (EditText) findViewById(R.id.usernameEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        Button mSignupButton = (Button) findViewById(R.id.signUpButton);
        mSignupButton.setOnClickListener(this);

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
        if (AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout))) {
            firstName = mFirstNameEditText.getText().toString();
            lastName = mLastNameEditText.getText().toString();
            email = mEmailEditText.getText().toString();
            password = mPasswordEditText.getText().toString();
            confirm = mConfirmPasswordEditText.getText().toString();
            if (firstName.equalsIgnoreCase("")) {
                 mFirstNameEditText.setError("Enter your First name");
            } else if (lastName.equalsIgnoreCase("")) {
                 mLastNameEditText.setError("Enter your Last name");
            } else if (email.equalsIgnoreCase("")) {
                 mEmailEditText.setError("Enter Email");
            } else if (!EmailValidator.isValidEmail(email)) {
                 mEmailEditText.setError("Enter Valid Email");
            } else if (password.equalsIgnoreCase("")) {
                 mPasswordEditText.setError("Enter Password");
            } else if (confirm.equalsIgnoreCase("")) {
                 mConfirmPasswordEditText.setError("Enter Confirm Password");
            } else if (!confirm.equals(password)) {
                 mConfirmPasswordEditText.setError("Password does not match");
            } else {
//                if (AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.PAYMENT_STATUS_KEY)) {
                    doSignUp();
//                } else {
//                    proceedToPayment();
//                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.PAYMENT_STATUS_KEY, true);
                        if (AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout))) {
                            doSignUp();
                        }
                        Log.i("tag", confirm.toJSONObject().toString(4));
                        Log.i("tag", confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        /*new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success!")
                                .setContentText(getResources().getString(R.string.payment_successful)).setConfirmText("Download Pro App").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                try {
                                    Intent viewIntent =
                                            new Intent("android.intent.action.VIEW",
                                                    Uri.parse("https://play.google.com/store/apps/"));
                                    startActivity(viewIntent);
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_to_connect),
                                            Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                                finish();

                            }
                        }).show();*/

                    } catch (JSONException e) {
                        Log.e("tag", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(SignupActivity.this, "Payment cancelled!", Toast.LENGTH_SHORT).show();
           /*     new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.oops))
                        .setContentText(getResources().getString(R.string.user_cancelled))
                        .show();*/
                Log.i("tag", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.oops))
                        .setContentText(getResources().getString(R.string.invalid_payment))
                        .show();
                Log.i(
                        "tag",
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    private void doSignUp() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Registering...");
        pDialog.setCancelable(false);
        pDialog.show();
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(AppConstants.SIGNUP_KEY_FIRST_NAME, firstName);
            jsonObject.put(AppConstants.SIGNUP_KEY_LAST_NAME, lastName);
            jsonObject.put(AppConstants.SIGNUP_KEY_EMAIL, email);
            jsonObject.put(AppConstants.SIGNUP_KEY_PASSWORD, password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.SIGN_UP_URL, jsonObject, Urls.SIGN_UP_URL_TAG, false);

    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {

        {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setTitleText("Error!")
                    .showCancelButton(false)
                    .setContentText(AppUtils.getNetworkError(error))
                    .setConfirmText("Ok");
        }


    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Success!")
                .setContentText("Registration completed.\nPlease login with your credentials")
                .showCancelButton(false)
                .setConfirmText("Ok").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.dismiss();
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}
