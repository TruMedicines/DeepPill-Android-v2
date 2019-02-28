package truemedicine.logiticks.com.trumedicine.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.activity.ScanActivity;
import truemedicine.logiticks.com.trumedicine.model.ImageSearchResponseModel;
import truemedicine.logiticks.com.trumedicine.network.NetworkManager;
import truemedicine.logiticks.com.trumedicine.network.Urls;


public class ImageSearchBackgroundService extends Service implements NetworkManager.OnNetWorkListener {
    private NotificationCompat.Builder builder;
    private NetworkManager networkManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "active");
        networkManager = NetworkManager.getInstance(getApplicationContext());
        networkManager.setOnServiceNetworkListener(this);
        builder = new NotificationCompat.Builder(this);
        return super.onStartCommand(intent, flags, startId);


    }

    private void showNotification(int i) {
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (i == 0) {
            builder.setContentTitle("Image search completed");
            builder.setContentText("No match found");
        } else {
            builder.setContentTitle("Image search completed");
            builder.setContentText(ScanActivity.searchResponseModels.size() + " match found");
        }
        builder.setAutoCancel(true);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ScanActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ScanActivity.class);
        stackBuilder.addNextIntent(resultIntent);
// Adds the Intent that starts the Activity to the top of the stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onErrorResponse(VolleyError error, int requestId) {
        ScanActivity.isSearching = false;
        showNotification(0);
    }

    @Override
    public void onResponse(Object object, int type, int requestId) {
        ScanActivity.isSearching = false;
        if (requestId == Urls.QR_CODE_URL_TAG || requestId == Urls.IMAGE_SEARCH_URL_TAG) {
            ScanActivity.searchResponseModels.clear();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<ImageSearchResponseModel>>() {
            }.getType();
            ScanActivity.searchResponseModels.addAll((ArrayList<ImageSearchResponseModel>) gson.fromJson(object.toString(), listType));
            if (ScanActivity.searchResponseModels.isEmpty()) {
                showNotification(0);
            } else {
                showNotification(1);
            }
        }
    }
}
