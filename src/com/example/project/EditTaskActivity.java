package com.example.project;

import java.lang.reflect.Method;
import java.util.Calendar;

import task.Task;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.failurereporter.R;

import database.TaskDbFacade;
import database.TaskDbHelper;

public class EditTaskActivity extends Activity {

	private EditText title;
	private EditText description;
	private DatePicker beginDate;
	private DatePicker endDate;
	private EditText addressEt;
	private EditText longitudeEt;
	private EditText latitudeEt;
	private CheckBox done;
	private long idToUpdate = -1;
	private TaskDbHelper dbOpenHelper = null;
	private TaskDbFacade dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_task);
		setupDbEnv();

		title = (EditText) findViewById(R.id.e_title_et);
		description = (EditText) findViewById(R.id.e_desc_et);
		beginDate = (DatePicker) findViewById(R.id.e_begin_datepicker);
		endDate = (DatePicker) findViewById(R.id.e_end_datepicker);
		done = (CheckBox) findViewById(R.id.e_done);
		addressEt = (EditText) findViewById(R.id.e_loc_et);
		longitudeEt = (EditText) findViewById(R.id.e_long_et);
		latitudeEt = (EditText) findViewById(R.id.e_lat_et);
		turnOffCalendar();

		if (savedInstanceState != null) {
			Log.i("lala", "title " + savedInstanceState.getString("title"));
			title.setText(savedInstanceState.getString("title"));
			description.setText(savedInstanceState.getString("description"));
			beginDate.init(savedInstanceState.getInt("byear"),
					savedInstanceState.getInt("bmonth"),
					savedInstanceState.getInt("bday"), null);
			endDate.init(savedInstanceState.getInt("eyear"),
					savedInstanceState.getInt("emonth"),
					savedInstanceState.getInt("eday"), null);
			addressEt.setText(savedInstanceState.getString("address"));
			longitudeEt.setText(savedInstanceState.getString("long"));
			latitudeEt.setText(savedInstanceState.getString("lat"));
			done.setChecked(savedInstanceState.getBoolean("done"));
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			idToUpdate = extras.getLong("id");
			title.setText(extras.getString("title"));
			description.setText(extras.getString("description"));
			done.setChecked(extras.getBoolean("done"));
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
			if (!checkDates()) {
				endDate.requestFocus();
			} 
			else if(checkIfExists()) {
				title.requestFocus();
				Toast.makeText(EditTaskActivity.this,
						"There is already a failure with such name", Toast.LENGTH_LONG)
						.show();
			} else {
				updateTask();
				goBack();
			}
			break;
		case R.id.action_cancel:
			goBack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateTask() {
		Log.i("topics", "update");
		Task task = dbHelper.getById(idToUpdate);
		task.setTitle(title.getText().toString());
		task.setDescription(description.getText().toString());
		task.setDone(task.isDone());
		task.setBeginDate(getDateInString(beginDate));
		dbHelper.update(task);
	}

	private void setupDbEnv() {
		Log.i("topics.database", "setup!");
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
				Method m = beginDate.getClass().getMethod(
						"setCalendarViewShown", boolean.class);
				m.invoke(beginDate, false);
				Method m2 = endDate.getClass().getMethod(
						"setCalendarViewShown", boolean.class);
				m2.invoke(endDate, false);
			} catch (Exception e) {
			} // eat exception in our case
		}
	}

	private boolean checkDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(beginDate.getYear(), beginDate.getMonth(),
				beginDate.getDayOfMonth());
		Calendar cal2 = Calendar.getInstance();
		cal2.set(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth());
		return (cal2.compareTo(cal) < 0) ? false : true;
	}
/*
	private void sendEmail() {
		StringBuilder body = new StringBuilder();
		body.append("NEW UNSOLVED FAILURE!").append("\n");
		body.append(title.getText().toString()).append("\n");
		body.append(description.getText().toString()).append("\n");
		body.append("Notification date: ").append(getDateInString(beginDate))
				.append("\n");
		body.append("Solution date: ").append(getDateInString(endDate))
				.append("\n");
		body.append("Status: ").append(isDone());
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, "");
		i.putExtra(Intent.EXTRA_SUBJECT, "NEW UNSOLVED FAILURE!");
		i.putExtra(Intent.EXTRA_TEXT, body.toString());
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(EditTaskActivity.this,
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void sendSms() {
		StringBuilder body = new StringBuilder();
		body.append("NEW UNSOLVED FAILURE!").append("\n");
		body.append(title.getText().toString()).append("\n");
		body.append(description.getText().toString()).append("\n");
		body.append("Notification date: ").append(getDateInString(beginDate))
				.append("\n");
		body.append("Solution date: ").append(getDateInString(endDate))
				.append("\n");
		body.append("Status: ").append(isDone());
		Intent sendSms = new Intent(Intent.ACTION_VIEW);
		sendSms.putExtra("sms_body", body.toString());
		sendSms.setType("vnd.android-dir/mms-sms");
		startActivity(sendSms);
	}
*/
	private String isDone() {
		return (done.isChecked()) ? "Done" : "In progress";
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("title", title.getText().toString());
		savedInstanceState.putString("description", description.getText()
				.toString());
		savedInstanceState.putInt("byear", beginDate.getYear());
		savedInstanceState.putInt("bmonth", beginDate.getMonth());
		savedInstanceState.putInt("bday", beginDate.getDayOfMonth());
		savedInstanceState.putInt("eyear", endDate.getYear());
		savedInstanceState.putInt("emonth", endDate.getMonth());
		savedInstanceState.putInt("eday", endDate.getDayOfMonth());
		savedInstanceState.putString("address", addressEt.getText().toString());
		savedInstanceState.putString("lat", latitudeEt.getText().toString());
		savedInstanceState.putString("long", longitudeEt.getText().toString());
		savedInstanceState.putBoolean("done", done.isChecked());
	}
	
	public boolean checkIfExists() {
		return (dbHelper.findByTitle(title.getText().toString()).isEmpty()) ? false : true;
	}
}
