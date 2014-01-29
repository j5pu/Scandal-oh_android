package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;
import java.util.List;
import com.bizeu.escandaloh.ScandalohFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

public class ScandalohFragmentPagerAdapter2 extends FragmentStatePagerAdapter  {

	// Lista de fragmentos con los esc�ndalos
    List<ScandalohFragment> fragments;
 
    
    /**
     * Constructor
     * 
     * @param fm Interfaz para interactuar con los fragmentos dentro de una actividad
     */
    public ScandalohFragmentPagerAdapter2(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<ScandalohFragment>();
    }

    
    
    /**
     * Devuelve el fragmento de una posici�n dada
     * @param position Posici�n
     */
    @Override
    public ScandalohFragment getItem(int position) {
    	Log.v("WE","position: " + position);
        return this.fragments.get(position);
    }
 
    
    /**
     * Devuelve el n�mero de fragmentos
     */
    @Override
    public int getCount() {
    	Log.v("We","Getcount: " + this.fragments.size());
        return this.fragments.size();
    }
    
    
    /**
     * getItemPosition
     */
    /*
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
    */
    
    
    /**
     * A�ade un fragmento al final de la lista
     * @param fragment Fragmento a a�adir
     */
    public void addFragment(ScandalohFragment fragment) {
        this.fragments.add(fragment);
    }
    
    
    /**
     * A�ade un fragmento al principio de la lista
     * @param fragment
     */
    public void addFragmentAtStart(ScandalohFragment fragment){
    	this.fragments.add(0,fragment);
    }
    
    
    /**
     * Elimina todos los fragmentos
     */
    public void clearFragments(){
    	this.fragments.clear();
    }
}
