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

import org.json.JSONObject;

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


public class FetchAllFreeImageService extends Service implements NetworkManager.OnNetWorkListener {
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
        fetchAllImagesFromServer();

        return START_NOT_STICKY;


    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mNotificationManager.cancel(1);
        stopSelf();
    }


    private void fetchAllImagesFromServer() {
        builder.setContentText("Syncing in progress...");
        builder.setProgress(0, 0, true);
        mNotificationManager.notify(1, builder.build());
        networkManager.postJsonRequest(NetworkOptions.GET_REQUEST, Urls.GET_ALL_FREE_IMAGES_URL,
                new JSONObject(), Urls.GET_ALL_FREE_IMAGES_TAG, false);
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
//        if (AppUtils.hasDataConnectivity(this)) {
//            if (requestId == Urls.GET_ALL_FREE_IMAGES_TAG) {
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
        if (requestId == Urls.GET_ALL_FREE_IMAGES_TAG) {
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
        mDbManager.clearFreeImages();
        mTotalImageToDownload = searchResponseModels.size();
        for (ImageSearchResponseModel model : searchResponseModels) {
            String[] ts = model.createdOn.split("T");
            mDbManager.addFreeImageDetails("", model.name, model.description, model.imageurl, ts[0], ts[1], 0, 1);
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
                                  brodcastIntent.putExtra("loadFromDb", 0);
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
                                  brodcastIntent.putExtra("loadFromDb", 0);
                                  sendBroadcast(brodcastIntent);

                                  if (!AppUtils.hasDataConnectivity(getApplicationContext())) {
                                      mNotificationManager.cancel(1);
                                      stopSelf();
                                  } else if (imageDownload == mTotalImageToDownload) {
                                      showNotification(1);
                                      stopSelf();

                                  }
                              }
                          }

                    );
        }
        brodcastIntent.putExtra("loadFromDb", 1);
        sendBroadcast(brodcastIntent);
    }

    private void showNotification(int i) {
        builder.setSmallIcon(R.drawable.ic_launcher);
        if (i == 0) {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Downloading free pill data...");
            builder.setOngoing(true);
        } else if(i==1)
        {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Downloading completed");
            builder.setAutoCancel(true);
            builder.setOngoing(false);
            builder.setProgress(0, 0, false);
        }
        else {
            builder.setContentTitle("TruMedicines");
            builder.setContentText("Downloading failed!");
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
