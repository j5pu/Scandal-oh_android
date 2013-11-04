package com.bizeu.escandaloh.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bizeu.escandaloh.ListEscandalosFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {

	final int PAGE_COUNT = 3;

	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		ListEscandalosFragment myFragment = new ListEscandalosFragment();
	        Bundle data = new Bundle();
	        data.putInt("current_page", position+1);
	        myFragment.setArguments(data);
	        return myFragment;
	}

	
	 /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
    
    
	@Override
	public CharSequence getPageTitle(int position) {
		switch(position){
			case 0:
				return "Feliz";
			case 1:
				return "Enfadado";
			case 2:
				return "Ambos";
			default:
				return null;
		}
	}
	
	
	

}
