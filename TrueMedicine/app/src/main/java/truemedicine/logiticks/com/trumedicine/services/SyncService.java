package truemedicine.logiticks.com.trumedicine.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.activity.MainActivity;
import truemedicine.logiticks.com.trumedicine.db.DBManager;
import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.model.ImageSearchResponseModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.NetworkOptions;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;


public class SyncService extends Service implements NetworkManager.OnNetWorkListener {
    private NotificationCompat.Builder builder;
    private NetworkManager networkManager;
    private DBManager mDbManager;
    private ArrayList<ImageListModel> images;
    int count = 0;
    private String serverImagename;
    private NotificationManager mNotificationManager;
    private int mTotalImageSize = 0;
    private ImageListModel model;
    private Intent brodcastIntent;
    public ArrayList<ImageSearchResponseModel> searchResponseModels;
    private int imageDownload = 0, mTotalImageToDownload = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "active");
        networkManager = NetworkManager.getInstance(getApplicationContext());
        networkManager.setOnSyncNetworkListener(this);
        builder = new NotificationCompat.Builder(this);
        mDbManager = new DBManager(this);
        showNotification(0);
        brodcastIntent = new Intent("com.trumedicines.android.SYNC_ACTION");
        count = 0;
        imageDownload = 0;
        searchResponseModels = new ArrayList<>();
        syncWithServer();

        return START_NOT_STICKY;


    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mNotificationManager.cancel(1);
        stopSelf();
    }

    private void syncWithServer() {
        images = mDbManager.getUnUploadedImages();
        mTotalImageSize = images.size();
        if (mTotalImageSize > 0) {

            upImage();
        } else {

            fetchAllImagesFromServer();
        }
    }

    private void upImage() {
        if (count < mTotalImageSize) {
            model = images.get(count);
            if (model.getUpdatedStatus() == 1 && !model.getmImagePath().contains("/")) {
                serverImagename = model.getmImagePath();
                createImage(serverImagename);
            } else {
                networkManager.postRequest(NetworkOptions.POST_REQUEST, Urls.IMAGE_UPLOAD_URL,
                        new File(model.getmImagePathLocal()), Urls.IMAGE_UPLOAD_URL_SERVICE_TAG, true);
            }
            int realCount = count + 1;
            builder.setContentText("Uploading image (" + realCount + "/" + mTotalImageSize + ")");
            builder.setProgress(mTotalImageSize, realCount, false);
            mNotificationManager.notify(1, builder.build());
        } else {
            builder.setContentText("Syncing in progress...");
            builder.setProgress(0, 0, true);
            mNotificationManager.notify(1, builder.build());
            fetchAllImagesFromServer();

        }
    }

    private void createImage(String imageUrl) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (model.getUpdatedStatus() == 0) {
                jsonObject.put("Id", "");
            } else {
                jsonObject.put("Id", model.getmServerId());
            }
            jsonObject.put("Imageurl", imageUrl);
            jsonObject.put("Name", model.getmTitle());
            jsonObject.put("Description", model.getmDescription());
            jsonObject.put("Location", "");
            jsonObject.put("QRPrefix", "");
            jsonObject.put("QRSerialStart", "");
            jsonObject.put("QRSerialEnd", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        networkManager.postJsonRequest(NetworkOptions.POST_REQUEST, Urls.CREATE_IMAGE_URL,
                jsonObject, Urls.CREATE_IMAGE_URL_SERVICE_TAG, true);
    }

    private void fetchAllImagesFromServer() {
        builder.setContentText("Syncing in progress...");
        builder.setProgress(0, 0, true);
        mNotificationManager.notify(1, builder.build());
        networkManager.postJsonRequest(NetworkOptions.GET_REQUEST, Urls.GET_ALL_IMAGES_URL,
                new JSONObject(), Urls.GET_ALL_IMAGES__SERVICE_TAG, true);
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
//        if (AppUtils.hasDataConnectivity(this)) {
//            if (requestId == Urls.IMAGE_UPLOAD_URL_SERVICE_TAG) {
//                upImage();
//            } else if (requestId == Urls.CREATE_IMAGE_URL_SERVICE_TAG) {
//                createImage(serverImagename);
//            } else if (requestId == Urls.GET_ALL_IMAGES__SERVICE_TAG) {
//                fetchAllImagesFromServer();
//            }
//        } else
        {
            showNotification(2);
            stopSelf();
        }
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        if (requestId == Urls.IMAGE_UPLOAD_URL_SERVICE_TAG) {
            serverImagename = object.toString().replace("\"", "");
            createImage(serverImagename);
        } else if (requestId == Urls.CREATE_IMAGE_URL_SERVICE_TAG) {
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
        } else if (requestId == Urls.GET_ALL_IMAGES__SERVICE_TAG) {
            parseAllImages(object.toString());


        }
    }

    private void parseAllImages(String response) {
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
        mTotalImageToDownload = searchResponseModels.size();
        for (ImageSearchResponseModel model : searchResponseModels) {
            String[] ts = model.createdOn.split("T");
            mDbManager.addImageDetails(model.id + "", model.name, model.description, model.imageurl, ts[0], ts[1], 1, 0);
        }
        ArrayList<ImageListModel> allImageDetails = mDbManager.getAllImageDetails(false);
        for (ImageListModel imageDetail : allImageDetails) {
            Glide.with(getApplicationContext())
                    .load(Urls.IMAGE_BLOB_URL + imageDetail.getmImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<GlideDrawable>() {
                              @Override
                              public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                  imageDownload++;
                                  Log.d("image", imageDownload + "");
                                  brodcastIntent.putExtra("loadFromDb",0);
                                  sendBroadcast(brodcastIntent);
                                  if (imageDownload == mTotalImageToDownload) {
                                      showNotification(1);
                                      stopSelf();
                                  }
                                  //left empty
                              }
                              @Override
                              public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                  imageDownload++;
                                  Log.d("imagefailed", imageDownload + "");
                                  brodcastIntent.putExtra("loadFromDb",0);
                                  sendBroadcast(brodcastIntent);

                                  if(!AppUtils.hasDataConnectivity(getApplicationContext()))
                                  {
                                      mNotificationManager.cancel(1);
                                      stopSelf();
                                  }
                                  else if (imageDownload == mTotalImageToDownload) {
                                      showNotification(1);
                                      stopSelf();

                                  }
                              }
                          }

                    );
        }
        brodcastIntent.putExtra("loadFromDb",1);
        sendBroadcast(brodcastIntent);
    }

    private void showNotification(int i) {
        builder.setSmallIcon(R.drawable.ic_launcher);
        if (i == 0) {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Syncing in progress...");
            builder.setOngoing(true);
        }
        else if(i==1)
        {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Syncing completed");
            builder.setAutoCancel(true);
            builder.setOngoing(false);
            builder.setProgress(0, 0, false);
        }
        else {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Syncing failed!");
            builder.setAutoCancel(true);
            builder.setOngoing(false);
            builder.setProgress(0, 0, false);
        }


// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addNextIntent(resultIntent);
// Adds the Intent that starts the Activity to the top of the stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(1, builder.build());
    }
}
