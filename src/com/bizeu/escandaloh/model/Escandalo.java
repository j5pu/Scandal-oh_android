package com.bizeu.escandaloh.model;

import android.graphics.Bitmap;

public class Escandalo {
	public static final String SERIO = "Serio";
	public static final String CONIA = "Coña";

	private String title;
	private String category;
	private Bitmap picture;
	private int num_comments;

	
	
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
	public Escandalo(String title, String category, Bitmap picture, int num_comments){
		this.title = title;
		this.category = category;
		this.picture = picture;
		this.num_comments = num_comments;
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
	
}
