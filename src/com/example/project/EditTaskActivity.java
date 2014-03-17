package com.example.project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import task.Task;
import utils.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.failurereporter.R;
import com.google.android.gms.maps.model.LatLng;

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
	
	private ScrollView myScrollView;
	private TableLayout myTableLayout;
	
	private LocationManager locationManager;
	private String provider;
	private LocationThread locThread;
	private double ltt;
	private double lgt;
	private Button getLocationButton;
	private Button getLocationByAddress;
	
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	protected static final int PHOTO_TAKEN = 101;
	protected static final int STABLE_CHILD_COUNT = 19; 
	Uri imageUri;
	List<byte[]> photos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_task);
		setupDbEnv();

		myScrollView = (ScrollView) findViewById(R.id.scrollView2);
		myTableLayout = (TableLayout) findViewById(R.id.tableLayout_edit);
		title = (EditText) findViewById(R.id.e_title_et);
		description = (EditText) findViewById(R.id.e_desc_et);
		beginDate = (DatePicker) findViewById(R.id.e_begin_datepicker);
		endDate = (DatePicker) findViewById(R.id.e_end_datepicker);
		done = (CheckBox) findViewById(R.id.e_done);
		addressEt = (EditText) findViewById(R.id.e_loc_et);
		longitudeEt = (EditText) findViewById(R.id.e_long_et);
		latitudeEt = (EditText) findViewById(R.id.e_lat_et);
		getLocationButton = (Button) findViewById(R.id.e_get_loc_b);
		getLocationByAddress = (Button) findViewById(R.id.e_get_loc_bn);
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
			for(int i = 0; i < 3; i++) {
				byte[] photo = savedInstanceState.getByteArray("photo"+i);
					if(photo != null) {
						photos.add(i, photo);
					} else {
						break;
					}
				}
				
				if(!photos.isEmpty()) {
					for(int i = 0; i < photos.size(); i++)
						createPhotoRow(photos.get(i), i+1);
				}
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Task t = (Task)extras.getSerializable("task");
			idToUpdate = t.getId();
			title.setText(t.getTitle());
			description.setText(t.getDescription());
			done.setChecked(t.isDone());
			beginDate.init(t.getStartYear(), t.getStartMonth(), t.getStartDay(), null);
			endDate.init(t.getEndYear(), t.getEndMonth(), t.getEndDay(), null);
			addressEt.setText(t.getNameOfPlace());
			longitudeEt.setText(String.valueOf(t.getLongitude()));
			latitudeEt.setText(String.valueOf(t.getLatitude()));
			photos = t.getPhotos();
			if(!photos.isEmpty()) {
				for(int i = 0; i < photos.size(); i++)
					createPhotoRow(photos.get(i), i+1);
			}
		/*	idToUpdate = extras.getLong("id");
			title.setText(extras.getString("title"));
			description.setText(extras.getString("description"));
			done.setChecked(extras.getBoolean("done"));*/
		}
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);

		getLocationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Location location = locationManager
						.getLastKnownLocation(provider);
				lgt = location.getLongitude();
				ltt = location.getLatitude();
				longitudeEt.setText(String.valueOf(lgt));
				latitudeEt.setText(String.valueOf(ltt));
				if (locThread != null)
					locThread.interrupt();
				locThread = new LocationThread(Utils.GET_FROM_LAT_AND_LNG);
				locThread.start();
			}
		});

		getLocationByAddress.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("koko", "lol jest ich "+myTableLayout.getChildCount());
				String address = addressEt.getText().toString();
				locThread = new LocationThread(Utils.GET_FROM_ADDRESS, address);
				locThread.start();
			}
		});

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
				suggestReport();
			}
			break;
		case R.id.action_cancel:
			sureToCancel();
			break;
		case R.id.action_camera:
			if (photos.size()<3){
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			} else {
				focusOnView();
			}
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
		task.setEndDate(getDateInString(endDate));
		task.setPhotos(photos);
		task.setLongitude(longitudeEt.getText().toString());
		task.setLatitude(latitudeEt.getText().toString());
		task.setNameOfPlace(addressEt.getText().toString());
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
		//startActivity(sendSms);
		startActivityForResult(sendSms, PHOTO_TAKEN);
	}

	private void suggestReport() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton("Sms", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sendSms();
			}
		});
		builder.setNeutralButton("E-mail",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						sendEmail();
					}
				});
		builder.setNegativeButton("Not now",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						goBack();
					}
				});
		builder.setMessage("Quick report?");
		// Set other dialog properties
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void sureToCancel() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	// Add the buttons
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   goBack();
    	           }
    	       });
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	           }
    	       });
    	builder.setMessage("Sure to cancel? Changes won't be saved");
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
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
	
	class LocationThread extends Thread {
		String address = "";
		int action = -1;

		public LocationThread() {
		}

		public LocationThread(int action) {
			this.action = action;
		}

		public LocationThread(String address) {
			this.address = address;
			Log.i("gowno", address);
		}

		public LocationThread(int action, String address) {
			this.address = address;
			this.action = action;
		}

		public void run() {
			Log.i("gowno", "looooooooo " + address);
			switch (action) {
			case Utils.GET_FROM_ADDRESS:
				try {
					final LatLng p = Utils.getLocationFromString(address);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							longitudeEt.setText(String.valueOf(p.longitude));
							latitudeEt.setText(String.valueOf(p.latitude));
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case Utils.GET_FROM_LAT_AND_LNG:
				Log.i("gowno", "lat " + latitudeEt.getText().toString());

				try {
					final String a = Utils.getStringFromLocation(ltt, lgt)
							.get(0).getAddressLine(0);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							addressEt.setText(a);

						}
					});
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		}
	}
	
	
	
	private final void focusOnView(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                myScrollView.scrollTo(0, myTableLayout.getChildAt(myTableLayout.getChildCount()-1).getBottom());
            }
        });
    }
	
	private void createPhotoRow(byte[] photoBytes, int n) {
		final TableRow tr = new TableRow(this);
		
		Bitmap picture = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        ImageView photo = new ImageView(this);
        photo.setPadding(5, 2, 5, 2);
        photo.setImageBitmap(picture);
        ImageButton imageDeleteButton = new ImageButton(this);
     //   imageDeleteButton.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        imageDeleteButton.setBackgroundResource(R.drawable.ic_action_discard);
        imageDeleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				int currentChildCount = myTableLayout.getChildCount();
				Log.i("koko"," ccc "+currentChildCount);
				int id = tr.getId();
				Log.i("koko","kliknelam "+id);
				photos.remove(id-STABLE_CHILD_COUNT-1);
				for (int i = id; i<= currentChildCount; i++) {
					Log.i("koko","for "+i+ " ccc "+currentChildCount);
					View view = myTableLayout.getChildAt(i-1);
					if (view == null)
						Log.i("koko","o matko to jest null :(");
					int oldId = view.getId();
					oldId -= 1;
					view.setId(oldId);
				}
				View viewUp = tr.focusSearch(View.FOCUS_UP);
				myTableLayout.removeView(tr);
				viewUp.requestFocus();
			}
		});
        tr.addView(photo);
        tr.addView(imageDeleteButton);
        tr.setId(STABLE_CHILD_COUNT+n);
        myTableLayout.addView(tr);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (locThread != null) {
			locThread.interrupt();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PHOTO_TAKEN) {
			goBack();
		}
		
		if (resultCode == RESULT_OK) {
			if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				Bundle extras = data.getExtras();
				
				if(extras.get("data") == null) Log.i("lol","data to debil");
				else Log.i("lol","gesher");
				Bitmap picture = (Bitmap) extras.getParcelable("data");
		        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		        byte[] byteArray = stream.toByteArray();
		        photos.add(byteArray);
		        
		        Log.i("koko","przed "+myTableLayout.getChildCount());
		        final TableRow tr = new TableRow(this);
		        ImageView photo = new ImageView(this);
		        photo.setPadding(2, 2, 2, 2);
		        photo.setImageBitmap(picture);
		        ImageButton imageDeleteButton = new ImageButton(this);
		        imageDeleteButton.setBackgroundResource(R.drawable.ic_action_discard);

		        imageDeleteButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					
						int currentChildCount = myTableLayout.getChildCount();
						Log.i("koko"," ccc "+currentChildCount);
						int id = tr.getId();
						Log.i("koko","kliknelam "+id);
						photos.remove(id-STABLE_CHILD_COUNT-1);
						for (int i = id; i<= currentChildCount; i++) {
							Log.i("koko","for "+i+ " ccc "+currentChildCount);
							View view = myTableLayout.getChildAt(i-1);
							if (view == null)
								Log.i("koko","o matko to jest null :(");
							int oldId = view.getId();
							oldId -= 1;
							view.setId(oldId);
						}
						View viewUp = tr.focusSearch(View.FOCUS_UP);
						myTableLayout.removeView(tr);
						viewUp.requestFocus();
					}
				});
		        tr.addView(photo);
		        tr.addView(imageDeleteButton);
		        tr.setId(STABLE_CHILD_COUNT+photos.size());
		        myTableLayout.addView(tr);
		      /*  if (photos.size()==3){
	        	//	button_camera.setEnabled(false);
	        		photo3.addView(photo);
	        	} else if (photos.size()==2){
	        		photo2.addView(photo);
	        	} else if (photos.size()==1){
	        		photo1.addView(photo);
	        	} else {
	        		Log.i("OLAG", "Pustki fotograficzne, a tak byæ nie powinno oO");
	        	}*/
		        
		        
		        //delete image
		      //  this.getContentResolver().delete(data.getData(), null, null);
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
			}
		}

	}
}
