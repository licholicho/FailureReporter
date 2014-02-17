package com.example.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.failurereporter.R;

import database.TaskDbFacade;
import database.TaskDbHelper;

public class MainActivity extends Activity implements LocationListener {

    private TaskDbHelper dbOpenHelper = null;
    public static TaskDbFacade dbHelper = null;
	private ListView options;
	private String [] menuOptions = {"Add New Task", "Ongoing Tasks", "History"};
	 
	private TextView latituteField;
	  private TextView longitudeField;
	  private LocationManager locationManager;
	  private String provider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		 setupDbEnv();
		 	 
		options = (ListView) findViewById(R.id.list_menu);
		options.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, menuOptions));
		options.setOnItemClickListener(new OnItemClickListener() {

		    public void onItemClick(AdapterView<?> parent, View view, int position,
		            long id) {
		    	Intent intent = new Intent();
		    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    	switch(position) {
		    	case 0:	    			
		    			intent.setClass(view.getContext(), AddTaskActivity.class);
		    			startActivity(intent);
		    			break;
		    	case 1:
	    				intent.setClass(view.getContext(), OngoingActivity.class);
	    				startActivity(intent);
		    			break;
		    	case 2:
		    			intent.setClass(view.getContext(), HistoryActivity.class);
		    			startActivity(intent);
		    			break;
		    	default:
		    			break;
		    	}
		    	Log.i("topics","d "+position);
		    }
		}); 
		latituteField = (TextView) findViewById(R.id.textv1);
	    longitudeField = (TextView) findViewById(R.id.textv2);

	    // Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);

	    /*
	    List<Task> tasks = dbHelper.listAll();
	    for (int i = 0; i < tasks.size(); i++) {
	    	Location tmp = new Location(provider);
	  	    tmp.setLatitude(tasks.get(i).getLocation().getLatitude());
	  	    tmp.setLongitude(tasks.get(i).getLocation().getLongitude());
	  	    if (location.distanceTo(tmp) < tasks.get(i).getReminder()){
	  	    	 Toast.makeText(this, "Zblizasz sie do celu " + tasks.get(i).getTitle(),
	  	    	        Toast.LENGTH_SHORT).show();
	  	    }	
	    }
	    */
	    // Initialize the location fields
	    if (location != null) {
	      Log.i("geo", "Provider " + provider + " has been selected.");
	      onLocationChanged(location);
	    } else {
	      latituteField.setText("Location not available");
	      longitudeField.setText("Location not available");
	    }
		//startService(new Intent(this, LocationService.class));
	 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	 
	 @Override
	  protected void onResume() {
	    super.onResume();
	    locationManager.requestLocationUpdates(provider, 400, 1, this);
	  }

	  /* Remove the locationlistener updates when Activity is paused */
	  @Override
	  protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	  }

	  @Override
	  public void onLocationChanged(Location location) {
	    double lat = location.getLatitude();
	    double lng = location.getLongitude();
	    latituteField.setText(String.valueOf(lat));
	    longitudeField.setText(String.valueOf(lng));
	  }

	  @Override
	  public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub

	  }

	  @Override
	  public void onProviderEnabled(String provider) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	        Toast.LENGTH_SHORT).show();

	  }

	  @Override
	  public void onProviderDisabled(String provider) {
	    Toast.makeText(this, "Disabled provider " + provider,
	        Toast.LENGTH_SHORT).show();
	  }
}
