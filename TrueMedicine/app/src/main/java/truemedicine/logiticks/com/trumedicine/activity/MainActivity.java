package truemedicine.logiticks.com.trumedicine.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.adapter.ImageDetailsRecyclerViewAdapter;
import truemedicine.logiticks.com.trumedicine.db.DBManager;
import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.model.ImageSearchResponseModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.services.FetchAllFreeImageService;
import truemedicine.logiticks.com.trumedicine.services.SyncService;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;
import truemedicine.logiticks.com.trumedicine.utils.Privacy;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkManager.OnNetWorkListener {

    private ArrayList<ImageListModel> mList;
    private ImageDetailsRecyclerViewAdapter detailsRecyclerViewAdapter;
    private DBManager mDbManager;
    private RecyclerView mImageDetailsRecyclerView;
    private CoordinatorLayout mCoordinatorLayout;
    private boolean isFreeApp = true;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private SweetAlertDialog pDialog;
    private int count;//variable keep the position of image in the "images" array.
    private ArrayList<ImageListModel> images;
    private NetworkManager networkManager;
    public static ArrayList<ImageSearchResponseModel> searchResponseModels;
    private String serverImagename;
    private SyncCompleteReceiver syncCompleteReceiver;
    private IntentFilter intentFilter;
    private String mSearchText = "";
    private int sortType = 0;
    private SearchView searchView;
    private IntentIntegrator intentIntegrator;
    private static String mQRValue;
    private boolean freeImageFetch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TruMedicines");
        setSupportActionBar(toolbar);
        checkIsFreeApp();
        mDbManager = new DBManager(this);
        intentIntegrator = new IntentIntegrator(this);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);


        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDbManager.getAllImageDetails(isFreeApp).size() >= 50 && isFreeApp) {

                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Limit exceeded").setConfirmText("Upgrade")
                            .setContentText(getResources().getString(R.string.storage_exceeded))
                            .setCancelText("Cancel")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                                    startActivity(intent);
                                }
                            }).show();
                } else {
                    searchView.setQuery("", true);
                    searchView.setIconified(true);
                    Intent intent = new Intent(MainActivity.this, ImageDetailActivity.class);
                    startActivity(intent);
                }

            }
        });


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setupDrawer();


        mImageDetailsRecyclerView = (RecyclerView) findViewById(R.id.imageDetails_recycler_view);
        mImageDetailsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mImageDetailsRecyclerView.setLayoutManager(mLayoutManager);
        mList = new ArrayList<>();
        setupList();
        networkManager = NetworkManager.getInstance(this);


        networkManager.setOnNetworkListener(MainActivity.this);
        syncCompleteReceiver = new SyncCompleteReceiver();
        intentFilter = new IntentFilter("com.trumedicines.android.SYNC_ACTION");


        if (AppUtils.hasDataConnectivity(MainActivity.this) && !isFreeApp) {
            images = mDbManager.getUnUploadedImages();
            if (!images.isEmpty() && !AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG)) {
                AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG, true);
                showSyncDialog("Sync with server", "Some pending uploads found.\n Do you want to sync with server?");

            } else if (!AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG)) {
                AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG, true);
                showSyncDialog("Sync with server", "Do you want to sync with server?");
            }
        }
        getFreeImageFetchStatus();
        if (isFreeApp && !freeImageFetch) {
            showSyncDialog("Download Free Data", "Do you want to download free pill data?");
        }

    }

    private void getFreeImageFetchStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        freeImageFetch = preferences.getBoolean(AppConstants.FREE_IMAGE_FETCH_STATUS, false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AppConstants.FREE_IMAGE_FETCH_STATUS, true);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncCompleteReceiver, intentFilter);
        loadFromDb();
        filterItems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncCompleteReceiver);
    }

    private void loadFromDb() {
        mList.clear();
        switch (sortType) {
            case 0:
                mList.addAll(mDbManager.getAllImageDetails(isFreeApp));
                break;
            case 1:
                mList.addAll(mDbManager.getAllImageDetailsAZ(isFreeApp));
                break;
            case 2:
                mList.addAll(mDbManager.getAllImageDetailsZA(isFreeApp));
                break;
        }

        if (mList.isEmpty()) {
            mImageDetailsRecyclerView.setVisibility(View.GONE);
        } else {
            mImageDetailsRecyclerView.setVisibility(View.VISIBLE);
        }
        detailsRecyclerViewAdapter.notifyFilterList();
    }

    private void syncWithServer() {
        startService(new Intent(this, SyncService.class));

/*        images = mDbManager.getUnUploadedImages();
        if (images.isEmpty()) {
            createDialog("Syncing with server..");
            fetchAllImagesFromServer();
        } else {
          *//*  createDialog("Uploading image..");
            count = 0;
            upImage();*//*


        }*/

    }

    private void showSyncDialog(String title, String messsage) {


        pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(messsage)
                .setCancelText("No")
                .setConfirmText("Yes")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        if (isFreeApp) {
                            syncWithServerForFreeImages();
                        } else {
                            syncWithServer();
                        }
                    }
                });

        pDialog.show();


    }

    private void syncWithServerForFreeImages() {
        startService(new Intent(this, FetchAllFreeImageService.class));
    }

   /* private void fetchAllImagesFromServer() {
        networkManager.setOnNetworkListener(this);
        networkManager.postJsonRequest(NetworkOptions.GET_REQUEST, Urls.GET_ALL_IMAGES_URL,
                new JSONObject(), Urls.GET_ALL_IMAGES_TAG, true);
        //swipeRefreshLayout.setRefreshing(true);
    }*/

    private void setupDrawer() {
        if (isFreeApp) {
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        } else {
            navigationView.inflateMenu(R.menu.activity_main_drawer_paid);
        }
    }

    private void checkIsFreeApp() {
        if (AppUtils.getStringSharedPreference(getApplicationContext(), AppConstants.SIGNIN_KEY_USERNAME).contentEquals("")) {
            isFreeApp = true;
        } else {
            isFreeApp = false;
        }
    }

    private void upImage() {
        if (count < images.size()) {
            networkManager.postRequest(NetworkOptions.POST_REQUEST, Urls.IMAGE_UPLOAD_URL,
                    new File(images.get(count).getmImagePath()), Urls.IMAGE_UPLOAD_URL_TAG, true);
            int realCount = count + 1;
            pDialog.setTitleText("Uploading image " + realCount + " of " + images.size());
//            AppUtils.makeShortToast(getApplicationContext(), "Uploading image " + realCount + " of " + images.size());
        } else {
            syncWithServer();
        }
    }

    private void createImage(String imageUrl) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Id", images.get(count).getmImageId());
            jsonObject.put("Imageurl", imageUrl);
            jsonObject.put("Name", images.get(count).getmTitle());
            jsonObject.put("Description", images.get(count).getmDescription());
            jsonObject.put("Location", "");
            jsonObject.put("QRPrefix", "");
            jsonObject.put("QRSerialStart", "");
            jsonObject.put("QRSerialEnd", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.CREATE_IMAGE_URL,
                jsonObject, Urls.CREATE_IMAGE_URL_TAG, true);
    }

    private void dismissDialog() {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.dismissWithAnimation();
            }
        }
    }

    private void createDialog(String s) {
        //Dialog stuff
        pDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(s);
        pDialog.setContentText("");
        pDialog.showCancelButton(false);
        pDialog.setCancelable(false);
        networkManager.setOnNetworkListener(this);
    }


    private void setupList() {
        detailsRecyclerViewAdapter = new ImageDetailsRecyclerViewAdapter(MainActivity.this, mList);
        mImageDetailsRecyclerView.setAdapter(detailsRecyclerViewAdapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showExitDialog();
        }
    }

    private void showExitDialog() {
        if (isFreeApp || !AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_REMEMBER_ME)) {
            finish();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("You are about to exit from TruMedicines")
                    .setCancelText("No, cancel!")
                    .setConfirmText("Yes, Exit!")
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchText = newText;
                filterItems();


                return false;
            }
        });

        return true;
    }

    private void filterItems() {
        int size = detailsRecyclerViewAdapter.filter(mSearchText);
        if (size == 0) {
            mImageDetailsRecyclerView.setVisibility(View.GONE);
        } else {
            mImageDetailsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_orderAZ) {
            sortType = 1;
            loadFromDb();
        } else if (id == R.id.action_action_orderZA) {
            sortType = 2;
            loadFromDb();
        } else if (id == R.id.action_sort_by_date) {
            sortType = 0;
            loadFromDb();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                //go to profile
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sync) {

            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                images = mDbManager.getUnUploadedImages();
                if (!images.isEmpty()) {
                    AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG, true);
                    showSyncDialog("Sync with server", "Some pending uploads found.\n Do you want to sync with server?");

                } else {
                    AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG, true);
                    showSyncDialog("Sync with server", "Do you want to sync with server?");
                }
            }
        } else if (id == R.id.nav_sync_free) {

            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {

                AppUtils.setBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_SYNC_DIALOG, true);
                showSyncDialog("Download Free Data", "Do you want to download free pill data?");

            }
        } else if (id == R.id.nav_license) {
            showLicenseDialog();
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, TermsLicenseActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_upgrade) {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_invite) {
            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                Intent intent = new Intent(MainActivity.this, ManageUserActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.nav_qrcode) {
            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.nav_qrcode_search) {
            if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
                intentIntegrator.initiateScan();
            }
        } else if (id == R.id.nav_signout) {
            AppUtils.cleardata(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(intent);
            finishAffinity();
        } else if (id == R.id.nav_login) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE: {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        //searchMode = searchModeTemp;
                        mQRValue = result.getContents();
                        if (Patterns.WEB_URL.matcher(mQRValue).matches()) {
                            Intent webViewIntent = new Intent(MainActivity.this, QRCodeURLWebView.class);
                            webViewIntent.putExtra("url", mQRValue);
                            startActivity(webViewIntent);
                        } else {
                            Toast.makeText(this, "Invalid URL!", Toast.LENGTH_LONG).show();
                        }
                        //mQRTextView.setText(mQRValue);
                        //searchImageForMatch();
                    }
                }
            }
            break;
        }
    }

    private void showLicenseDialog() {

        /*
        * License Dialog external library
        * find it from https://github.com/PSDev/LicensesDialog
        */
        final License license = new Privacy();
        final Notice notice = new Notice(getResources().getString(R.string.app_license_name),
                getResources().getString(R.string.app_license_url),
                getResources().getString(R.string.app_copy_right), license);
        new LicensesDialog.Builder(this)
                .setNotices(notice)
                .build()
                .show().setTitle("END-USER LICENSE AGREEEMENT");
    }


    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        Log.e("ERROR", "" + error.toString());
        dismissDialog();
        //syncWithServer();
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        if (requestId == Urls.IMAGE_UPLOAD_URL_TAG) {
            serverImagename = object.toString().replace("\"", "");
            createImage(serverImagename);
        } else if (requestId == Urls.CREATE_IMAGE_URL_TAG) {
            Log.e("uploadImage", object.toString());
            //update image's upload status
            try {
                if (new JSONObject(object.toString()).optString("Id") != null) {
                    //update upload status & server id
                    mDbManager.updateImageUploadStatus(images.get(count).getmImageId(),
                            new JSONObject(object.toString()).optString("Id"), serverImagename);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //when image has created, upload the next image.
            count += 1;
            upImage();
        }
        /*else if (requestId == Urls.GET_ALL_IMAGES_TAG) {

            parseAllImages(object.toString());
        }*/
    }

    /*private void parseAllImages(String response) {
        Log.e("ALL_IMAGE_RESPONSE", response);
        searchResponseModels = new ArrayList<>();
        searchResponseModels.clear();
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<ImageSearchResponseModel>>() {
        }.getType();
        searchResponseModels.addAll((ArrayList<ImageSearchResponseModel>) gson.fromJson(response, listType));
        storeToDb();
    }

    private void storeToDb() {
        mDbManager.clear();
        for (ImageSearchResponseModel model : searchResponseModels) {
            String[] ts = model.createdOn.split("T");
            mDbManager.addImageDetails(model.id + "", model.name, model.description, model.imageurl, ts[0], ts[1], 1, 0);
        }
        loadFromDb();

    }
*/
    public class SyncCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("loadFromDb", 0) == 1) {
                loadFromDb();
            } else {
                detailsRecyclerViewAdapter.notifyFilterList();
            }
        }
    }

}
