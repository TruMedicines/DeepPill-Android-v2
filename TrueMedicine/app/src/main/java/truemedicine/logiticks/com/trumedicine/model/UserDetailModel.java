package truemedicine.logiticks.com.trumedicine.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class UserDetailModel {
    @SerializedName("FirstName")
    @Expose
    public String firstName;
    @SerializedName("LastName")
    @Expose
    public String lastName;
    @SerializedName("DeviceId")
    @Expose
    public Object deviceId;
    @SerializedName("PaymentId")
    @Expose
    public Object paymentId;
    @SerializedName("RegistrationDate")
    @Expose
    public String registrationDate;
    @SerializedName("AccountType")
    @Expose
    public Integer accountType;
    @SerializedName("OwnerId")
    @Expose
    public Object ownerId;
    @SerializedName("CreatedOn")
    @Expose
    public String createdOn;
    @SerializedName("UpdatedOn")
    @Expose
    public Object updatedOn;
    @SerializedName("AccountExpiry")
    @Expose
    public String accountExpiry;
    @SerializedName("Email")
    @Expose
    public String email;
    @SerializedName("EmailConfirmed")
    @Expose
    public Boolean emailConfirmed;
    @SerializedName("SecurityStamp")
    @Expose
    public String securityStamp;
    @SerializedName("PhoneNumber")
    @Expose
    public Object phoneNumber;
    @SerializedName("PhoneNumberConfirmed")
    @Expose
    public Boolean phoneNumberConfirmed;
    @SerializedName("TwoFactorEnabled")
    @Expose
    public Boolean twoFactorEnabled;
    @SerializedName("LockoutEndDateUtc")
    @Expose
    public Object lockoutEndDateUtc;
    @SerializedName("LockoutEnabled")
    @Expose
    public Boolean lockoutEnabled;
    @SerializedName("AccessFailedCount")
    @Expose
    public Integer accessFailedCount;
    @SerializedName("Id")
    @Expose
    public String id;
    @SerializedName("UserName")
    @Expose
    public String userName;
}
