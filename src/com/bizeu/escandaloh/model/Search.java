package com.bizeu.escandaloh.model;

public class Search {

	private String photo_id;
	private String url_scandal;
	private String title;
	private String username;
	
	public Search(String id_scandal, String url_scandal, String title, String username){
		this.photo_id = id_scandal;
		this.url_scandal = url_scandal;
		this.title = title;
		this.username = username;
	}
	
	
	public String getPhotoId(){
		return photo_id;
	}
	
	public void setIdScandal(String new_id){
		photo_id = new_id;
	}
	
	public String getUrlScandal(){
		return url_scandal;
	}
	
	public void setUrlScandal(String new_url){
		this.url_scandal = new_url;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String new_title){
		title = new_title;
	}
	
	public String getUserName(){
		return username;
	}
	
	public void setUserName(String new_username){
		this.username = new_username;
	}
}
