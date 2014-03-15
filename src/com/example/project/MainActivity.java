package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.failurereporter.R;

import database.TaskDbFacade;
import database.TaskDbHelper;

public class MainActivity extends Activity {

    private TaskDbHelper dbOpenHelper = null;
    public static TaskDbFacade dbHelper = null;
	private ListView options;
	private String [] menuOptions = {"Add New Task", "Ongoing Tasks", "History"};
	
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
		    			//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
	  }

	  @Override
	  protected void onPause() {
	    super.onPause();
	  }


}
