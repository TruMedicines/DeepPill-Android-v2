package truemedicine.logiticks.com.trumedicine.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.FileProvider;
import android.os.Environment;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import truemedicine.logiticks.com.trumedicine.utils.Privacy;
import volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.adapter.SearchResultRecyclerViewAdapter;
import truemedicine.logiticks.com.trumedicine.model.ImageSearchResponseModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.services.ImageSearchBackgroundService;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener, NetworkManager.OnNetWorkListener, SearchResultRecyclerViewAdapter.OnItemClickListener {

    private NetworkManager networkManager;
    private String email;
    private SweetAlertDialog pDialog;
    private ImageButton fabScanImageButton, fabScanQRButton;
    private static final int TAKE_PICTURE = 150, SELECT_PICTURE = 151;
    private static final int MY_DATA_CHECK_CODE = 101;
    String IMAGE_DIRECTORY_NAME = "TruMedicine";
    private static Uri fileUri;
    private ImageView mSelectedImageView;
    private final String[] mCameraPermission = {Manifest.permission.CAMERA};
    private final String[] mStoragePermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static ArrayList<ImageSearchResponseModel> searchResponseModels;
    private RecyclerView mSearchResultListRecyclerView;
    private SearchResultRecyclerViewAdapter recyclerViewAdapter;
    private String mTextToSpeechStatus = "";
    private TextToSpeech textToSpeech = null;
    private static int searchMode = -1;
    private int searchModeTemp = -1;
    public static boolean isSearching = false;
    private boolean searching = false;
    private ProgressBar progressBar;
    private TextView mQRTextView;
    private static String mQRValue;
    private TextView mResultStatusTextView;
    private Uri tempfileUri;
    private IntentIntegrator intentIntegrator;
    private boolean mCropEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TruMedicine Pill Search");
        toolbar.setNavigationIcon(android.R.drawable.ic_dialog_info);
        setSupportActionBar(toolbar);
        networkManager = NetworkManager.getInstance(this);
        setUI();
        setList();
        checkTextToSpeechStatus();
        intentIntegrator = new IntentIntegrator(this);
        AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        stopService(new Intent(this, ImageSearchBackgroundService.class));
        networkManager.setOnServiceNetworkListener(this);
        mCropEnabled = AppUtils.getBooleanSharedPreference(getApplicationContext(), AppConstants.KEY_CROP_ENABLE);
        showSavedUI();
        super.onResume();
    }

    private void showSavedUI() {
        recyclerViewAdapter.notifyDataSetChanged();

        if (isSearching) {
            progressBar.setVisibility(View.VISIBLE);
            mResultStatusTextView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            mResultStatusTextView.setVisibility(View.VISIBLE);
            recyclerViewAdapter.notifyDataSetChanged();
            if (searchResponseModels.isEmpty()) {
                mResultStatusTextView.setText("No match found!");
            } else {
                if (searchResponseModels.size() == 1)
                {
                    mResultStatusTextView.setText(searchResponseModels.size() + " match found");
                }
                else
                {
                    mResultStatusTextView.setText(searchResponseModels.size() + " matches found");
                }

            }
        }
        if (searchMode == Urls.QR_CODE_URL_TAG && !searchResponseModels.isEmpty()) {
            mQRTextView.setVisibility(View.VISIBLE);
            mSelectedImageView.setVisibility(View.GONE);
            mQRTextView.setText(mQRValue);

        } else if (searchMode == Urls.IMAGE_SEARCH_URL_TAG && !searchResponseModels.isEmpty()) {
            mQRTextView.setVisibility(View.GONE);
            mSelectedImageView.setVisibility(View.VISIBLE);
            previewCapturedImage(fileUri);

        } else if (searchResponseModels.isEmpty()) {
            mResultStatusTextView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        startService(new Intent(this, ImageSearchBackgroundService.class));
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void checkTextToSpeechStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
        mTextToSpeechStatus = preferences.getString(AppConstants.TEXT_TO_SPEECH_STATUS, "");
        Log.d("preference", "mTextToSpeech value " + mTextToSpeechStatus);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }

    private void setUI() {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSearchResultListRecyclerView = (RecyclerView) findViewById(R.id.searchResults);
        fabScanImageButton = (ImageButton) findViewById(R.id.fab_scan_image);
        fabScanQRButton = (ImageButton) findViewById(R.id.fab_scan_qr);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mSelectedImageView = (ImageView) findViewById(R.id.selected_image);
        mQRTextView = (TextView) findViewById(R.id.qr_text);
        mResultStatusTextView = (TextView) findViewById(R.id.result_status);
        fabScanQRButton.setOnClickListener(this);
        fabScanImageButton.setOnClickListener(this);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
    }

    private void setList() {
        if (searchResponseModels == null) {
            searchResponseModels = new ArrayList<>();
        }
        recyclerViewAdapter = new SearchResultRecyclerViewAdapter(this, searchResponseModels, this);
        mSearchResultListRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mSearchResultListRecyclerView.setLayoutManager(mLayoutManager);
        mSearchResultListRecyclerView.setAdapter(recyclerViewAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /*
             * License Dialog external library
             * find it from https://github.com/PSDev/LicensesDialog
             */
            final License license = new Privacy();
            final Notice notice = new Notice(getResources().getString(R.string.app_license_name),
                    getResources().getString(R.string.app_license_url),
                    getResources().getString(R.string.app_copy_right), license);
            new LicensesDialog.Builder(ScanActivity.this)
                    .setNotices(notice)
                    .build()
                    .show().setTitle("END-USER LICENSE AGREEEMENT");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_scan_image: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(mCameraPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[1]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[0]) == PackageManager.PERMISSION_GRANTED) {
                        takePhoto();
                    } else {
                        //request permission
                        requestPermissions(new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PICTURE);
                    }
                } else {
                    takePhoto();
                }
            }
            break;
            case R.id.fab_scan_qr: {
                searchModeTemp = Urls.IMAGE_SEARCH_URL_TAG;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(mStoragePermission[1]) == PackageManager.PERMISSION_GRANTED) {
                        chooseFromLibrary();
                    } else {
                        //request permission
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_PICTURE);
                    }
                } else {
                    chooseFromLibrary();
                }
            }
            break;

        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        TextView title = new TextView(getApplicationContext());
        title.setText("Search");
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);


        AlertDialog.Builder builder = new AlertDialog.Builder(
                ScanActivity.this);


        builder.setCustomTitle(title);

        // builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(mCameraPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[1]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[0]) == PackageManager.PERMISSION_GRANTED) {
                            takePhoto();
                        } else {
                            //request permission
                            requestPermissions(new String[]{
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PICTURE);
                        }
                    } else {
                        takePhoto();
                    }

                } else if (items[item].equals("Choose from Library")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (checkSelfPermission(mStoragePermission[1]) == PackageManager.PERMISSION_GRANTED) {
                            chooseFromLibrary();
                        } else {
                            //request permission
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_PICTURE);
                        }
                    } else {
                        chooseFromLibrary();
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void chooseFromLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private void takePhoto() {
        Intent intent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Intent intents = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        tempfileUri  = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".FileProvider", AppUtils.getOutputMediaFile());

        intents.putExtra(MediaStore.EXTRA_OUTPUT, tempfileUri);

        // start the image capture Intent
        startActivityForResult(intents, TAKE_PICTURE);

    }

    private void searchImageForMatch() {
        if (AppUtils.checkIsConnectedToNetwork(getApplicationContext(), findViewById(R.id.main_coordinator_layout))) {
            mResultStatusTextView.setVisibility(View.GONE);
            searchResponseModels.clear();
            recyclerViewAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            isSearching = true;
            networkManager.setOnServiceNetworkListener(this);
            searching = true;
            if (searchMode == Urls.QR_CODE_URL_TAG) {
                mQRTextView.setVisibility(View.VISIBLE);
                mSelectedImageView.setVisibility(View.GONE);
                networkManager.postRequest(NetworkOptions.GET_REQUEST, Urls.QR_CODE_URL + "?serialNo=" + mQRValue, "", Urls.QR_CODE_URL_TAG, true);

            } else if (searchMode == Urls.IMAGE_SEARCH_URL_TAG) {
                mQRTextView.setVisibility(View.GONE);
                mSelectedImageView.setVisibility(View.VISIBLE);

                Log.d("myTag", "Search is occurring");

                networkManager.postRequest(NetworkOptions.POST_REQUEST, Urls.IMAGE_SEARCH_URL, new File(fileUri.getPath()), Urls.IMAGE_SEARCH_URL_TAG, true);
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {

        isSearching = false;
        progressBar.setVisibility(View.GONE);
        mResultStatusTextView.setVisibility(View.VISIBLE);
        mResultStatusTextView.setText("No match found!");
        if (requestId == Urls.IMAGE_SEARCH_URL_TAG && AppUtils.hasDataConnectivity(getApplicationContext())) {
            pDialog = new SweetAlertDialog(ScanActivity.this);
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            pDialog.setTitleText("Sorry!").setContentText("No match found!");
            pDialog.show();
        } else {
            pDialog = new SweetAlertDialog(ScanActivity.this);
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            pDialog.setTitleText("Sorry!")
                    .setContentText("No match found")
                    .setConfirmText("Retry").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    searchImageForMatch();
                }
            });
            pDialog.setCancelText("Cancel");
            pDialog.show();
        }
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        isSearching = false;
        progressBar.setVisibility(View.GONE);
        mResultStatusTextView.setVisibility(View.VISIBLE);
        Log.d("myTag", "Search happened " + object.toString());
        if (requestId == Urls.QR_CODE_URL_TAG || requestId == Urls.IMAGE_SEARCH_URL_TAG) {
            searchResponseModels.clear();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<ImageSearchResponseModel>>() {
            }.getType();

            searchResponseModels.addAll((ArrayList<ImageSearchResponseModel>) gson.fromJson(object.toString(), listType));
            recyclerViewAdapter.notifyDataSetChanged();

        }
        if (searchResponseModels.isEmpty()) {
            mResultStatusTextView.setText("No match found!");
            if (requestId == Urls.IMAGE_SEARCH_URL_TAG) {
                pDialog = new SweetAlertDialog(ScanActivity.this);
                pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                pDialog.setCancelable(true);
                pDialog.setCanceledOnTouchOutside(true);
                pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                pDialog.setTitleText("Sorry!")
                        .setContentText("No match found!");
                pDialog.setCancelText("No");
                pDialog.show();
            } else {
                pDialog = new SweetAlertDialog(ScanActivity.this);
                pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                pDialog.setCancelable(true);
                pDialog.setCanceledOnTouchOutside(true);
                pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
                pDialog.setTitleText("Sorry!")
                        .setContentText("No match found")
                        .setConfirmText("Retry").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        searchImageForMatch();
                    }
                });
                pDialog.setCancelText("Cancel");
                pDialog.show();
            }
        } else {
            if (searchResponseModels.size() == 1)
            {
                mResultStatusTextView.setText(searchResponseModels.size() + " match found");
            }
            else
            {
                mResultStatusTextView.setText(searchResponseModels.size() + " matches found");
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PICTURE:
                Bitmap bitmap = null;
                if (resultCode == RESULT_OK) {
                    if (data != null) {

                        try {
                            File file = new File(AppUtils.getPath(getApplicationContext(), data.getData()));
                            tempfileUri = Uri.fromFile(file);
                            cropImage();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
//                    File file = new File(AppUtils.getPath(getApplicationContext(), tempfileUri));
//                    tempfileUri = Uri.fromFile(file);

//                    File file = new File(AppUtils.getPath(getApplicationContext(), data.getData()));
//                    tempfileUri = Uri.fromFile(file);

                    cropImage();

                }


                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    searchMode = searchModeTemp;
                    Uri resultUri = result.getUri();
                    File f = new File(AppUtils.getPath(getApplicationContext(), resultUri));
                    fileUri = Uri.fromFile(f);
                    previewCapturedImage(fileUri);
                    searchImageForMatch();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
            break;
            case IntentIntegrator.REQUEST_CODE: {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        //searchMode = searchModeTemp;
                        mQRValue = result.getContents();
                        if(Patterns.WEB_URL.matcher(mQRValue).matches()) {
                            Intent webViewIntent = new Intent(ScanActivity.this, QRCodeURLWebView.class);
                            webViewIntent.putExtra("url", mQRValue);
                            startActivity(webViewIntent);
                        }
                        else {
                            Toast.makeText(this, "Invalid URL!", Toast.LENGTH_LONG).show();
                        }
                        //mQRTextView.setText(mQRValue);
                        //searchImageForMatch();
                    }
                }
            }
            break;
            case MY_DATA_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    textToSpeech = new TextToSpeech(ScanActivity.this, this);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
            }
            break;
        }

    }

    private void cropImage() {
        if (mCropEnabled) {
            CropImage.activity(tempfileUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        } else {
            searchMode = searchModeTemp;
//            File f = new File(AppUtils.getPath(getApplicationContext(), tempfileUri));
            fileUri = tempfileUri;
            previewCapturedImage(tempfileUri);
            searchImageForMatch();
        }
    }

    private void previewCapturedImage(Uri selectedImage) {
        try {
            Glide.with(getApplicationContext())
                    .load(selectedImage)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                    into(mSelectedImageView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TAKE_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                takePhoto();
            }
        } else if (requestCode == SELECT_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                chooseFromLibrary();
            }
        }
    }

    @Override
    public void onPlayClicked(ImageSearchResponseModel imageSearchResponseModel) {
        play(imageSearchResponseModel.name, imageSearchResponseModel.description);
    }

    /****
     * for playing text to speech
     * ********
     *
     * @param name
     * @param description
     */
    private void play(String name, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("tts", "inside");
            textToSpeech.speak(name + ".     " + description, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(name + ".     " + description, TextToSpeech.QUEUE_FLUSH, null);
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
