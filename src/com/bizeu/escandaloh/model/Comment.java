package com.bizeu.escandaloh.model;

import java.util.Date;

public class Comment {

	private String text;
	private String username;
	private String date;
	
	public Comment(String text, String username, String date){
		this.text = text;
		this.username = username;
		this.date = date;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String new_text){
		this.text = new_text;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String new_username){
		this.username = new_username;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String new_date){
		this.date = new_date;
	}
}
