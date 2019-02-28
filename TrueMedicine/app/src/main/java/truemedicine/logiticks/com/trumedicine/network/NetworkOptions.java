package truemedicine.logiticks.com.trumedicine.network;

import volley.Request;


public interface NetworkOptions {
    /**
     * Request type
     */
    int POST_REQUEST = Request.Method.POST;
    int GET_REQUEST = Request.Method.GET;
    int PUT_REQUEST = Request.Method.PUT;
    int DELETE_REQUEST = Request.Method.DELETE;
    int HEAD_REQUEST = Request.Method.HEAD;


    /**
     * Request methods
     */
    int JSON_OBJECT_REQUEST = 0x3;
    int STRING_REQUEST = 0x4;
    int FILE_UPLOAD = 0x5;
}
