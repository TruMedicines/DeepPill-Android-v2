package truemedicine.logiticks.com.trumedicine.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.views.CGEditText;
import truemedicine.logiticks.com.trumedicine.views.CGTextViewLight;

public class ProfileActivity extends AppCompatActivity implements NetworkManager.OnNetWorkListener {

    RelativeLayout changePasswordRL, updateUserRL;
    LinearLayout userDetailsRL;
    private NetworkManager networkManager;
    CGTextViewLight nameTV, emailTV, accountExpiryTV;
    CGEditText oldPasswordET, newPasswordET, repeatPasswordET, firstNameET, lastNameET;

    Button changePasswordButton, updateUserButton;
    private int page = -1;
    private SweetAlertDialog pDialog;
    private MenuItem profileChange, passwordChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        initViews();
        initNetwork();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout))) {
            fetchUserDetails();
        }
    }



    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        userDetailsRL = (LinearLayout) findViewById(R.id.user_details_container);
        updateUserRL = (RelativeLayout) findViewById(R.id.update_user_container);
        changePasswordRL = (RelativeLayout) findViewById(R.id.change_password_container);

        nameTV = (CGTextViewLight) findViewById(R.id.user_name_tv);
        emailTV = (CGTextViewLight) findViewById(R.id.email_tv);
        accountExpiryTV = (CGTextViewLight) findViewById(R.id.expiry_date_tv);

        oldPasswordET = (CGEditText) findViewById(R.id.oldPasswordEditText);
        newPasswordET = (CGEditText) findViewById(R.id.newPasswordEditText);
        repeatPasswordET = (CGEditText) findViewById(R.id.repeatPasswordEditText);
        changePasswordButton = (Button) findViewById(R.id.change_password_btn);

        firstNameET = (CGEditText) findViewById(R.id.first_name_update_et);
        lastNameET = (CGEditText) findViewById(R.id.last_name_update_et);
        updateUserButton = (Button) findViewById(R.id.updateUserButton);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPasswordET.getText().toString().equals("") || newPasswordET.getText().toString().equals("") || repeatPasswordET.getText().toString().equals("")) {
                    AppUtils.makeShortToast(ProfileActivity.this, "Please fill all fields!");
                }
                else if(newPasswordET.getText().toString().length()<6)
                {
                    AppUtils.makeShortToast(ProfileActivity.this, "Password must be at least 6 characters.");
                }
                else if (!newPasswordET.getText().toString().equals(repeatPasswordET.getText().toString())) {
                    AppUtils.makeShortToast(ProfileActivity.this, "New Password and Confirm Password doesn't match");
                } else {
                    makeChangePasswordCall(
                            oldPasswordET.getText().toString(),
                            newPasswordET.getText().toString()
                    );
                }
            }
        });

        updateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstNameET.getText().toString().equals("") || lastNameET.getText().toString().equals("")) {
                    AppUtils.makeShortToast(ProfileActivity.this, "Please fill all fields!");
                } else {
                    makeUpdateUserCall(
                            firstNameET.getText().toString(),
                            lastNameET.getText().toString()
                    );
                }
            }
        });
    }

    private void makeUpdateUserCall(String firstName, String lastName) {
        if (AppUtils.hasDataConnectivity(ProfileActivity.this)) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Updating profile information...");
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.show();
            try {
                JSONObject request = new JSONObject();
                request.put("FirstName", firstName);
                request.put("LastName", lastName);
                networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.UPDATE_USER_URL,
                        request, Urls.UPDATE_USER_TAG, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeChangePasswordCall(String currentPassword, String newPassword) {
        if (AppUtils.hasDataConnectivity(ProfileActivity.this)) {

            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Changing password...");
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.show();
            try {
                JSONObject request = new JSONObject();
                request.put("CurrentPassword", currentPassword);
                request.put("NewPassword", newPassword);

                networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.CHANGE_PASSWORD_URL,
                        request, Urls.CHANGE_PASSWORD_TAG, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchUserDetails() {
        if (AppUtils.hasDataConnectivity(ProfileActivity.this)) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Retrieving profile information...");
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.show();
            networkManager.postJsonRequest(NetworkOptions.GET_REQUEST, Urls.GET_ACCOUNT_DETAILS_URL,
                    new JSONObject(), Urls.GET_ACCOUNT_DETAILS_TAG, true);
        }
    }

    private void showSuccessDialog(String content, final String firstName, final String lastName) {
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Success");
        pDialog.setContentText(content);
        pDialog.setConfirmText("OK");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                userDetailsRL.setVisibility(View.VISIBLE);
                changePasswordRL.setVisibility(View.GONE);
                updateUserRL.setVisibility(View.GONE);
                page = -1;
                getSupportActionBar().setTitle("Profile");
                profileChange.setVisible(true);
                passwordChange.setVisible(true);
                if (!firstName.equals("")) {
                    nameTV.setText(firstName + " " + lastName);
                }
            }
        });

    }

    private void showErrorDialog() {
        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Error");
        if (!AppUtils.getStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_PASSWORD).contentEquals(oldPasswordET.getText().toString())) {
            pDialog.setContentText("Old Password entered is wrong!");

        } else {
            pDialog.setContentText("Some error occurred!");
        }
        pDialog.setConfirmText("OK");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        });
    }

    @Override
    protected void onResume() {
//        initNetwork();
        super.onResume();
    }

    private void initNetwork() {
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        profileChange = menu.findItem(R.id.edit_profile_mi);
        passwordChange = menu.findItem(R.id.change_password_mi);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            if (page != -1) {
                profileChange.setVisible(true);
                passwordChange.setVisible(true);
                userDetailsRL.setVisibility(View.VISIBLE);
                changePasswordRL.setVisibility(View.GONE);
                updateUserRL.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Profile");
                page = -1;
                oldPasswordET.setText("");
                newPasswordET.setText("");
                repeatPasswordET.setText("");

            } else {
                finish();
            }
        }
        switch (id) {
            case R.id.edit_profile_mi:
                profileChange.setVisible(false);
                passwordChange.setVisible(true);
                page = 1;
                getSupportActionBar().setTitle("Edit Profile");
                updateUserRL.setVisibility(View.VISIBLE);
                userDetailsRL.setVisibility(View.GONE);
                changePasswordRL.setVisibility(View.GONE);
                break;
            case R.id.change_password_mi:
                profileChange.setVisible(true);
                passwordChange.setVisible(false);
                page = 2;
                getSupportActionBar().setTitle("Change Password");
                changePasswordRL.setVisibility(View.VISIBLE);
                userDetailsRL.setVisibility(View.GONE);
                updateUserRL.setVisibility(View.GONE);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (page != -1) {
            profileChange.setVisible(true);
            passwordChange.setVisible(true);
            getSupportActionBar().setTitle("Profile");
            userDetailsRL.setVisibility(View.VISIBLE);
            changePasswordRL.setVisibility(View.GONE);
            updateUserRL.setVisibility(View.GONE);
            page = -1;
            oldPasswordET.setText("");
            newPasswordET.setText("");
            repeatPasswordET.setText("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        // Log.d("error",error.networkResponse.toString());
        if (requestId == Urls.GET_ACCOUNT_DETAILS_TAG || requestId == Urls.UPDATE_USER_TAG || requestId == Urls.CHANGE_PASSWORD_TAG) {
            Log.e("ERROR", error.networkResponse.data.toString());
            showErrorDialog();
        }
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        Log.e("SUCCESS", object.toString());
        if (requestId == Urls.GET_ACCOUNT_DETAILS_TAG) {
            pDialog.dismissWithAnimation();
            userDetailsRL.setVisibility(View.VISIBLE);
            changePasswordRL.setVisibility(View.GONE);
            updateUserRL.setVisibility(View.GONE);

            try {
                JSONObject responseObj = new JSONObject(object.toString());
                firstNameET.setText(responseObj.optString("FirstName"));
                lastNameET.setText(responseObj.optString("LastName"));
                nameTV.setText(responseObj.optString("FirstName") + " " + responseObj.optString("LastName"));
                emailTV.setText(responseObj.optString("UserName"));
                accountExpiryTV.setText(responseObj.optString("AccountExpiry").substring(0, 10));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (requestId == Urls.UPDATE_USER_TAG) {
            showSuccessDialog("Profile updated successfully.",
                    firstNameET.getText().toString(), lastNameET.getText().toString());

        } else if (requestId == Urls.CHANGE_PASSWORD_TAG) {
            oldPasswordET.setText("");
            newPasswordET.setText("");
            repeatPasswordET.setText("");
            showSuccessDialog("Password changed successfully.", "", "");
            AppUtils.setStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_PASSWORD, newPasswordET.getText().toString());
        }
    }
}
