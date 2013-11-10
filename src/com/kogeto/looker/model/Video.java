package com.kogeto.looker.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Video implements Parcelable {

	public String videokey; //a unique identifer of the video
	public String turl; //the url of the thumbnail image
	public String vurl; //the url of the video file
	public String title; //the display name of the video
	public String duration; //in seconds 
	public String date_added;
	public String vf;
	public String description;
	public int id;
	

	
	public Video(){}
	
	
	public Video(Parcel parcel) {
		this.videokey = parcel.readString();
		this.turl = parcel.readString();
		this.vurl = parcel.readString();
		this.title = parcel.readString();
		this.duration = parcel.readString();
		this.date_added = parcel.readString();
		this.vf = parcel.readString();
		this.description = parcel.readString();
		this.id = parcel.readInt();
	}
	
	
	
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(this.videokey);
		parcel.writeString(this.turl);
		parcel.writeString(this.vurl);
		parcel.writeString(this.title);
		parcel.writeString(this.duration);
		parcel.writeString(this.date_added);
		parcel.writeString(this.vf);
		parcel.writeString(this.description);
		parcel.writeInt(id);
	}

	
	
	public static final Parcelable.Creator CREATOR =
    	new Parcelable.Creator() {
            public Video createFromParcel(Parcel parcel) {
                return new Video(parcel);
            }
 
            public Video[] newArray(int size) {
                return new Video[size];
            }
        };
        

	@Override
	public int describeContents() {
		return this.hashCode();
	}
	
}
