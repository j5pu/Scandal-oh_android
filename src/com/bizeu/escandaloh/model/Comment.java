package com.bizeu.escandaloh.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Comment implements Parcelable {

	private String date;
	private String id;
	private String photo;
	private String resource_uri;
	private String social_network;
	private String text;
	private String user;
	private String user_id;
	private String username;

	
	public Comment(String date, String id, String photo, String resource_uri,
					String social_network, String text, String user, String user_id, String username){
		this.date = date;
		this.id = id;
		this.photo = photo;
		this.resource_uri = resource_uri;
		this.social_network = social_network;
		this.text = text;
		this.user = user;
		this.user_id = user_id;
		this.username = username;
	}
	

    public Comment(Parcel in){
        String[] data = new String[9];
        in.readStringArray(data);
        this.date = data[0];
        this.id = data[1];
        this.photo = data[2];
        this.resource_uri = data[3];
        this.social_network = data[4];
        this.text = data[5];
        this.user = data[6];
        this.user_id = data[7];
        this.username = data[8];
    }
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String new_date){
		this.date = new_date;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String new_id){
		this.id = new_id;
	}
	
	public String getPhoto(){
		return photo;
	}
	
	public void setPhoto(String new_photo){
		this.photo = new_photo;
	}
	
	public String getResourceuri(){
		return this.resource_uri;
	}
	
	public void setResourceUri(String new_resource_uri){
		this.resource_uri = new_resource_uri;
	}
	
	public String getSocialNetwork(){
		return social_network;
	}
	
	public void setSocialNetwork(String new_socialnetwork){
		this.social_network = new_socialnetwork;
	}

	public String getText(){
		return text;
	}
	
	public void setText(String new_text){
		this.text = new_text;
	}
	
	public String getUser(){
		return user;
	}
	
	public void setUser(String new_user){
		this.user = new_user;
	}

	public String getUserId(){
		return user_id;
	}
	
	public void setUserId(String new_userid){
		this.user_id = new_userid;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String new_username){
		this.username = new_username;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
        		this.date,
                this.id,
                this.photo,
                this.resource_uri,
                this.social_network,
                this.text,
                this.user,
                this.user_id,
                this.username
         });
	}
	

	
	

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in); 
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
	
}
