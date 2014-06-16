package com.bizeu.escandaloh.model;

public class History {

	private String id;
	private String url_scandal;
	private String action;
	private String date;
	private String text;
	private String num_com;
	private String likes;
	private String dislikes;
	private String username;
	private String category;
	
	public History(String id, String url, String action, String date, String text, String num_com, String likes, String dislikes, String username, String category){
		this.id = id;
		this.url_scandal = url;
		this.action = action;
		this.date = date;
		this.text = text;
		this.num_com = num_com;
		this.likes = likes;
		this.dislikes = dislikes;
		this.username = username;
		this.category = category;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String new_id){
		id = new_id;
	}
	
	public String getUrl(){
		return url_scandal;
	}
	
	public void setUrl(String new_url){
		this.url_scandal = new_url;
	}
	
	public String getAction(){
		return action;
	}
	
	public void setAction(String new_action){
		this.action = new_action;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String new_date){
		this.date = new_date;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String new_text){
		this.text = new_text;
	}
	
	public String getNumComments(){
		return num_com;
	}
	
	public void setNumComments(String new_num_com){
		this.num_com = new_num_com;
	}
	
	public String getLikes(){
		return likes;
	}
	
	public void setLikes(String new_likes){
		this.likes = new_likes;
	}
	
	public String getDislikes(){
		return dislikes;
	}
	
	public void setDislikes(String new_dislikes){
		this.dislikes = new_dislikes;
	}
	
	public String getUserName(){
		return username;
	}
	
	public void setUserName(String new_username){
		this.username = new_username;
	}
	
	public String getCategory(){
		return category;
	}
	
	public void setCategory(String new_category){
		this.category = new_category;
	}

}
