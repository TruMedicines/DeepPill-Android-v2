package truemedicine.logiticks.com.trumedicine.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.adapter.USerDetailRecyclerViewAdapter;
import truemedicine.logiticks.com.trumedicine.model.UserDetailModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

public class ManageUserActivity extends AppCompatActivity implements View.OnClickListener, NetworkManager.OnNetWorkListener, USerDetailRecyclerViewAdapter.OnItemClickListener {

    private NetworkManager networkManager;
    private SweetAlertDialog pDialog;
    private RecyclerView mUserListRecyclerView;
    private USerDetailRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<UserDetailModel> userDetailModels;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Users");
        setSupportActionBar(toolbar);

        setUI();
        setList();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
    }

    @Override
    protected void onResume() {
        getUsersList();
        super.onResume();
    }



    private void getUsersList() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Retrieving all invited users...");
        pDialog.setCancelable(false);
        pDialog.show();
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        JSONObject jsonObject = new JSONObject();
        networkManager.postJsonRequest(NetworkOptions.GET_REQUEST, Urls.GET_ALL_USERS_URL, jsonObject, Urls.GET_ALL_USERS_URL_TAG, true);

    }

    private void setList() {
        userDetailModels = new ArrayList<>();
        recyclerViewAdapter = new USerDetailRecyclerViewAdapter(this, userDetailModels, this);
        mUserListRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mUserListRecyclerView.setLayoutManager(mLayoutManager);
        mUserListRecyclerView.setAdapter(recyclerViewAdapter);


    }


    private void setUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUserListRecyclerView = (RecyclerView) findViewById(R.id.users_list);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageUserActivity.this, InviteUserActivity.class);
                startActivity(intent);

            }
        });
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

        }
    }


    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        if (requestId == Urls.GET_ALL_USERS_URL_TAG) {
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setTitleText("Error!")
                    .setContentText("Error while retrieving user list")
                    .setConfirmText("Retry").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    getUsersList();
                }
            });
            pDialog.setCancelText("Cancel");
            pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    finish();
                }
            })

            ;
        } else {
            pDialog.dismissWithAnimation();
        }

    }

    @Override
    public void onResponse(Object object, int type, int requestId) {

        if (requestId == Urls.GET_ALL_USERS_URL_TAG) {
            try {


                pDialog.dismiss();
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<UserDetailModel>>() {
                }.getType();
                userDetailModels.clear();
                userDetailModels.addAll((ArrayList<UserDetailModel>) gson.fromJson(object.toString(), listType));
                int index = 0;
                UserDetailModel userDetailModel = null;
                for (index = 0; index < userDetailModels.size(); index++) {
                    userDetailModel = userDetailModels.get(index);
                    if (AppUtils.getStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_USERNAME).equalsIgnoreCase(userDetailModel.email)) {
                        break;
                    }
                }
                if (!userDetailModels.isEmpty()) {
                    userDetailModels.remove(index);
                    userDetailModels.add(0, userDetailModel);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
                if (userDetailModels.size()>=11)
                {
                    fab.setVisibility(View.GONE);
                }
                else {
                    fab.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
            }

        } else if (requestId == Urls.DELETE_USERS_URL_TAG) {
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
            pDialog.setTitleText("Deleted!")
                    .setContentText("User has been deleted!")
                    .setConfirmText("Ok").
                    showCancelButton(false);
        }

    }

    @Override
    public void onItemDelete(UserDetailModel userDetailModel) {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Deleting user...");
        pDialog.setCancelable(false);
        pDialog.show();
        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userDetailModel.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        networkManager.postRequest(NetworkOptions.GET_REQUEST, Urls.DELETE_USERS_URL + "?userId=" + userDetailModel.id, "", Urls.DELETE_USERS_URL_TAG, true);
        userDetailModels.remove(userDetailModel);
        recyclerViewAdapter.notifyDataSetChanged();
        if (userDetailModels.size()>=11)
        {
            fab.setVisibility(View.GONE);
        }
        else {
            fab.setVisibility(View.VISIBLE);
        }

    }
}
