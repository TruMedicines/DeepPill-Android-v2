package truemedicine.logiticks.com.trumedicine.network;


public interface Urls {

    int LOGIN_URL_TAG = 1000;
    int RESET_PASSWORD_URL_TAG = 1001;
    int SIGN_UP_URL_TAG = 1002;
    int GET_ALL_USERS_URL_TAG = 1003;
    int DELETE_USERS_URL_TAG = 1004;
    int INVITE_USERS_URL_TAG = 1005;
    int IMAGE_SEARCH_URL_TAG = 1006;
    int IMAGE_UPLOAD_URL_TAG = 1007;
    int QR_CODE_URL_TAG = 1008;
    int CREATE_IMAGE_URL_TAG = 1009;
    int DELETE_IMAGE_TAG = 1010;
    int GET_ALL_IMAGES_TAG = 1011;
    int GET_ACCOUNT_DETAILS_TAG = 1012;
    int CHANGE_PASSWORD_TAG = 1013;
    int UPDATE_USER_TAG = 1014;


    int IMAGE_UPLOAD_URL_SERVICE_TAG = 1015;
    int CREATE_IMAGE_URL_SERVICE_TAG = 1016;
    int GET_ALL_IMAGES__SERVICE_TAG = 1017;
    int GET_ALL_FREE_IMAGES_TAG = 1018;

    String BASE_URL = "http://52.244.229.246:5000/";
    String BASE_URL_API = "http://52.244.229.246:5000/API/Account/";

    String LOGIN_URL = BASE_URL + "token";
    String RESET_PASSWORD_URL = BASE_URL_API + "ResetUserPassword";
    String SIGN_UP_URL = BASE_URL_API + "Register";
    String GET_ALL_USERS_URL = BASE_URL_API + "GetAllUsers";
    String DELETE_USERS_URL = BASE_URL_API + "DeleteUser";
    String INVITE_USERS_URL = BASE_URL_API + "Invite";
    String IMAGE_SEARCH_URL = BASE_URL + "ImageSearch";

    String QR_CODE_URL = BASE_URL + "Api/Image/GetBySerialNo";
    String IMAGE_UPLOAD_URL = BASE_URL + "API/Image/FileUpload";
    String CREATE_IMAGE_URL = BASE_URL + "API/Image/Create";
    String DELETE_IMAGE_URL = BASE_URL + "API/Image/Remove?id=";
    String GET_ALL_IMAGES_URL = BASE_URL + "API/Image/GetAll";
    String GET_ALL_FREE_IMAGES_URL = BASE_URL + "API/Image/GetAllInitImages";
    String GET_ACCOUNT_DETAILS_URL = BASE_URL_API + "GetCurrentUser";
    String CHANGE_PASSWORD_URL = BASE_URL_API + "ChangePassword";
    String UPDATE_USER_URL = BASE_URL_API + "UpdateUser";

    String IMAGE_BLOB_URL = "https://trumedicinesblob.blob.core.windows.net/blobs/";//"http://13.93.221.136/Blobs/";
    String IMAGE_BLOB_THUMB_URL = "http://52.244.229.246:14234/BlobsThumb100/";


}
