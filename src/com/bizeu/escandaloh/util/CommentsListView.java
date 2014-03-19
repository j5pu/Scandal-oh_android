package com.bizeu.escandaloh.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class CommentsListView extends ListView {

	public CommentsListView(Context context) {
		super(context);
	}

	public CommentsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CommentsListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Al cambiar el tama�o que se vaya al �ltimo elemento
	 * De esta forma conseguimos que al salir el teclado se vaya al �ltimo comentario
	 */
	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);		
		setSelection(this.getCount());
	}
}