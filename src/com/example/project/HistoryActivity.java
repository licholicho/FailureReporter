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

public class HistoryActivity extends Activity {

	private ListView taskLv;
	private List<Task> tasks;
	private TaskDbHelper dbOpenHelper = null;
	public static TaskDbFacade dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);
		setupDbEnv();

		tasks = dbHelper.listAllDone();
		taskLv = (ListView) findViewById(R.id.ongoing_menu);
		taskLv.setAdapter(new MenuAdapter(this, tasks));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sort_title) {
			sortByTitle();
		}
		if (item.getItemId() == R.id.sort_bdate) {
			sortByBdate();
		}
		if (item.getItemId() == R.id.sort_edate) {
			sortByEdate();
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

	public void sortByTitle() {
		tasks = dbHelper.listAll();
		taskLv.setAdapter(new MenuAdapter(this, tasks));
	}

	public void sortByBdate() {
		tasks = dbHelper.listAllSortedBy("b_date", 1);
		taskLv.setAdapter(new MenuAdapter(this, tasks));
	}

	public void sortByEdate() {
		tasks = dbHelper.listAllSortedBy("e_date", 1);
		taskLv.setAdapter(new MenuAdapter(this, tasks));
	}

}
