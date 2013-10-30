package com.bizeu.escandaloh.model;


import android.graphics.Bitmap;

public class Escandalo {
	public static final String HAPPY_CATEGORY = "/api/v1/category/1/";
	public static final String ANGRY_CATEGORY = "/api/v1/category/2/";
	public static final String ANGRY = "Angry";
	public static final String HAPPY = "Happy";
	
	private String id;
	private String title;
	private String category;
	private Bitmap picture;
	private int num_comments;
	private String resource_uri; // Uri de la foto
	private String route_img; // Ruta de la foto

	
	
	/**
	 * Constructor
	 */
	public Escandalo(){
		super();
	}
	
	/**
	 * Constructor por parámetros
	 * @param foto
	 * @param titulo
	 * @param numero_comentarios
	 */
	public Escandalo(String id, String title, String category, Bitmap picture, int num_comments, String resource_uri, String route_img){
		this.id = id;
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
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
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
	
}
