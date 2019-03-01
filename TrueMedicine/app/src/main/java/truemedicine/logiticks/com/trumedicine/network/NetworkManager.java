package truemedicine.logiticks.com.trumedicine.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import volley.AuthFailureError;
import volley.Cache;
import volley.DefaultRetryPolicy;
import volley.Request;
import volley.RequestQueue;
import volley.Response;
import volley.VolleyError;
import volley.toolbox.JsonObjectRequest;
import volley.toolbox.StringRequest;
import volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import truemedicine.logiticks.com.trumedicine.model.LoginResponseModel;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;

/**
 *
 * This class is frame work of {@link volley.toolbox.Volley} library.
 * This class provide Several methods to handle the {@link volley.toolbox.Volley} mRequest and response.
 */
public class NetworkManager implements NetworkOptions {

    /**
     * Static declarations
     */
    private static final String RESPONSE_TAG = "RESPONSE";
    private static final String REQUEST_TAG = "REQUEST";
    private static int MY_SOCKET_TIMEOUT_MS = 300000;
    private static NetworkManager mNetworkManager = null;

    private OnNetWorkListener mNetWorkListener = null;
    private OnNetWorkListener mServiceNetWorkListener = null;
    private OnNetWorkListener mSyncNetWorkListener = null;
    private boolean mIsProgressEnabled = false;
    private ProgressDialog mDialog = null;
    private JsonObjectRequest mRequest;
    private StringRequest request;
    private RequestQueue mRequestQueue;
    private Context context;

    private static final String FILE_PART_NAME = "file";

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private File mImageFile;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String twoHyphens = "--";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1024 * 1024;
    String mimeType = "multipart/form-data;boundary=" + boundary;

    /**
     * Limiting the constructor for accessing public
     *
     * @param mContext {@link Context}
     */
    private NetworkManager(Context mContext) {
        this.context = mContext;
        getRequestQueue(mContext);
    }

    /**
     * Singleton managed class
     *
     * @param context {@link Context}
     * @return It will return the Singleton instance of a {@link NetworkManager}
     */
    public static NetworkManager getInstance(Context context) {
        if (mNetworkManager == null)
            mNetworkManager = new NetworkManager(context);

        return mNetworkManager;
    }

    /**
     * Creating {@link ProgressDialog}
     *
     * @param context            {@link Context} of the {@link android.app.Activity}
     * @param message            {@link ProgressDialog#setMessage(CharSequence)}
     * @param cancelTouchOutSide {@link ProgressDialog#setCanceledOnTouchOutside(boolean)}
     */
    public void setProgressDialog(Context context, String message, boolean cancelTouchOutSide) {
        mDialog = null;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(message);
        mDialog.setCanceledOnTouchOutside(cancelTouchOutSide);
        mIsProgressEnabled = true;
        mDialog.show();

    }


    /**
     * This method sending {@link JSONObject} to server
     *
     * @param requestType Type of the mRequest <font color=black>eg: {@link NetworkOptions#POST_REQUEST}, {@link NetworkOptions#GET_REQUEST}</font>
     * @param url         Server URL
     * @param data        Requesting parameter in {@link JSONObject}
     * @param requestId   Request id for identifying the requested method
     */
    public void postUrlencodedRequest(int requestType, String url, final String data, final int requestId) {
        Log.d(REQUEST_TAG, url + data);


        mRequest = new JsonObjectRequest(requestType, url, new JSONObject(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (mNetWorkListener != null) {
                    NetworkManager.this.mNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);
                }

                Log.d(RESPONSE_TAG, response);

                if (mIsProgressEnabled) {
                    mDialog.dismiss();
                    mIsProgressEnabled = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkManager.this.mNetWorkListener.onErrorResponse(error, requestId);
            }
        }) {


            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }


            @Override
            public byte[] getBody() {
                // TODO Auto-generated method stub
                return data.getBytes();
            }
        };

        addToRequestQueue(mRequest, requestId + "");
    }

    public void getAccessToken(final int requestId) {


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("grant_type", "password");
        jsonObject.addProperty(AppConstants.SIGNIN_KEY_USERNAME, AppUtils.getStringSharedPreference(context, AppConstants.SIGNIN_KEY_USERNAME));
        jsonObject.addProperty(AppConstants.SIGNIN_KEY_PASSWORD, AppUtils.getStringSharedPreference(context, AppConstants.SIGNIN_KEY_PASSWORD));
        final String data = AppUtils.jsonToUrlEncodedString(jsonObject, "");

        Log.d(REQUEST_TAG, Urls.LOGIN_URL + data);
        JsonObjectRequest mTokenRequest = new JsonObjectRequest(NetworkOptions.POST_REQUEST, Urls.LOGIN_URL, new JSONObject(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (mNetWorkListener != null) {
                    Gson gson = new Gson();
                    LoginResponseModel loginResponseModel = gson.fromJson(response, LoginResponseModel.class);
                    AppUtils.setStringSharedPreference(context, AppConstants.KEY_ACCESS_TOKEN, loginResponseModel.accessToken);
                    AppUtils.setStringSharedPreference(context, AppConstants.KEY_TOKEN_EXPIRES, loginResponseModel.expires);
                    // NetworkManager.this.mNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, Urls.LOGIN_URL_TAG);
                    if (mRequest != null) {
                        addToRequestQueue(mRequest, requestId + "");
                    }
                }
                Log.d(RESPONSE_TAG, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getAccessToken(requestId);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public byte[] getBody() {
                // TODO Auto-generated method stub
                return data.getBytes();
            }
        };

        addToRequestQueue(mTokenRequest, Urls.LOGIN_URL_TAG + "");
    }

    /**
     * This method sending {@link JSONObject} to server
     *
     * @param requestType Type of the mRequest <font color=black>eg: {@link NetworkOptions#POST_REQUEST}, {@link NetworkOptions#GET_REQUEST}</font>
     * @param url         Server URL
     * @param jsonObject  Requesting parameter in {@link JSONObject}
     * @param requestId   Request id for identifying the requested method
     */
    public void postJsonRequest(int requestType, String url, final JSONObject jsonObject, final int requestId, final boolean needToken) {
        Log.d(REQUEST_TAG, url + jsonObject.toString());


        mRequest = new JsonObjectRequest(requestType, url, jsonObject, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                if (requestId == Urls.CREATE_IMAGE_URL_SERVICE_TAG || requestId == Urls.GET_ALL_IMAGES__SERVICE_TAG||requestId==Urls.GET_ALL_FREE_IMAGES_TAG) {
                    NetworkManager.this.mSyncNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);

                } else {
                    NetworkManager.this.mNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);
                }

                Log.d(RESPONSE_TAG, response);

                if (mIsProgressEnabled) {
                    mDialog.dismiss();
                    mIsProgressEnabled = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestId == Urls.CREATE_IMAGE_URL_SERVICE_TAG|| requestId == Urls.GET_ALL_IMAGES__SERVICE_TAG||requestId==Urls.GET_ALL_FREE_IMAGES_TAG) {
                    NetworkManager.this.mSyncNetWorkListener.onErrorResponse(error, requestId);

                } else {
                    NetworkManager.this.mNetWorkListener.onErrorResponse(error, requestId);
                }
            }
        }) {


            @Override
            public String getBodyContentType() {
                return "application/json";
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (needToken) {
                    HashMap<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + AppUtils.getStringSharedPreference(context, AppConstants.KEY_ACCESS_TOKEN));
                    return header;
                }
                return super.getHeaders();
            }
        };
        mRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        if (AppUtils.isTokenExpires(context) && needToken) {
//            getAccessToken(requestId);
//        } else {
            addToRequestQueue(mRequest, requestId + "");
//        }
    }

    public void postRequest(int requestType, String url, final String data, final int requestId, final boolean needToken) {
        Log.d(REQUEST_TAG, url + data.toString());


        mRequest = new JsonObjectRequest(requestType, url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                if (requestId == Urls.IMAGE_SEARCH_URL_TAG || requestId == Urls.QR_CODE_URL_TAG) {
                    NetworkManager.this.mServiceNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);

                } else {
                    NetworkManager.this.mNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);
                }
                Log.d(RESPONSE_TAG, response);

                if (mIsProgressEnabled) {
                    mDialog.dismiss();
                    mIsProgressEnabled = false;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestId == Urls.IMAGE_SEARCH_URL_TAG || requestId == Urls.QR_CODE_URL_TAG) {
                    NetworkManager.this.mServiceNetWorkListener.onErrorResponse(error, requestId);

                } else {
                    NetworkManager.this.mNetWorkListener.onErrorResponse(error, requestId);
                }

            }
        }) {

            @Override
            public byte[] getBody() {
                return data.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (needToken) {
                    HashMap<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + AppUtils.getStringSharedPreference(context, AppConstants.KEY_ACCESS_TOKEN));
                    return header;
                }
                return super.getHeaders();
            }
        };
//        if (AppUtils.isTokenExpires(context) && needToken) {
//            getAccessToken(requestId);
//        } else {
            addToRequestQueue(mRequest, requestId + "");
//        }
    }


    public void postRequest(int requestType, String url, final File file, final int requestId, final boolean needToken) {
        Log.d(REQUEST_TAG, url + "   " + file.getAbsolutePath());


        mImageFile = file;
        mRequest = new JsonObjectRequest(requestType, url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {


                if (requestId == Urls.IMAGE_SEARCH_URL_TAG || requestId == Urls.QR_CODE_URL_TAG) {
                    NetworkManager.this.mServiceNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);

                } else if (requestId == Urls.IMAGE_UPLOAD_URL_SERVICE_TAG) {
                    NetworkManager.this.mSyncNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);
                } else {
                    NetworkManager.this.mNetWorkListener.onResponse(response, JSON_OBJECT_REQUEST, requestId);
                }


                Log.d(RESPONSE_TAG, response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestId == Urls.IMAGE_SEARCH_URL_TAG || requestId == Urls.QR_CODE_URL_TAG) {
                    NetworkManager.this.mServiceNetWorkListener.onErrorResponse(error, requestId);

                } else if (requestId == Urls.IMAGE_UPLOAD_URL_SERVICE_TAG) {
                    NetworkManager.this.mSyncNetWorkListener.onErrorResponse(error, requestId);
                } else {
                    NetworkManager.this.mNetWorkListener.onErrorResponse(error, requestId);
                }

            }
        }) {

            @Override
            public String getBodyContentType() {
                return mimeType;
            }

            @Override
            public byte[] getBody() {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                dos = new DataOutputStream(bos);
                try {
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"file.png" + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    ByteArrayInputStream fileInputStream = new ByteArrayInputStream(readContentIntoByteArray(file));
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    return bos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (needToken) {
                    HashMap<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + AppUtils.getStringSharedPreference(context, AppConstants.KEY_ACCESS_TOKEN));
                    return header;
                }
                return super.getHeaders();
            }
        };
        mRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        if (AppUtils.isTokenExpires(context) && needToken) {
//            getAccessToken(requestId);
//        } else {
            addToRequestQueue(mRequest, requestId + "");
//        }
    }

    private static byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;

    }

    private HttpEntity buildMultipartEntity(File file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        String fileName = file.getName();
        FileBody fileBody = new FileBody(file);
        builder.addPart("onion", fileBody);

        return builder.build();
    }

    private void buildMultipartEntity() {
        mBuilder.addBinaryBody(FILE_PART_NAME, mImageFile, ContentType.create("image/jpeg"), mImageFile.getName());
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
    }

    /**
     * This method allows to specify the Request time out
     *
     * @param request {@link Request}
     */
    private void setRequestTimeout(Request request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    /**
     * Cancel the The {@link Request}
     */
    public void cancelRequest() {
        if (mRequest != null && !mRequest.isCanceled()) {
            mRequest.cancel();
        }
    }


    /**
     * Setup {@link volley.toolbox.Volley} network callback
     *
     * @param onNetworkListener {@link OnNetWorkListener}
     */
    public void setOnNetworkListener(OnNetWorkListener onNetworkListener) {
        this.mNetWorkListener = onNetworkListener;
    }

    /**
     * Setup {@link volley.toolbox.Volley} network callback
     *
     * @param onNetworkListener {@link OnNetWorkListener}
     */
    public void setOnServiceNetworkListener(OnNetWorkListener onNetworkListener) {
        this.mServiceNetWorkListener = onNetworkListener;
    }

    /**
     * Setup {@link volley.toolbox.Volley} network callback
     *
     * @param onNetworkListener {@link OnNetWorkListener}
     */
    public void setOnSyncNetworkListener(OnNetWorkListener onNetworkListener) {
        this.mSyncNetWorkListener = onNetworkListener;
    }

    /**
     * Adding {@link Request} to queue
     *
     * @param request add request to Queue.
     * @param <T>     Type of a {@link Request}
     */
    private <T> void addToRequestQueue(Request<T> request, String tag) {
        if (mRequestQueue != null) {
            if (tag.length() > 0)
                mRequest.setTag(tag);
            mRequestQueue.add(request);
        }
    }

    /**
     * Getting {@link RequestQueue}
     *
     * @param context {@link Context }
     */
    private void getRequestQueue(Context context) {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
    }

    /**
     * Setting server time out param in Millie Second default value 500 ms
     *
     * @param mySocketTimeoutMs {@link Integer} in MS
     */
    public void setMySocketTimeoutMs(int mySocketTimeoutMs) {
        MY_SOCKET_TIMEOUT_MS = mySocketTimeoutMs;
        if (mRequest != null)
            setRequestTimeout(mRequest);
    }

    /**
     * This method return cached request {@link Cache}
     * Like below you can check for a cached response of an URL before making a network call.
     *
     * @param url Requested {@link java.net.URL}
     * @return It will return the cached value {@link String}
     * @throws UnsupportedEncodingException
     * @throws NullPointerException
     */
    public String getCachedValue(String url) throws UnsupportedEncodingException, NullPointerException {
        Cache cache = mRequestQueue.getCache();
        Cache.Entry entry = cache.get(url);
        if (entry != null) {

            return new String(entry.data, "UTF-8");
        } else {
            return null;
        }
    }

    /**
     * Invalidate means we are invalidating the cached data instead of deleting it.
     * Volley will still uses the cached object until the new data received from server.
     * Once it receives the response from the server it will override the older cached response.
     *
     * @param url Requested {@link java.net.URL}
     */
    public void invalidateCache(String url) {
        if (mRequestQueue != null)
            mRequestQueue.getCache().invalidate(url, true);
    }

    /**
     * If you want disable the cache for a particular url, you can use method as below.
     * Turn off {@link Cache} functionality
     */
    public void turnOffCache() {
        if (mRequest != null)
            mRequest.setShouldCache(false);
    }

    /**
     * If you want enable the cache for a particular url, you can use method as below.
     * Turn on {@link Cache} functionality
     */
    public void turnOnCache() {
        if (mRequest != null)
            mRequest.setShouldCache(true);
    }

    /**
     * Cancel single request
     * Following will cancel all the request with the tag named.
     *
     * @param requestId Requested TAG ID
     */
    public void cancelAllRequest(int requestId) {
        mRequestQueue.cancelAll(requestId + "");
    }

    /**
     * This callback for {@link volley.toolbox.Volley} Network management
     */
    public interface OnNetWorkListener {

        /**
         * This method will return the trowed exception details from {@link volley.toolbox.Volley}
         *
         * @param error     returns {@link VolleyError}
         * @param requestId
         */
        void onErrorResponse(VolleyError error, int requestId);

        /**
         * This listener for listening {@link volley.toolbox.Volley} network response
         *
         * @param object    object From server its an instance of {@link JSONObject}
         * @param type      Type of the mRequest <font color=black>eg: {@link NetworkOptions#POST_REQUEST}, {@link NetworkOptions#GET_REQUEST}</font>
         * @param requestId Request id for identifying the requested method
         */
        void onResponse(Object object, int type, int requestId);
    }


}
