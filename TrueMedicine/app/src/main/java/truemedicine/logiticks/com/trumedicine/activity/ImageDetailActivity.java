package truemedicine.logiticks.com.trumedicine.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;
import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.db.DBManager;
import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

public class ImageDetailActivity extends AppCompatActivity
        implements View.OnClickListener, TextToSpeech.OnInitListener, NetworkManager.OnNetWorkListener {

    private static final int MY_DATA_CHECK_CODE = 101;
    private String mImgDecodableString = "";
    private String mTitle = "";
    private String mImageId = "";

    private NetworkManager networkManager;

    private static final int TAKE_PICTURE = 150, SELECT_PICTURE = 151;
    private final String[] mCameraPermission = {Manifest.permission.CAMERA};
    private final String[] mStoragePermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    private Button mAudioPlayButton = null;
    private Button mEditButton = null;
    private Button mDeleteButton = null;
    private ImageButton mCaptureButton = null;
    private TextView mTitleTextView = null;
    private TextView mDescriptionTextView = null;
    private ImageButton mGalleryButton = null;
    private RelativeLayout mHomeRelativeLayout = null;
    private ImageView mItemImageView = null;
    private EditText mTitleEditText = null;
    private EditText mDescriptionEditText;
    private Button mAddDetailsToDbButton = null;
    private Button mUpdateImageDetailsButton = null;
    private DBManager mDbManager = null;
    private TextToSpeech textToSpeech = null;
    private String mTextToSpeechStatus = "";
    private SweetAlertDialog pDialog;
    private long lastInsertedImageId;
    private boolean fromServer = false;
    private Toolbar toolbar;
    private Uri tempfileUri;
    private String mDescription;
    private String serverImagename;
    private ImageListModel listModel;
    private boolean update = false;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        checkTextToSpeechStatus();

        setUpComponents();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    private void setUpComponents() {
        try {
            mImgDecodableString = getIntent().getStringExtra("ImagePath");
        } catch (NullPointerException e) {
            mImgDecodableString="";
            e.printStackTrace();
        }
        try {
            listModel = (ImageListModel) getIntent().getSerializableExtra("model");
            mTitle = listModel.getmTitle();

            mImgDecodableString = listModel.getmImagePathLocal();

            mDescription = listModel.getmDescription();
            mImageId = listModel.getmImageId();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
if(mImgDecodableString==null)
{
    mImgDecodableString="";
}

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        networkManager = NetworkManager.getInstance(this);
        networkManager.setOnNetworkListener(this);
        setUI();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUI() {

        mDbManager = new DBManager(ImageDetailActivity.this);
        mItemImageView = (ImageView) findViewById(R.id.imgView);
        mTitleEditText = (EditText) findViewById(R.id.titleEditText);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        mAddDetailsToDbButton = (Button) findViewById(R.id.addDetailsToDbButton);
        mUpdateImageDetailsButton = (Button) findViewById(R.id.updateImageDetailsButton);
        mAudioPlayButton = (Button) findViewById(R.id.playButton);
        mEditButton = (Button) findViewById(R.id.editButton);
        mDeleteButton = (Button) findViewById(R.id.deleteButton);
        mCaptureButton = (ImageButton) findViewById(R.id.captureButton);
        mGalleryButton = (ImageButton) findViewById(R.id.galleryButton);
        mHomeRelativeLayout = (RelativeLayout) findViewById(R.id.detailRelativeLayout);
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);

        mEditButton.setVisibility(View.GONE);
        mAudioPlayButton.setVisibility(View.GONE);
        mDeleteButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);

        mUpdateImageDetailsButton.setVisibility(View.GONE);
        if (!mTitle.equals("")) {
            toolbar.setTitle("View Image");
            setSupportActionBar(toolbar);
            mTitleTextView.setVisibility(View.VISIBLE);
            mTitleTextView.setText(mTitle);
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setText(mDescription);
            mTitleEditText.setVisibility(View.GONE);
            mDescriptionEditText.setVisibility(View.GONE);
            mTitleEditText.setText(mTitle);
            mDescriptionEditText.setText(mDescription);
            mAddDetailsToDbButton.setVisibility(View.GONE);
            mCaptureButton.setVisibility(View.GONE);
            mGalleryButton.setVisibility(View.GONE);
            mEditButton.setVisibility(View.VISIBLE);
            mAudioPlayButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle("Add Image");
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            if (listModel != null) {
                if (listModel.getUploadStatus() == 0 && listModel.getmImagePathLocal().contains("/")) {
                    Glide.with(getApplicationContext())
                            .load(listModel.getmImagePathLocal())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                            into(mItemImageView);
                } else {
                    Glide.with(getApplicationContext())
                            .load(Urls.IMAGE_BLOB_URL + listModel.getmImagePath())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                            into(mItemImageView);

                }
                findViewById(R.id.capture_upload).setVisibility(View.GONE);
            }
            else if(!mImgDecodableString.isEmpty())
            {
                Glide.with(getApplicationContext())
                        .load(mImgDecodableString)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                        into(mItemImageView);
            }
        } catch (NullPointerException e) {
        }

        setWidgetAction();
    }

    private void setWidgetAction() {
        mAddDetailsToDbButton.setOnClickListener(this);
        mUpdateImageDetailsButton.setOnClickListener(this);
        mAudioPlayButton.setOnClickListener(this);
        mEditButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mCaptureButton.setOnClickListener(this);
        mGalleryButton.setOnClickListener(this);
    }

    private void checkTextToSpeechStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ImageDetailActivity.this);
        mTextToSpeechStatus = preferences.getString(AppConstants.TEXT_TO_SPEECH_STATUS, "");
        Log.d("preference", "mTextToSpeech value " + mTextToSpeechStatus);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mAddDetailsToDbButton.getId()) {
            mTitle = mTitleEditText.getText().toString().trim();
            mDescription = mDescriptionEditText.getText().toString().trim();
            if (mTitle.equals("")) {
                mTitleEditText.setError("Enter Title");
                mTitleEditText.setText("");
            }  else if (mImgDecodableString.equals("")) {
                Snackbar snackbar = Snackbar.make(v, getResources().getString(R.string.upload_pic), Snackbar.LENGTH_LONG).setAction("", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).setActionTextColor(Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);
                snackbar.show();
               /* Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.upload_pic), Toast.LENGTH_LONG).show();*/
            } else {
                addDetailsToDb(mTitle, mDescription, mImgDecodableString);
            }

        } else if (v.getId() == mAudioPlayButton.getId()) {
            play();

        } else if (v.getId() == mCaptureButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(mCameraPermission[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[1]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(mStoragePermission[0]) == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                } else {
                    //request permission
                    requestPermissions(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PICTURE);
                }
            } else {
                captureImage();
            }

        } else if (v.getId() == mGalleryButton.getId()) {
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

        } else if (v.getId() == mEditButton.getId()) {
            toolbar.setTitle("Edit Image");
            setSupportActionBar(toolbar);
            update = true;
            mUpdateImageDetailsButton.setVisibility(View.VISIBLE);
            mTitleEditText.setVisibility(View.VISIBLE);
            mDescriptionEditText.setVisibility(View.VISIBLE);
            mTitleTextView.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            mTitleEditText.setEnabled(true);
            mItemImageView.setEnabled(true);
            mCaptureButton.setVisibility(View.VISIBLE);
            mGalleryButton.setVisibility(View.VISIBLE);
            mAudioPlayButton.setVisibility(View.GONE);
            mEditButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);


        } else if (v.getId() == mUpdateImageDetailsButton.getId()) {
            Log.d("LIST", "M HERE REACHED");
            mTitle = mTitleEditText.getText().toString().trim();
            mDescription = mDescriptionEditText.getText().toString().trim();
            if (mTitle.equals("")) {
                mTitleEditText.setError("Enter Title");
                mTitleEditText.setText("");
            } else if (mImgDecodableString.equals("")) {
                Snackbar snackbar = Snackbar.make(v, getResources().getString(R.string.upload_pic), Snackbar.LENGTH_LONG).setAction("", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).setActionTextColor(Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);
                snackbar.show();
               /* Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.upload_pic), Toast.LENGTH_LONG).show();*/
            } else {
                Log.d("LIST", "M HERE REACHED");
                updateImageDetails(mTitle, mDescription, mImgDecodableString);
            }

        } else if (v.getId() == mDeleteButton.getId()) {
            deleteImageDetail();
        }
    }

    private void deleteImageDetail() {

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this image!")
                .setCancelText("No, cancel!")
                .setConfirmText("Yes, delete it!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        if (mDbManager.isImageUploaded(mImageId) && !AppUtils.getStringSharedPreference(getApplicationContext(),
                                AppConstants.SIGNIN_KEY_USERNAME).equalsIgnoreCase("") && AppUtils.hasDataConnectivity(ImageDetailActivity.this)) {
                            if (mDbManager.getImageOfId(mImageId) != null) {
                                sDialog.setTitleText("Deleting image..")
                                        .showCancelButton(false)
                                        .setContentText("")
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                deleteImageInServer(listModel.getmServerId());
                            }
                        } else {
                            boolean deleted = mDbManager.deleteImageDetails(mImageId);
                            if (deleted) {
                                sDialog.dismiss();
                                showDeleteSuccessDialog();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletion_failed), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .show();
    }

    private void deleteImageInServer(String serverId) {
        // TODO: 17/6/16 What should be the case of no internet?
        if (AppUtils.hasDataConnectivity(ImageDetailActivity.this)) {
            networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.DELETE_IMAGE_URL + serverId,
                    new JSONObject(), Urls.DELETE_IMAGE_TAG, true);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_net_to_delete), Toast.LENGTH_LONG).show();
        }
    }

    private void chooseFromLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private void captureImage() {
        Intent intents = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tempfileUri = Uri.fromFile(AppUtils.getOutputMediaFile());
        intents.putExtra(MediaStore.EXTRA_OUTPUT, tempfileUri);

        // start the image capture Intent
        startActivityForResult(intents, TAKE_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TAKE_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                captureImage();
            }
        } else if (requestCode == SELECT_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                chooseFromLibrary();
            }
        }
    }

    private void updateImageDetails(String title, String description,
                                    String image_path) {
        // TODO Auto-generated method stub
        Log.d("LIST", "M HERE REACHED");
        String time = getCurrentTime();
        String date = getCurrentDate();
        // addDetailsToDb(title,description,image_path);
        lastInsertedImageId = mDbManager.addImageDetails(listModel.getmServerId(), title, description,
                image_path, date, time, 0, 1);
        mDbManager.deleteImageDetails(mImageId);
        if (lastInsertedImageId > 0) {

            //If user logged in & internet is available, upload image to server
            if (!AppUtils.getStringSharedPreference(getApplicationContext(),
                    AppConstants.SIGNIN_KEY_USERNAME).equalsIgnoreCase("") &&
                    AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout))) {
                if (!listModel.getmImagePathLocal().contentEquals(mImgDecodableString)||listModel.getUploadStatus()==0) {
                    uploadImage(image_path);
                } else {
                    showProgrssDialog("Updating image details...");
                    serverImagename=listModel.getmImagePath();
                    createImage(listModel.getmImagePath());
                }

            } else {
                showUploadSuccessDialog();
            }

        }
    }


    /****
     * for playing text to speech
     **********/
    private void play() {
        if (mTextToSpeechStatus.equalsIgnoreCase(getResources().getString(R.string.trues))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("tts", "inside");
                textToSpeech.speak(mTitle + ".           " + mDescription, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(mTitle + ".           " + mDescription, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Toast.makeText(getApplicationContext(), "TextToSpeech functionality is disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
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

    private void addDetailsToDb(String title, String description,
                                String imagePath) {
        // TODO Auto-generated method stub
        String time = getCurrentTime();
        String date = getCurrentDate();
        lastInsertedImageId = mDbManager.addImageDetails("", title, description,
                imagePath, date, time, 0, 0);

        //check if insertion was successful or not
        if (lastInsertedImageId > 0) {

            //If user logged in & internet is available, upload image to server
            if (!AppUtils.getStringSharedPreference(getApplicationContext(),
                    AppConstants.SIGNIN_KEY_USERNAME).equalsIgnoreCase("") &&
                    AppUtils.checkIsConnectedToNetwork(this, findViewById(R.id.main_coordinator_layout))) {
                uploadImage(imagePath);
            } else {
                showUploadSuccessDialog();
            }

        } else {
            insertionFailed();
        }
    }

    private void showProgrssDialog(String s) {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(s);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void uploadImage(String imagePath) {

        showProgrssDialog("Uploading image...");
        networkManager.postRequest(NetworkOptions.POST_REQUEST, Urls.IMAGE_UPLOAD_URL,
                new File(imagePath), Urls.IMAGE_UPLOAD_URL_TAG, true);
    }

    private void createImage(String imageUrl) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (update) {
                jsonObject.put("Id", listModel.getmServerId());
            } else {
                jsonObject.put("Id", "");
            }
            jsonObject.put("Imageurl", imageUrl);
            jsonObject.put("Name", mTitle);
            jsonObject.put("Description", mDescription);
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

    private void insertionFailed() {

        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.dismissWithAnimation();
            }
        }

        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getResources().getString(R.string.oops))
                .setContentText(getResources().getString(R.string.something_went_wrong))
                .show();

    }

    private String getCurrentDate() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy",Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        Log.d("date", "date " + formattedDate);
        return formattedDate;
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        Log.d("Time zone", "=" + tz.getDisplayName());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm ",Locale.getDefault());
        date.setTimeZone(tz);
        String localTime = date.format(currentLocalTime);
        Log.d("time", " tiime is " + localTime);
        return localTime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                File file = new File(AppUtils.getPath(getApplicationContext(), data.getData()));
                Uri fileUri = Uri.fromFile(file);
                previewCapturedImage(fileUri);
            } else if (requestCode == TAKE_PICTURE) {
                File f = new File(AppUtils.getPath(getApplicationContext(), tempfileUri));
                tempfileUri = Uri.fromFile(f);
                previewCapturedImage(tempfileUri);
            }

        }
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                textToSpeech = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void previewCapturedImage(Uri selectedImage) {
        try {
            findViewById(R.id.capture_upload).setVisibility(View.GONE);
            mImgDecodableString = selectedImage.getPath();
            Glide.with(getApplicationContext())
                    .load(mImgDecodableString)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                    into(mItemImageView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void onCaptureImageResult(Intent data) {
        // TODO Auto-generated method stub
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (thumbnail != null) {
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }
        Log.d("IMAGE", "bitmap image thumbnail  " + thumbnail);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        Log.d("IMAGE", "bitmap image destination " + destination);
        mImgDecodableString = "" + destination;
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHomeRelativeLayout.setBackgroundColor(getResources().getColor(R.color.button_text_color));
        mItemImageView.setImageBitmap(thumbnail);
        Drawable d = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(mImgDecodableString));
        d.setAlpha(300);
    }

    private void onSelectFromGalleryResult(Uri data) {

        try {
            mImgDecodableString = data.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(mImgDecodableString);
            Log.d("IMAGE", "bitmap  " + mImgDecodableString);
            // Set the Image in ImageView after decoding the String

            mItemImageView.setImageBitmap(bitmap);
        } catch (OutOfMemoryError error) {
        }
        mHomeRelativeLayout.setBackgroundColor(getResources().getColor(R.color.button_text_color));


    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.US);
        } else {
            Toast.makeText(this, getResources().getString(R.string.text_to_speech_failed), Toast.LENGTH_LONG).show();
        }
        if (!mTitle.equalsIgnoreCase("")) {
            play();

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        Log.e("ERROR", error.toString());
        insertionFailed();
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        if (requestId == Urls.IMAGE_UPLOAD_URL_TAG) {
            serverImagename = object.toString().replace("\"", "");
            createImage(serverImagename);
        } else if (requestId == Urls.CREATE_IMAGE_URL_TAG) {
            //update image's upload status
            try {
                if (new JSONObject(object.toString()).optString("Id") != null && !update) {
                    //update upload status & server id
                    mDbManager.updateImageUploadStatus(Long.toString(lastInsertedImageId),
                            new JSONObject(object.toString()).optString("Id"), serverImagename);
                } else {
                    mDbManager.updateImageUploadStatus(Long.toString(lastInsertedImageId), listModel.getmServerId(), serverImagename);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismissWithAnimation();


            /* Sweet Alert Dialog -> custom dialog library
             * url : https://github.com/pedant/sweet-alert-dialog
             * clone from https://github.com/pedant/sweet-alert-dialog.git
             * */

            showUploadSuccessDialog();

        } else if (requestId == Urls.DELETE_IMAGE_TAG) {

            if (object.toString().replace("\"", "").equals("true")) {
                boolean deleted = mDbManager.deleteImageDetails(mImageId);
                if (deleted) {
                    showDeleteSuccessDialog();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletion_failed), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void showUploadSuccessDialog() {
        String message = "";
        if (update) {
            message = "Updated successfully.";
        } else {
            message = "Image saved successfully.";
        }
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success!")
                .setContentText(message).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
               /* Intent intent = new Intent(ImageDetailActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                finish();
            }
        }).show();
    }

    private void showDeleteSuccessDialog() {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.dismissWithAnimation();
            }
        }
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Deleted!")
                .setContentText("Image deleted successfully.")
                .setConfirmText("OK")
                .showCancelButton(false)
                .setCancelClickListener(null).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                /*Intent intent = new Intent(ImageDetailActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                finish();
            }
        })
                .show();


    }

    private void goBackToMainActivity() {
        finish();
    }



}
