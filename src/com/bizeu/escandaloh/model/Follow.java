package com.bizeu.escandaloh.model;


/**
 * Un Objecto Follow puede ser un seguidor o un seguido
 *
 */
public class Follow {
	
	private String user_id;
	private String name;
	private String url_avatar;
	
	public Follow(String user_id, String name, String url_avatar){
		this.user_id = user_id;
		this.name = name;
		this.url_avatar = url_avatar;
	}
	
	public String getUserId(){
		return user_id;
	}
	
	public void setUserId(String new_user_id){
		this.user_id = new_user_id;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String new_name){
		this.name = new_name;
	}
	
	public String getUrlAvatar(){
		return url_avatar;
	}
	
	public void setUrlAvatar(String new_url_avatar){
		this.url_avatar = new_url_avatar;
	}
}
