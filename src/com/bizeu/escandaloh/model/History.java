package com.bizeu.escandaloh.model;

public class History {

	private String id;
	private String url_scandal;
	private String action;
	private String date;
	private String text;
	
	public History(String id, String url, String action, String date, String text){
		this.id = id;
		this.url_scandal = url;
		this.action = action;
		this.date = date;
		this.text = text;
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

}
