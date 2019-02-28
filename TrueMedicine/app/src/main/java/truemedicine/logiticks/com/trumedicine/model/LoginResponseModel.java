package truemedicine.logiticks.com.trumedicine.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponseModel {

    @SerializedName("userName")
    @Expose
    public String userName;
    @SerializedName("expires_in")
    @Expose
    public Integer expiresIn;
    @SerializedName("token_type")
    @Expose
    public String tokenType;
    @SerializedName("as:client_id")
    @Expose
    public String asClientId;
    @SerializedName(".issued")
    @Expose
    public String issued;
    @SerializedName("access_token")
    @Expose
    public String accessToken;
    @SerializedName(".expires")
    @Expose
    public String expires;

}