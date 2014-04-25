package com.bizeu.escandaloh.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Scandaloh {
	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final String ANGRY = "Angry";
	public static final String HAPPY = "Happy";
	
	private String id;
	private String user_id;
	private String title;
	private String category;
	private Bitmap picture;
	private String picture_url;
	private int num_comments;
	private String resource_uri; // Uri de la foto
	private String route_img; // Ruta de la foto pequeña sin marca de agua
	private String route_img_big; // Ruta de lda foto grande con marca de agua
	private String resource_audio; // Uri del audio. Null es que la imagen no tiene audio
	private boolean has_audio;
	private String date;
	private String user;
	private String avatar;
	private Comment last_comment; // Comentario más reciente
	private String social_network;
	private int already_voted;
	private int likes;
	private int dislikes;
	// Noticias
	private int media_type; // 0:escándalo   1:noticia
	private String favicon;
	private String source;
	private String source_name;
	

	
	
	/**
	 * Constructor
	 */
	public Scandaloh(){
		super();
	}
	
	/**
	 * Constructor por parámetros
	 * @param foto
	 * @param titulo
	 * @param numero_comentarios
	 */
	public Scandaloh(String id, String user_id, String title, String category, Bitmap picture, int num_comments, 
						String resource_uri, String route_img, String route_img_big, String uri_audio,
						String user, String date, String avatar, Comment last_comment, String social_network,
						int already_voted, int likes, int dislikes, int media_type, String favicon, String source, 
						String source_name){
		this.id = id;
		this.user_id = user_id;
		this.title = title;
		if (category.equals(HAPPY_CATEGORY)){
	        	this.category = HAPPY;
		}
		else{
			this.category = ANGRY;
		}
		this.picture = picture;
		this.num_comments = num_comments;
		this.resource_uri = resource_uri;
		this.route_img = route_img;
		this.route_img_big = route_img_big;
		this.resource_audio = uri_audio;
		this.has_audio = uri_audio.equals("null") ? false : true;
		this.user = user;
		this.date = date;
		this.avatar = avatar;
		this.last_comment = last_comment;
		this.social_network = social_network;
		this.already_voted = already_voted;
		this.likes = likes;
		this.dislikes = dislikes;
		this.media_type = media_type;
		this.favicon = favicon;
		this.source = source;
		this.source_name = source_name;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public void setUserId(String new_user_id){
		this.user_id = new_user_id;
	}
	
	public String getUserId(){
		return user_id;
	}
	
	
	public void setTitle(String new_title){
		this.title = new_title;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setCategory(String new_category){
		this.category = new_category;
	}
	
	public String getCategory(){
		return category;
	}
	
	public void setPicture(Bitmap new_picture){
		this.picture = new_picture;
	}
	
	public Bitmap getPicture(){
		return picture;
	}
	
	public void setNumComments(int new_num_comments){
		this.num_comments = new_num_comments;
	}
	
	public int getNumComments(){
		return num_comments;
	}
	
	public void setResourceUri(String new_resource){
		this.resource_uri = new_resource;
	}
	
	public String getResourceUri(){
		return this.resource_uri;
	}
	
	public void setRouteImg(String new_route){
		this.route_img = new_route;
	}
	
	public String getRouteImg(){
		return this.route_img;
	}
	
	public void setRouteImgBig(String new_route_big){
		this.route_img_big = new_route_big;
	}
	
	public String getRouteImgBig(){
		return this.route_img_big;
	}
	
	public void setUriAudio(String new_uri_audio){
		this.resource_audio = new_uri_audio;
	}
	
	public String getUriAudio(){
		return this.resource_audio;
	}
	
	public void setHasAudio(boolean new_has_audio){
		this.has_audio = new_has_audio;
	}
	
	public boolean hasAudio(){
		return this.has_audio;
	}	
	
	public void setPictureUrl(String new_image_url){
		this.picture_url = new_image_url;
	}
	
	public String getPictureUrl(){
		return this.picture_url;
	}
	
	public void setUser(String new_user){
		this.user = new_user;
	}
	
	public String getUser(){
		return this.user;
	}
	
	public void setDate(String new_date){	
	}
	
	public String getDate(){
		return this.date;
	}
	
	public Comment getLastComment(){
		return this.last_comment;
	}
	
	public void setLastComment(Comment new_last_comment){
		this.last_comment = new_last_comment;
	}
	
	public String getAvatar(){
		return this.avatar;
	}
	
	public void setAvatar(String new_avatar){
		this.avatar = new_avatar;
	}
	
	public String getSocialNetwork(){
		return this.social_network;
	}
	
	public void setSocialNetwork(String new_social_network){
		this.social_network = new_social_network;
	}
	
	public int getAlreadyVoted(){
		return this.already_voted;
	}
	
	public void setAlreadyVoted(int new_already_voted){
		this.already_voted = new_already_voted;
	}
	
	public int getLikes(){
		return this.likes;
	}
	
	public void setLikes(int new_likes){
		this.likes = new_likes;
	}
	
	public int getDislikes(){
		return this.dislikes;
	}
	
	public void setDislikes(int new_dislikes){
		this.dislikes = new_dislikes;
	}
	
	public int getMediaType(){
		return this.media_type;
	}
	
	public void setMediaType(int new_media_type){
		this.media_type = new_media_type;
	}
	
	public String getFavicon(){
		return this.favicon;
	}
	
	public void setFavicon(String new_favicon){
		this.favicon = new_favicon;
	}
	
	public String getSource(){
		return this.source;
	}
	
	public void setSource(String new_source){
		this.source = new_source;
	}
	
	public String getSourceName(){
		return this.source_name;
	}
	
	public void setSourceName(String new_source_name){
		this.source_name = new_source_name;
	}
	
}
