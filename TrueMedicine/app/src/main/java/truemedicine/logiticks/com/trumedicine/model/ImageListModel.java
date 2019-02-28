package truemedicine.logiticks.com.trumedicine.model;

import java.io.Serializable;

public class ImageListModel implements Serializable{
	
	private String mTitle = "";
	private String mDescription = "";
	private String mImageId = "";
	private String mImagePath = "";
	private String mImagePathLocal = "";
	private String mDate = "";
	private String mTime = "";
	private String mServerId = "";
	private int uploadStatus=0;
	private int updatedStatus=0;

	public void setUpdatedStatus(int updatedStatus) {
		this.updatedStatus = updatedStatus;
	}

	public int getUpdatedStatus() {
		return updatedStatus;
	}

	public void setmImagePathLocal(String mImagePathLocal) {
		this.mImagePathLocal = mImagePathLocal;
	}

	public String getmImagePathLocal() {
		return mImagePathLocal;
	}

	public void setUploadStatus(int uploadStatus) {
		this.uploadStatus = uploadStatus;
	}

	public int getUploadStatus() {
		return uploadStatus;
	}

	public String getmDate() {
		return mDate;
	}

	public void setmDate(String mDate) {
		this.mDate = mDate;
	}

	public String getmTime() {
		return mTime;
	}

	public void setmTime(String mTime) {
		this.mTime = mTime;
	}

	public String getmTitle() {
		return mTitle;
	}
	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	public String getmDescription() {
		return mDescription;
	}
	public void setmDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	public String getmImageId() {
		return mImageId;
	}
	public void setmImageId(String mImageId) {
		this.mImageId = mImageId;
	}
	public String getmImagePath() {
		return mImagePath;
	}
	public void setmImagePath(String mImagePath) {
		this.mImagePath = mImagePath;
	}

	public String getmServerId() {
		return mServerId;
	}

	public void setmServerId(String mServerId) {
		this.mServerId = mServerId;
	}
}
