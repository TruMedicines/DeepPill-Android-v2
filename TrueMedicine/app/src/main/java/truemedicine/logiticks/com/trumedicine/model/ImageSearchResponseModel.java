package truemedicine.logiticks.com.trumedicine.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ImageSearchResponseModel {
    @SerializedName("Imageurl")
    @Expose
    public String imageurl;
    @SerializedName("Name")
    @Expose
    public String name;
    @SerializedName("Description")
    @Expose
    public String description="";
    @SerializedName("Location")
    @Expose
    public Object location;
    @SerializedName("ApplicationUserId")
    @Expose
    public String applicationUserId;
    @SerializedName("ManufactureDate")
    @Expose
    public String manufactureDate;
    @SerializedName("ExpiryDate")
    @Expose
    public String expiryDate;
    @SerializedName("ExpiryPeriod")
    @Expose
    public String expiryPeriod;
    @SerializedName("QRPrefix")
    @Expose
    public String qRPrefix;
    @SerializedName("QRSerialStart")
    @Expose
    public Integer qRSerialStart;
    @SerializedName("QRSerialEnd")
    @Expose
    public Integer qRSerialEnd;
    @SerializedName("Id")
    @Expose
    public Integer id;
    @SerializedName("CreatedOn")
    @Expose
    public String createdOn;
    @SerializedName("Percentage")
    @Expose
    public String percentage;
    @SerializedName("CreatedById")
    @Expose
    public String createdById;
    @SerializedName("UpdatedOn")
    @Expose
    public Object updatedOn;
    @SerializedName("UpdatedBy")
    @Expose
    public Object updatedBy;
    @SerializedName("DeletedOn")
    @Expose
    public Object deletedOn;
    @SerializedName("DeletedBy")
    @Expose
    public Object deletedBy;
    @SerializedName("RefId")
    @Expose
    public String refId;

}
