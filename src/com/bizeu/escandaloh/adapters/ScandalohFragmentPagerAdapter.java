package com.bizeu.escandaloh.adapters;

import java.util.ArrayList;
import java.util.List;
import com.bizeu.escandaloh.ScandalohFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

public class ScandalohFragmentPagerAdapter extends FragmentStatePagerAdapter  {

	// Lista de fragmentos con los escándalos
    List<ScandalohFragment> fragments;
 
    
    /**
     * Constructor
     * 
     * @param fm Interfaz para interactuar con los fragmentos dentro de una actividad
     */
    public ScandalohFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<ScandalohFragment>();
    }

    
    
    /**
     * Devuelve el fragmento de una posición dada
     * @param position Posición
     */
    @Override
    public ScandalohFragment getItem(int position) {
        return this.fragments.get(position);
    }
 
    
    /**
     * Devuelve el número de fragmentos
     */
    @Override
    public int getCount() {
        return this.fragments.size();
    }
    
    
    /**
     * getItemPosition
     */
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
    
    
    /**
     * Añade un fragmento al final de la lista
     * @param fragment Fragmento a añadir
     */
    public void addFragment(ScandalohFragment fragment) {
        this.fragments.add(fragment);
    }
    
    
    /**
     * Añade un fragmento al principio de la lista
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
