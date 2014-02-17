package com.example.project;

import java.util.List;

import task.Task;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.failurereporter.R;

import database.TaskDbFacade;
import database.TaskDbHelper;

public class OngoingActivity extends Activity {

	private ListView taskLv;
	private List<Task> tasks;
	private TaskDbHelper dbOpenHelper = null;
	public static TaskDbFacade dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);

		setupDbEnv();

		tasks = dbHelper.listAll();
		taskLv = (ListView) findViewById(R.id.ongoing_menu);
		taskLv.setAdapter(new MenuAdapter(this, tasks));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ongoing, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            Intent i = new Intent(this, AddTaskActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);            
        }
        return super.onOptionsItemSelected(item);
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

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		super.onBackPressed(); 
	}
	
}
