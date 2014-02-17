package com.example.project;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import task.Task;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.failurereporter.R;

import database.TaskDbFacade;
import database.TaskDbHelper;

public class AddTaskActivity extends Activity {

	private EditText title;
	private EditText description;
	private DatePicker beginDate;
//	private EditText reminderDistance;
	private TaskDbHelper dbOpenHelper = null;
    private TaskDbFacade dbHelper = null;
    private Geocoder gc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_task);
		setupDbEnv();

		title = (EditText) findViewById(R.id.title_et);
		description = (EditText) findViewById(R.id.desc_et);
		beginDate = (DatePicker) findViewById(R.id.begin_datepicker);
		turnOffCalendar();
		gc = new Geocoder(this, Locale.getDefault());
		
		if(Geocoder.isPresent()){
			
			  List<Address> list;
			try {
				list = gc.getFromLocationName("Dalvik, Islandia", 1);
				if (list.isEmpty()){
					 Log.i("geo","pustta!!");
				} else {
				  Address address = list.get(0);

				  double lat = address.getLatitude();
				  double lng = address.getLongitude();
				  Log.i("geo",lat+" "+lng);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				 Log.i("geo","!!");
				e.printStackTrace();
			}

			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_task, menu);
		return true;
	}

	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
			case R.id.action_save:
				saveTask();
				goBack();
				break;
			case R.id.action_cancel:
				goBack();
				break;
	    }
	    	return super.onOptionsItemSelected(item);
	    }
	
	 public void saveTask(){
		 Log.i("topics","save");
		 Task task = new Task();
		 task.setTitle(title.getText().toString());
		 task.setDescription(description.getText().toString());
		 task.setBeginDate(getDateInString(beginDate));
		 // zmienic 
		 Log.i("topics","save1");
		 dbHelper.insert(task);
		 Log.i("topics","save2");
	 }
	 
	 private void setupDbEnv() {
		  Log.i("topics.database","setup!");
	        if (dbOpenHelper == null) {
	            dbOpenHelper = new TaskDbHelper(this);
	        } 
	        if (dbHelper == null) {
	            dbHelper = new TaskDbFacade(dbOpenHelper.getWritableDatabase());
	        }
	    }
	  
	  private void goBack() {
			Intent i = new Intent(this, OngoingActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(i);  
	  }
	  
	  private String getDateInString(DatePicker dp) {
		  StringBuilder res = new StringBuilder();
		  res.append(String.valueOf(dp.getYear()));
		  res.append(String.valueOf(dp.getMonth()));
		  res.append(String.valueOf(dp.getDayOfMonth()));
		  return res.toString();	  
	  }
	  
	  private void turnOffCalendar() {
		  int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		  if (currentapiVersion >= 11) {
		    try {
		      Method m = beginDate.getClass().getMethod("setCalendarViewShown", boolean.class);
		      m.invoke(beginDate, false);
		    }
		    catch (Exception e) {} // eat exception in our case
		  }
	  }
}
