package com.example.project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import utils.GeoUtils;
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
import android.provider.MediaStore.Images;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.failurereporter.R;
import com.google.android.gms.maps.model.LatLng;

import database.FailureDbFacade;
import database.FailureDbHelper;
import failure.Failure;

public class EditFailureActivity extends Activity {

	private EditText title;
	private EditText description;
	private DatePicker beginDate;
	private DatePicker endDate;
	private EditText addressEt;
	private EditText longitudeEt;
	private EditText latitudeEt;
	private CheckBox done;
	private long idToUpdate = -1;
	private FailureDbHelper dbOpenHelper = null;
	private FailureDbFacade dbHelper = null;
	
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
	protected static final int BACK_FROM_OUTSIDE_APP = 101;
	protected static final int STABLE_CHILD_COUNT = 1; 
	Uri imageUri;
	private TextView photosTv;
	List<byte[]> photos;
	
	String originalName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_failure);
		setupDbEnv();

		Log.e("onCreate","onCreate");
		myScrollView = (ScrollView) findViewById(R.id.scrollView1);
		myTableLayout = (TableLayout) findViewById(R.id.tableLayout_add);
		title = (EditText) findViewById(R.id.title_et);
		description = (EditText) findViewById(R.id.desc_et);
		beginDate = (DatePicker) findViewById(R.id.begin_datepicker);
		endDate = (DatePicker) findViewById(R.id.end_datepicker);
		endDate.setMaxDate(new Date().getTime());
		done = (CheckBox) findViewById(R.id.done);
		addressEt = (EditText) findViewById(R.id.loc_et);
		longitudeEt = (EditText) findViewById(R.id.long_et);
		latitudeEt = (EditText) findViewById(R.id.lat_et);
		getLocationButton = (Button) findViewById(R.id.get_loc_b);
		getLocationByAddress = (Button) findViewById(R.id.get_loc_bn);
		turnOffCalendar();
		photosTv = (TextView) findViewById(R.id.photos_tv);
		photos = new ArrayList<byte[]>();

		if (savedInstanceState != null) {
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
			if (photos.isEmpty()) {
			for(int i = 0; i < 3; i++) {
				byte[] photo = savedInstanceState.getByteArray("photo"+i);
					if(photo != null) {
						photos.add(i, photo);
					} else {
						break;
					}
				}
				
				if(!photos.isEmpty()) {
					Log.e("jest","dodaje w tym drugim");
					photosTv.setVisibility(View.VISIBLE);
					for(int i = 0; i < photos.size(); i++)
						createPhotoRow(photos.get(i), i+1);
				}
				Log.i("koko","liczba dzieci "+myTableLayout.getChildCount());
			}
		} else {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Log.e("jest","extras");
			Failure t = (Failure)extras.getSerializable("failure");
			originalName = t.getTitle();
			idToUpdate = t.getId();
			title.setText(t.getTitle());
			description.setText(t.getDescription());
			done.setChecked(t.isDone());
			beginDate.init(t.getStartYear(), t.getStartMonth(), t.getStartDay(), null);
			endDate.init(t.getEndYear(), t.getEndMonth(), t.getEndDay(), null);
			addressEt.setText(t.getNameOfPlace());
			longitudeEt.setText(String.valueOf(t.getLongitude()));
			latitudeEt.setText(String.valueOf(t.getLatitude()));
			if (photos.isEmpty()) {
			photos = t.getPhotos();
			if(!photos.isEmpty()) {
				Log.e("jest","dodaje w extras");
				for(int i = 0; i < photos.size(); i++)
					createPhotoRow(photos.get(i), i+1);
			}
			}
		}
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
				locThread = new LocationThread(GeoUtils.GET_FROM_LAT_AND_LNG);
				locThread.start();
			}
		});

		getLocationByAddress.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("koko", "lol jest ich "+myTableLayout.getChildCount());
				String address = addressEt.getText().toString();
				locThread = new LocationThread(GeoUtils.GET_FROM_ADDRESS, address);
				locThread.start();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_failure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_save:
			if (!checkDates()) {
				endDate.requestFocus();
				Toast.makeText(EditFailureActivity.this,
						"End date cannot precede begin date!", Toast.LENGTH_LONG)
						.show();
			} 
			else if(checkIfExists()) {
				title.requestFocus();
				Toast.makeText(EditFailureActivity.this,
						"There is already a failure with such name", Toast.LENGTH_LONG)
						.show();
			} else {
				updateFailure();
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

	public void updateFailure() {
		Log.i("topics", "update");
		Failure f = dbHelper.getById(idToUpdate);
		f.setTitle(title.getText().toString());
		f.setDescription(description.getText().toString());
		f.setDone(f.isDone());
		f.setBeginDate(Utils.getDateInString(beginDate));
		f.setEndDate(Utils.getDateInString(endDate));
		f.setPhotos(photos);
		f.setLongitude(longitudeEt.getText().toString());
		f.setLatitude(latitudeEt.getText().toString());
		f.setNameOfPlace(addressEt.getText().toString());
		dbHelper.update(f);
	}

	private void setupDbEnv() {
		Log.i("topics.database", "setup!");
		if (dbOpenHelper == null) {
			dbOpenHelper = new FailureDbHelper(this);
		}
		if (dbHelper == null) {
			dbHelper = new FailureDbFacade(dbOpenHelper.getWritableDatabase());
		}
	}

	private void goBack() {
		Intent i = new Intent(this, OngoingActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
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
		if (done.isChecked())
			body.append("UPDATED INFO ABOUT SOLVED FAILURE!").append("\n");
		else
			body.append("UPDATED INFO ABOUT UNSOLVED FAILURE!").append("\n");
		body.append(title.getText().toString()).append("\n");
		body.append(description.getText().toString()).append("\n");
		body.append("Notification date: ").append(Utils.getDateInString(beginDate))
				.append("\n");
		body.append("Solution date: ").append(Utils.getDateInString(endDate))
				.append("\n");
		body.append("Status: ").append(isDone());
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		if(!photos.isEmpty()) {
			for (int j = 0; j < photos.size(); j++) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(photos.get(j), 0, photos.get(j).length);
				String pathofBmp = Images.Media.insertImage(getContentResolver(), bitmap,"photo"+j, null);
				Uri bmpUri = Uri.parse(pathofBmp);
				i.putExtra(Intent.EXTRA_STREAM, bmpUri);
			}
		}
		i.putExtra(Intent.EXTRA_EMAIL, "");
		i.putExtra(Intent.EXTRA_SUBJECT, "UPDATED INFO ABOUT "+title.getText().toString().toUpperCase()+"!");
		i.putExtra(Intent.EXTRA_TEXT, body.toString());
		try {
			startActivityForResult(Intent.createChooser(i, "Send mail..."), BACK_FROM_OUTSIDE_APP);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(EditFailureActivity.this,
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void sendSms() {
		StringBuilder body = new StringBuilder();
		if (done.isChecked())
			body.append("UPDATED INFO ABOUT SOLVED FAILURE!").append("\n");
		else
			body.append("UPDATED INFO ABOUT UNSOLVED FAILURE!").append("\n");
		body.append(title.getText().toString()).append("\n");
		body.append(description.getText().toString()).append("\n");
		body.append("Notification date: ").append(Utils.getDateInString(beginDate))
				.append("\n");
		body.append("Solution date: ").append(Utils.getDateInString(endDate))
				.append("\n");
		body.append("Status: ").append(isDone());
		Intent sendSms = new Intent(Intent.ACTION_VIEW);
		sendSms.putExtra("sms_body", body.toString());
		sendSms.setType("vnd.android-dir/mms-sms");
		//startActivity(sendSms);
		startActivityForResult(sendSms, BACK_FROM_OUTSIDE_APP);
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
    	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   goBack();
    	           }
    	       });
    	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
		for (int i = 0; i < photos.size(); i++)
			savedInstanceState.putByteArray("photo"+i, photos.get(i));
	}
	
	public boolean checkIfExists() {
		String newName = title.getText().toString();
		if (newName.equals(originalName)) 	return false;
		else return (dbHelper.findByTitle(newName).isEmpty()) ? false : true;
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
			case GeoUtils.GET_FROM_ADDRESS:
				try {
					final LatLng p = GeoUtils.getLocationFromString(address);
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
			case GeoUtils.GET_FROM_LAT_AND_LNG:
				Log.i("gowno", "lat " + latitudeEt.getText().toString());

				try {
					final String a = GeoUtils.getStringFromLocation(ltt, lgt)
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
		LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
		Bitmap picture = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        ImageView photo = new ImageView(this);
        photo.setPadding(5, 2, 5, 2);
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
				if(photos.isEmpty())
					photosTv.setVisibility(View.GONE);
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
        ll.addView(photo);
        ll.addView(imageDeleteButton);
        tr.setId(STABLE_CHILD_COUNT+n);
        tr.addView(ll);
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
		
		if (requestCode == BACK_FROM_OUTSIDE_APP) {
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
		        photosTv.setVisibility(View.VISIBLE);
		        
		        Log.i("koko","przed "+myTableLayout.getChildCount());
		        final TableRow tr = new TableRow(this);
		        LinearLayout ll = new LinearLayout(this);
		        ll.setOrientation(LinearLayout.HORIZONTAL);
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
						if(photos.isEmpty())
							photosTv.setVisibility(View.GONE);
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
		        ll.addView(photo);
		        ll.addView(imageDeleteButton);
		        tr.setId(STABLE_CHILD_COUNT+photos.size());
		        tr.addView(ll);
		        myTableLayout.addView(tr);

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
			}
		}

	}
	

}
