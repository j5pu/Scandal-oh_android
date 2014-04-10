package com.bizeu.escandaloh.model;

public class Notification {

	private String avatar;        // Avatar del usuario de la notificaci�n
	private String text;          // Texto de la notificaci�n
	private String photo_id;      // ID del esc�ndalo
	private boolean is_read;       // Indica si la notificaci�n ha sido ya le�da
	
	public Notification(String text, String avatar, String photo_id, String is_read){
		this.avatar = avatar;
		this.text = text;
		this.photo_id = photo_id;
		if (is_read.equals(0)){
			this.is_read = true;
		}
		else{
			this.is_read = false;
		}	
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
