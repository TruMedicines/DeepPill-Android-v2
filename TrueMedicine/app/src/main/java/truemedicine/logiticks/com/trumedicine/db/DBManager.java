package truemedicine.logiticks.com.trumedicine.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.model.ImageModel;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

public class DBManager extends SQLiteOpenHelper {

    private long timeInMilliseconds;

    public DBManager(Context context) {
        super(context, DBUtil.DB_NAME, null, DBUtil.DB_VERSION);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_IMAGE_DETAILS = "CREATE TABLE " + DBUtil.TABLE_IMAGE_DETAIL
                + "(" + DBUtil.IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DBUtil.IMAGE_TITLE + " VARCHAR,"
                + DBUtil.IMAGE_DESCRIPTION + " VARCHAR,"
                + DBUtil.IMAGE_PATH + " VARCHAR, "
                + DBUtil.IMAGE_PATH_LOCAL + " VARCHAR,"
                + DBUtil.IMAGE_DATE + " date,"
                + DBUtil.IMAGE_TIME + " VARCHAR,"
                + DBUtil.IMAGE_TIME_MILLISECOND + " VARCHAR,"
                + DBUtil.IMAGE_SERVER_ID + " VARCHAR,"
                + DBUtil.IMAGE_UPDATE_STATUS + " INTEGER,"
                + DBUtil.IS_FREE_IMAGE + " INTEGER DEFAULT '0',"
                + DBUtil.IMAGE_UPLOAD_STATUS + " INTEGER)";
        Log.d("QUERY", "QUERY " + CREATE_TABLE_IMAGE_DETAILS);

        db.execSQL(CREATE_TABLE_IMAGE_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBUtil.TABLE_IMAGE_DETAIL);

        onCreate(db);
    }

    public long addImageDetails(String server_id, String title, String description,
                                String imagePath, String date, String time, int uploadStatus, int updatedStatus) {


        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (uploadStatus == 1) {
            timeInMilliseconds = AppUtils.dateTimeToMillisecond(date + "T" + time);
        } else {
            timeInMilliseconds = System.currentTimeMillis();
        }


        values.put(DBUtil.IMAGE_TITLE, title);
        values.put(DBUtil.IMAGE_DESCRIPTION, description);
        values.put(DBUtil.IMAGE_PATH, imagePath);
        values.put(DBUtil.IMAGE_PATH_LOCAL, imagePath);
        values.put(DBUtil.IMAGE_DATE, AppUtils.millisecondToDate(timeInMilliseconds));
        values.put(DBUtil.IMAGE_TIME, AppUtils.millisecondToTime(timeInMilliseconds));
        values.put(DBUtil.IMAGE_TIME_MILLISECOND, timeInMilliseconds);
        values.put(DBUtil.IMAGE_SERVER_ID, server_id);
        values.put(DBUtil.IMAGE_UPLOAD_STATUS, uploadStatus);
        values.put(DBUtil.IMAGE_UPDATE_STATUS, updatedStatus);
        long insert = database.insert(DBUtil.TABLE_IMAGE_DETAIL, null, values);
        database.close();
        return insert;
    }

    public long addFreeImageDetails(String server_id, String title, String description,
                                    String imagePath, String date, String time, int uploadStatus, int updatedStatus) {


        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (uploadStatus == 1) {
            timeInMilliseconds = AppUtils.dateTimeToMillisecond(date + "T" + time);
        } else {
            timeInMilliseconds = System.currentTimeMillis();
        }


        values.put(DBUtil.IMAGE_TITLE, title);
        values.put(DBUtil.IMAGE_DESCRIPTION, description);
        values.put(DBUtil.IMAGE_PATH, imagePath);
        values.put(DBUtil.IMAGE_PATH_LOCAL, imagePath);
        values.put(DBUtil.IMAGE_DATE, AppUtils.millisecondToDate(timeInMilliseconds));
        values.put(DBUtil.IMAGE_TIME, AppUtils.millisecondToTime(timeInMilliseconds));
        values.put(DBUtil.IMAGE_TIME_MILLISECOND, timeInMilliseconds);
        values.put(DBUtil.IMAGE_SERVER_ID, server_id);
        values.put(DBUtil.IMAGE_UPLOAD_STATUS, uploadStatus);
        values.put(DBUtil.IMAGE_UPDATE_STATUS, updatedStatus);
        values.put(DBUtil.IS_FREE_IMAGE, 1);
        long insert = database.insert(DBUtil.TABLE_IMAGE_DETAIL, null, values);
        database.close();
        return insert;
    }

    public long addImageDetailsFromserver(String server_id, String title, String description,
                                          String imagePath, String date, String time, int uploadStatus) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBUtil.IMAGE_TITLE, title);
        values.put(DBUtil.IMAGE_DESCRIPTION, description);
        values.put(DBUtil.IMAGE_PATH, imagePath);
        values.put(DBUtil.IMAGE_DATE, date);
        values.put(DBUtil.IMAGE_TIME, time);
        values.put(DBUtil.IMAGE_SERVER_ID, server_id);
        values.put(DBUtil.IMAGE_UPLOAD_STATUS, uploadStatus);
        long insert = database.insert(DBUtil.TABLE_IMAGE_DETAIL, null, values);
        database.close();
        return insert;
    }


    public boolean updateImageDetails(ImageModel imagemodel) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBUtil.IMAGE_TITLE, imagemodel.getmTitle());
        values.put(DBUtil.IMAGE_DESCRIPTION, imagemodel.getmDescription());
        values.put(DBUtil.IMAGE_PATH, imagemodel.getmImagePath());
        int update = database.update(DBUtil.TABLE_IMAGE_DETAIL, values,
                DBUtil.IMAGE_ID + "=?", new String[]{String.valueOf(imagemodel.getmImageId())});
        database.close();
        return update > 0;

    }

    public boolean isImageUploaded(String imageId) {
        boolean isUploaded;
        String query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL
                + " WHERE " + DBUtil.IMAGE_ID + "=" + imageId;
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            if (cursor.getInt(cursor.getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)) == 1) {
                isUploaded = true;
            } else {
                isUploaded = false;
            }
        } else {
            isUploaded = false;
        }
        cursor.close();
        database.close();
        return isUploaded;
    }

    private boolean checkServerId(String serverId) {
        boolean status = false;
        String query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " + DBUtil.IMAGE_SERVER_ID + "=" + serverId;
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            status = true;
        } else {
            status = false;
        }
        cursor.close();
        database.close();
        return status;
    }

    public ImageListModel getImageOfId(String imageId) {
        ImageListModel imageListModel = null;
        String query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " + DBUtil.IMAGE_ID + "=" + imageId;
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            imageListModel = new ImageListModel();
            imageListModel.setmImageId(Integer.toString(cursor.getInt(cursor.getColumnIndex(DBUtil.IMAGE_ID))));
            imageListModel.setmTitle(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_TITLE)));
            imageListModel.setmDescription(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
            imageListModel.setmImagePath(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_PATH)));
            imageListModel.setmDate(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_DATE)));
            imageListModel.setmTime(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_TITLE)));
            imageListModel.setmServerId(cursor.getString(cursor.getColumnIndex(DBUtil.IMAGE_SERVER_ID)));
        }
        cursor.close();
        database.close();
        return imageListModel;
    }

    public boolean updateImageUploadStatus(String imageId, String serverId, String imagename) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBUtil.IMAGE_UPLOAD_STATUS, 1);
        values.put(DBUtil.IMAGE_UPDATE_STATUS, 0);
        values.put(DBUtil.IMAGE_SERVER_ID, serverId);
        values.put(DBUtil.IMAGE_PATH, imagename);
        int status = database.update(DBUtil.TABLE_IMAGE_DETAIL, values,
                DBUtil.IMAGE_ID + "=?", new String[]{String.valueOf(imageId)});
        database.close();
        return status > 0;
    }

    public ArrayList<ImageListModel> getUnUploadedImages() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<ImageListModel> unUploadedImages = new ArrayList<>();
        String query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " +
                DBUtil.IMAGE_UPLOAD_STATUS + "= 0";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            unUploadedImages = new ArrayList<>();
            while (cursor.moveToNext()) {
                ImageListModel model = new ImageListModel();
                model.setmImageId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_ID)));
                model.setmTitle(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TITLE)));
                model.setmDescription(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
                model.setmImagePath(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH)));
                model.setmImagePathLocal(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH_LOCAL)));
                model.setmDate(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DATE)));
                model.setmTime(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TIME)));
                model.setmServerId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_SERVER_ID)));
                model.setUploadStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)));
                model.setUpdatedStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPDATE_STATUS)));
                unUploadedImages.add(model);
            }
            cursor.close();
            database.close();
        }

        return unUploadedImages;
    }

    public ArrayList<ImageListModel> getAllImageDetails(boolean isFreeApp) {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<ImageListModel> list = null;
        String query = "";
        if (isFreeApp) {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " +
                    DBUtil.IMAGE_UPLOAD_STATUS + "= 0" + " ORDER BY " + DBUtil.IMAGE_TIME_MILLISECOND + " DESC";
        } else {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " ORDER BY " + DBUtil.IMAGE_TIME_MILLISECOND + " DESC";
        }
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                ImageListModel model = new ImageListModel();
                model.setmImageId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_ID)));
                model.setmTitle(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TITLE)));
                model.setmDescription(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
                model.setmImagePath(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH)));
                model.setmImagePathLocal(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH_LOCAL)));
                model.setmDate(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DATE)));
                model.setmTime(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TIME)));
                model.setmServerId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_SERVER_ID)));
                model.setUploadStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)));
                model.setUpdatedStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPDATE_STATUS)));
                list.add(model);
            }
            cursor.close();
            database.close();
        }

        return list;
    }

    public boolean deleteImageDetails(String mImageId) {
        SQLiteDatabase database = getWritableDatabase();
        int update = database.delete(DBUtil.TABLE_IMAGE_DETAIL,
                DBUtil.IMAGE_ID + "=?", new String[]{String.valueOf(mImageId)});
        database.close();
        return update > 0;
    }

    public ArrayList<ImageListModel> getAllImageDetailsAZ(boolean isFreeApp) {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<ImageListModel> list = null;
        String query = "";
        if (isFreeApp) {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " +
                    DBUtil.IMAGE_UPLOAD_STATUS + "= 0" + " ORDER BY " + DBUtil.IMAGE_TITLE + " ASC";
        } else {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " ORDER BY " + DBUtil.IMAGE_TITLE + " ASC";
        }
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                ImageListModel model = new ImageListModel();
                model.setmImageId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_ID)));
                model.setmTitle(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TITLE)));
                model.setmDescription(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
                model.setmImagePath(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH)));
                model.setmImagePathLocal(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH_LOCAL)));
                model.setmDate(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DATE)));
                model.setmTime(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TIME)));
                model.setmServerId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_SERVER_ID)));
                model.setUploadStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)));
                model.setUpdatedStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPDATE_STATUS)));
                list.add(model);
            }
            cursor.close();
            database.close();
        }

        return list;
    }

    public ArrayList<ImageListModel> getAllImageDetailsZA(boolean isFreeApp) {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<ImageListModel> list = null;
        String query = "";
        if (isFreeApp) {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " +
                    DBUtil.IMAGE_UPLOAD_STATUS + "= 0" + " ORDER BY " + DBUtil.IMAGE_TITLE + " DESC";
        } else {
            query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " ORDER BY " + DBUtil.IMAGE_TITLE + " DESC";
        }
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                ImageListModel model = new ImageListModel();
                model.setmImageId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_ID)));
                model.setmTitle(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TITLE)));
                model.setmDescription(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
                model.setmImagePath(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH)));
                model.setmImagePathLocal(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH_LOCAL)));
                model.setmDate(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DATE)));
                model.setmTime(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TIME)));
                model.setmServerId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_SERVER_ID)));
                model.setUploadStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)));
                model.setUpdatedStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPDATE_STATUS)));
                list.add(model);
            }
            cursor.close();
            database.close();
        }

        return list;
    }

    public ArrayList<ImageListModel> getAllImageDetailSortedByDate() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<ImageListModel> list = null;
        String query = "SELECT * FROM " + DBUtil.TABLE_IMAGE_DETAIL + " ORDER BY " + DBUtil.IMAGE_DATE + " DESC" + " , " + DBUtil.IMAGE_TIME + " DESC";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                ImageListModel model = new ImageListModel();
                model.setmImageId(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_ID)));
                model.setmTitle(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TITLE)));
                model.setmDescription(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DESCRIPTION)));
                model.setmImagePath(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_PATH)));
                model.setmDate(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_DATE)));
                model.setmTime(cursor.getString(cursor
                        .getColumnIndex(DBUtil.IMAGE_TIME)));
                model.setUploadStatus(cursor.getInt(cursor
                        .getColumnIndex(DBUtil.IMAGE_UPLOAD_STATUS)));
                list.add(model);
            }
            cursor.close();
            database.close();
        }

        return list;
    }

    public void clear() {
        SQLiteDatabase database = getReadableDatabase();
        database.execSQL("DELETE from " + DBUtil.TABLE_IMAGE_DETAIL);
        database.close();
    }

    public void clearFreeImages() {
        SQLiteDatabase database = getReadableDatabase();
        database.execSQL("DELETE from " + DBUtil.TABLE_IMAGE_DETAIL + " WHERE " +
                DBUtil.IS_FREE_IMAGE + "= 1");
        database.close();
    }
}
