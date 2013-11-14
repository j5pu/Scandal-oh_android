package com.bizeu.escandaloh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bizeu.escandaloh.adapters.CommentAdapter.CommentHolder;
import com.bizeu.escandaloh.model.Comment;
import com.bizeu.escandaloh.users.MainLoginActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectCountryActivity extends Activity {

	private ListView list_countries;
	

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_countries);

		list_countries = (ListView) findViewById(R.id.list_select_countries);
		
		final ArrayList<Country> countries = new ArrayList<Country>();
		addCountries(countries);	

		final CountryArrayAdapter adapter = new CountryArrayAdapter(this,
				R.layout.country, countries);
		list_countries.setAdapter(adapter);

		list_countries.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {

				String code_selected_country = countries.get(position).getCode();
				
				// Guardamos el código del pais seleccionado
				SharedPreferences prefs = getBaseContext().getSharedPreferences(
	        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
	        	prefs.edit().putString(MyApplication.CODE_COUNTRY, code_selected_country).commit();
	        	MyApplication.CODE_SELECTED_COUNTRY = code_selected_country;
				
	        	// Mostramos la pantalla del carrusel
				Intent i = new Intent(SelectCountryActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
	
	
	private void addCountries(ArrayList<Country> countries){
		
		countries.add(new Country(getResources().getString(R.string.argentina), "AR"));
		countries.add(new Country(getResources().getString(R.string.bolivia), "BO"));
		countries.add(new Country(getResources().getString(R.string.chile), "CL"));
		countries.add(new Country(getResources().getString(R.string.colombia), "CO"));
		countries.add(new Country(getResources().getString(R.string.costa_rica), "CR"));
		countries.add(new Country(getResources().getString(R.string.cuba), "CU"));
		countries.add(new Country(getResources().getString(R.string.ecuador), "EC"));
		countries.add(new Country(getResources().getString(R.string.el_salvador), "SV"));
		countries.add(new Country(getResources().getString(R.string.espania), "ES"));
		countries.add(new Country(getResources().getString(R.string.guatemala), "GT"));
		countries.add(new Country(getResources().getString(R.string.guinea_ecuatorial), "GQ"));
		countries.add(new Country(getResources().getString(R.string.honduras), "HN"));
		countries.add(new Country(getResources().getString(R.string.mexico), "MX"));
		countries.add(new Country(getResources().getString(R.string.nicagarua), "NI"));
		countries.add(new Country(getResources().getString(R.string.panama), "PA"));
		countries.add(new Country(getResources().getString(R.string.paraguay), "PY"));
		countries.add(new Country(getResources().getString(R.string.peru), "PE"));
		countries.add(new Country(getResources().getString(R.string.puerto_rico), "PR"));
		countries.add(new Country(getResources().getString(R.string.republica_dominicana), "DO"));
		countries.add(new Country(getResources().getString(R.string.uruguay), "UY"));
		countries.add(new Country(getResources().getString(R.string.venezuela), "VE"));
	}
	
	
	private class CountryArrayAdapter extends ArrayAdapter<Country> {

	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Country> data;
	    Country country;

	    public CountryArrayAdapter(Context context, int layoutResourceId,ArrayList<Country> data) {
	      super(context, layoutResourceId, data);
	      this.context = context;
	      this.layoutResourceId = layoutResourceId;
	      this.data = data;
	    }
	    
	    
	    /**
	     * getView
	     */
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View mView = convertView;
	        
	        TextView country_name;

	        country = data.get(position);
	        
	        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        mView = inflater.inflate(layoutResourceId, parent, false);
	                                   
	        country_name = (TextView)mView.findViewById(R.id.txt_list_country);
	        country_name.setText(country.getName());
        
	        return mView;
	    }
	}
	
	
	private class Country {
		private String name;
		private String code;
		
		public Country(String name, String code){
			this.name = name;
			this.code = code;
		}
		
		public void setName(String new_name){
			this.name = new_name;
		}
		
		public String getName(){
			return this.name;
		}
		
		public void setCode(String new_code){
			this.code = new_code;
		}
		
		public String getCode(){
			return this.code;
		}
	}
}
