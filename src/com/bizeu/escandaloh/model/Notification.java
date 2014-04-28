package com.bizeu.escandaloh.model;

public class Notification {

	private String avatar;        // Avatar del usuario de la notificación
	private String text;          // Texto de la notificación
	private String photo_id;      // ID del escándalo
	private boolean is_read;       // Indica si la notificación ha sido ya leída
	private int type; // 0: notificación de escándalo           1: notificación de usuario
	
	public Notification(int type, String text, String avatar, String photo_id, String is_read){
		this.avatar = avatar;
		this.text = text;
		this.photo_id = photo_id;
		if (is_read.equals("true")){
			this.is_read = true;
		}
		else{
			this.is_read = false;
		}	
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	public void setType(int new_type){
		type = new_type;
	}
	
	public String getAvatar(){
		return avatar;
	}

	public void setAvatar(String new_avatar){
		this.avatar = new_avatar;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String new_text){
		this.text = new_text;
	}
	
	public boolean isRead(){
		return is_read;
	} 
	
	public void setIsRead(boolean new_is_read){
		this.is_read = new_is_read;
	}
	
	public String getPhotoId(){
		return photo_id;
	}
	
	public void setPhotoId(String new_photo_id){
		this.photo_id = new_photo_id;
	}
}
