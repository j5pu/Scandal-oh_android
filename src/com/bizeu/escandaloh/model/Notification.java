package com.bizeu.escandaloh.model;

public class Notification {

	private String avatar;     // Avatar del usuario de la notificación
	private String scandal;    // Escándalo de la notificación
	private String text;       // Texto de la notificación
	private String photo_id;   // ID del escándalo
	private boolean is_read;   // Indica si la notificación ha sido ya leída
	private int type; 		   // Tipo de notificación {1,..,9} --> El tipo 6 es la notificación "Tal user te está siguiendo"
	private String date;
	
	public Notification(int type, String text, String avatar, String scandal, String photo_id, String is_read, String date){
		this.avatar = avatar;
		this.scandal = scandal;
		this.text = text;
		this.photo_id = photo_id;
		if (is_read.equals("true")){
			this.is_read = true;
		}
		else{
			this.is_read = false;
		}	
		this.type = type;
		this.date = date;
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
	
	public String getScandal(){
		return scandal;
	}
	
	public void setScandal(String new_scandal){
		this.scandal = new_scandal;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String new_text){
		this.text = new_text;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String new_date){
		this.date = new_date;
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
