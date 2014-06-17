package com.bizeu.escandaloh.model;

public class Search {

	private String photo_id;
	private String url_scandal;
	private String title;
	private String username;
	private String date;
	private String likes;
	private String dislikes;
	private String category;
	private String num_comments;
	
	public Search(String id_scandal, String url_scandal, String title, String username, String date, String likes, String dislikes, String category, String num_comments){
		this.photo_id = id_scandal;
		this.url_scandal = url_scandal;
		this.title = title;
		this.username = username;
		this.date = date;
		this.likes = likes;
		this.dislikes = dislikes;
		this.category = category;
		this.num_comments = num_comments;
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
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String new_date){
		this.date = new_date;
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
	
	public String getCategory(){
		return category;
	}
	
	public void setCategory(String new_category){
		this.category = new_category;
	}
	
	public String getNumComments(){
		return num_comments;
	}
	
	public void setNumComments(String new_num_comments){
		this.num_comments = new_num_comments;
	}
}
