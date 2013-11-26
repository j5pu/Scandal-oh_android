package com.bizeu.escandaloh;

import java.util.ArrayList;

import com.bizeu.escandaloh.RecordAudioDialog.OnMyDialogResult;
import com.bizeu.escandaloh.util.Audio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectCountryActivity extends Activity {

	private ListView list_countries;
	
	private Context context;
	

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_countries);

		context = this;
		
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

				final String code_selected_country = countries.get(position).getCode();
				String name_selected_country = countries.get(position).getName();
				
				// Mostramos un mensaje indicando que el país no se podrá cambiar en el futuro
				AlertDialog.Builder alert_country = new AlertDialog.Builder(context);
				alert_country.setTitle("País elegido: " + name_selected_country);
				alert_country
						.setMessage("Esta opción no se podrá cambiar en el futuro. ¿Estás seguro?");
				alert_country.setPositiveButton("Si",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialogo1,
									int id) {
								// Guardamos el código del pais seleccionado
								SharedPreferences prefs = getBaseContext().getSharedPreferences(
					        		      "com.bizeu.escandaloh", Context.MODE_PRIVATE);
					        	prefs.edit().putString(MyApplication.CODE_COUNTRY, code_selected_country).commit();
					        	MyApplication.code_selected_country = code_selected_country;
								
					        	// Mostramos la pantalla del carrusel
								Intent i = new Intent(SelectCountryActivity.this, MainActivity.class);
								startActivity(i);
								finish();						
							}
						});
				alert_country.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialogo1,
									int id) {
								dialogo1.cancel();
							}
						});
				alert_country.show();
			}
		});
	}
	
	
	private void addCountries(ArrayList<Country> countries){
		
		countries.add(new Country(getResources().getString(R.string.espania), "ES"));
		countries.add(new Country(getResources().getString(R.string.argentina), "AR"));
		countries.add(new Country(getResources().getString(R.string.bolivia), "BO"));
		countries.add(new Country(getResources().getString(R.string.chile), "CL"));
		countries.add(new Country(getResources().getString(R.string.colombia), "CO"));
		countries.add(new Country(getResources().getString(R.string.costa_rica), "CR"));
		countries.add(new Country(getResources().getString(R.string.cuba), "CU"));
		countries.add(new Country(getResources().getString(R.string.ecuador), "EC"));
		countries.add(new Country(getResources().getString(R.string.el_salvador), "SV"));
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
	        ImageView country_flag;

	        country = data.get(position);
	        
	        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        mView = inflater.inflate(layoutResourceId, parent, false);
	                                   
	        // Nombre 
	        country_name = (TextView)mView.findViewById(R.id.txt_list_country);
	        country_name.setText(country.getName());
	        
	        // Bandera
	        country_flag = (ImageView)mView.findViewById(R.id.img_list_country_flag);
	        String code = country.getCode();
	        if (code.equals("DO")){
	        	code = "do_republica"; // Es así porque eclipse no me deja meter una foto como "do.png"
	        }
	        int resource_id_flag = getResources().getIdentifier(code.toLowerCase(), "drawable", getPackageName());
	        country_flag.setImageResource(resource_id_flag);
        
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
