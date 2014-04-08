/**
 * 
 */
package org.apache.cordova.datepicker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

public class EIDADatePickerDialog extends DatePickerDialog {
	
	private CharSequence title;
	
	public EIDADatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }
	
	public void setPermanentTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }
	
	@Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        setTitle(title);
    }
}