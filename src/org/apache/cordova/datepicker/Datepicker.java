/**
 * 
 */
package org.apache.cordova.datepicker;

import java.util.Calendar;
import java.util.Date;
import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

/**
 * @author ng4e
 * @author Daniel van 't Oever
 * 
 *         Rewrote plugin so it it similar to the iOS datepicker plugin and it
 *         accepts prefilled dates and time
 */
public class Datepicker extends CordovaPlugin {

	private static final String ACTION_DATE = "date";
	private static final String ACTION_TIME = "time";
	private final String pluginName = "Datepicker";
	private EIDADatePickerDialog dpd = null;
    @Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		Log.d(pluginName, "Datepicker called with options: " + args);

		return this.show(args, callbackContext);
	}

	public synchronized boolean show(final JSONArray data, final CallbackContext callbackContext) {
		final Calendar c = Calendar.getInstance();
		final Runnable runnable;
		final Context currentCtx = cordova.getActivity();
		final Datepicker datePickerPlugin = this;

		String action = "date";
		boolean mIsFullDate = false;

		/*
		 * Parse information from data parameter and where possible, override
		 * above date fields
		 */
		int month = -1, day = -1, year = -1, hour = -1, min = -1;
		try {
			JSONObject obj = data.getJSONObject(0);
			action = obj.getString("mode");
			mIsFullDate = obj.getBoolean("isFullDate");
			System.out.println("###mIsFullDate:"+mIsFullDate);
			String optionDate = obj.getString("date");

			String[] datePart = optionDate.split("/");
			month = Integer.parseInt(datePart[0]);
			day = Integer.parseInt(datePart[1]);
			year = Integer.parseInt(datePart[2]);
			hour = Integer.parseInt(datePart[3]);
			min = Integer.parseInt(datePart[4]);

			/* currently not handled in Android */
			// boolean optionAllowOldDates = obj.getBoolean("allowOldDates");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// By default initalize these fields to 'now'
		final int mYear = year == -1 ? c.get(Calendar.YEAR) : year;
		final int mMonth = month == -1 ? c.get(Calendar.MONTH) : month - 1;
		final int mDay = day == -1 ? c.get(Calendar.DAY_OF_MONTH) : day;
		final int mHour = hour == -1 ? c.get(Calendar.HOUR_OF_DAY) : hour;
		final int mMinutes = min == -1 ? c.get(Calendar.MINUTE) : min;
		final boolean mIsFullDateFinal = mIsFullDate;

		if (ACTION_TIME.equalsIgnoreCase(action)) {
			runnable = new Runnable() {
				public void run() {
					final TimeSetListener timeSetListener = new TimeSetListener(datePickerPlugin, callbackContext);
					final TimePickerDialog timeDialog = new TimePickerDialog(currentCtx, timeSetListener, mHour,
							mMinutes, true);
					timeDialog.show();
				}
			};

		} else if (ACTION_DATE.equalsIgnoreCase(action)) {
			runnable = new Runnable() {
				public void run() {
					final DateSetListener dateSetListener = new DateSetListener(datePickerPlugin, callbackContext);
					//final DatePickerDialog dateDialog = new DatePickerDialog(currentCtx, dateSetListener, mYear,
					//		mMonth, mDay);
					dpd =  createDialogWithoutDateField(currentCtx, dateSetListener, mYear,mMonth, mDay, mIsFullDateFinal);
					//Additions
					 dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dateDialog, int which) {
				               if (which == DialogInterface.BUTTON_NEGATIVE) {                 
				                  dateDialog.dismiss();
				               }
				            }
				          });
					dpd.show();
				}
			};

		} else {
			Log.d(pluginName, "Unknown action. Only 'date' or 'time' are valid actions");
			return false;
		}

		cordova.getActivity().runOnUiThread(runnable);
		return true;
	}

	private EIDADatePickerDialog createDialogWithoutDateField(Context context, OnDateSetListener callBack ,int year, int monthofYear, int dayofMonth, boolean isFullDate){
		
		//dpd = new DatePickerDialog(context, callBack,year,monthofYear, dayofMonth);
		dpd = new EIDADatePickerDialog(context, callBack,year,monthofYear, dayofMonth);		
	
		try{
			DatePicker datePicker = dpd.getDatePicker();
			Date today = new Date();
		
			Field datePickerFields[] = datePicker.getClass().getDeclaredFields();
			if(isFullDate){
				dpd.setPermanentTitle("Pick a date");
				for (Field datePickerField : datePickerFields) {
					datePickerField.setAccessible(true);
					Object dayPicker = new Object();
					dayPicker = datePickerField.get(datePicker);
					((View) dayPicker).setVisibility(View.GONE);
				}
			}
			else{
				dpd.setPermanentTitle("Pick an expiry date");
				for (Field datePickerField : datePickerFields) {
					if ("mDaySpinner".equals(datePickerField.getName())) {
						datePickerField.setAccessible(true);
						Object dayPicker = new Object();
						dayPicker = datePickerField.get(datePicker);
						((View) dayPicker).setVisibility(View.GONE);
					}
				}
			}
			datePicker.setMinDate(today.getTime());
		}
		catch(Exception ex){
		}
		return dpd;
	}
	
	private final class DateSetListener implements OnDateSetListener {
		private final Datepicker datePickerPlugin;
		private final CallbackContext callbackContext;

		private DateSetListener(Datepicker datePickerPlugin, CallbackContext callbackContext) {
			this.datePickerPlugin = datePickerPlugin;
			this.callbackContext = callbackContext;
		}

		/**
		 * Return a string containing the date in the format YYYY/MM/DD
		 */
		public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
			dpd.setTitle("Pick a date");
			String returnDate = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;
			callbackContext.success(returnDate);
		}
	}

	private final class TimeSetListener implements OnTimeSetListener {
		private final Datepicker datePickerPlugin;
		private final CallbackContext callbackContext;

		private TimeSetListener(Datepicker datePickerPlugin, CallbackContext callbackContext) {
			this.datePickerPlugin = datePickerPlugin;
			this.callbackContext = callbackContext;
		}

		/**
		 * Return the current date with the time modified as it was set in the
		 * time picker.
		 */
		public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
			Date date = new Date();
			date.setHours(hourOfDay);
			date.setMinutes(minute);

			callbackContext.success(date.toLocaleString());
		}
	}
}

