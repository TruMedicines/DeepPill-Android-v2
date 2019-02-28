package truemedicine.logiticks.com.trumedicine.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.utils.EmailValidator;

public class InviteUserActivity extends AppCompatActivity implements View.OnClickListener, NetworkManager.OnNetWorkListener {

    private Button mSendInvitationButton;
    private NetworkManager networkManager;
    private String email;
    private SweetAlertDialog pDialog;
    private LinearLayout mMailContainer;
    private ArrayList<String> emailArrayList = new ArrayList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Invite Members");
        setSupportActionBar(toolbar);

        setUI();
      //  insertEmailField();
        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    private void insertEmailField() {
        final View mailView = getLayoutInflater().inflate(R.layout.item_email_enter, null);

        ImageButton delete = (ImageButton) mailView.findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMailContainer.removeView(mailView);
            }
        });
        mMailContainer.addView(mailView);
    }


    private void setUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSendInvitationButton = (Button) findViewById(R.id.inviteButton);
        mMailContainer = (LinearLayout) findViewById(R.id.mail_container);
        mSendInvitationButton.setOnClickListener(this);
        findViewById(R.id.add_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertEmailField();
            }
        });
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
            case R.id.inviteButton: {
                inviteUser();

            }
            break;

        }
    }

    private void inviteUser() {
        if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
            emailArrayList.clear();
            if (createEmailArray(mMailContainer)) {
                if (emailArrayList.isEmpty()) {
                    Toast.makeText(InviteUserActivity.this, "Enter Email id", Toast.LENGTH_SHORT).show();
                } else {
                    pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Sending invitations...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    JSONArray jsonArray = new JSONArray(emailArrayList);
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("", jsonArray);
                        Log.d("qwe", jsonObject.toString());
                        networkManager = NetworkManager.getInstance(this);
                        networkManager.setOnNetworkListener(this);
                        networkManager.postRequest(NetworkOptions.POST_REQUEST, Urls.INVITE_USERS_URL, jsonArray.toString(), Urls.INVITE_USERS_URL_TAG, true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private boolean createEmailArray(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            Object child = viewGroup.getChildAt(i);
            if (child instanceof EditText) {
                String emailText = ((EditText) child).getText().toString();
                if (EmailValidator.isValidEmail(emailText)) {
                    emailArrayList.add(emailText);
                } else {
                    ((EditText) child).setError("Enter Valid Email");
                    return false;
                }
            } else if (child instanceof ViewGroup) {

                if (!createEmailArray((ViewGroup) child)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setTitleText("Error!")
                    .setContentText(AppUtils.getNetworkError(error))
                    .setConfirmText("Ok");

            pDialog.showCancelButton(false);
        }

    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        String json = AppUtils.getStringSharedPreference(getApplicationContext(), "emails");
        ArrayList<String> invited = new ArrayList<>();
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();

            emailArrayList.addAll((ArrayList<String>) gson.fromJson(json, listType));
        }

        Gson gson = new Gson();
        AppUtils.setStringSharedPreference(getApplicationContext(), "emails", gson.toJson(emailArrayList));
        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Success!")
                .setContentText("Invitations sent successfully!")
                .setConfirmText("Ok").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.dismiss();
                finish();

            }
        })
        ;

    }

}
