package com.example.project;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.failurereporter.R;

import database.FailureDbFacade;
import database.FailureDbHelper;
import failure.Failure;

public class HistoryActivity extends Activity {

	private ListView failureLv;
	private List<Failure> reports;
	private FailureDbHelper dbOpenHelper = null;
	public static FailureDbFacade dbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);
		setupDbEnv();

		reports = dbHelper.listAllDone();
		failureLv = (ListView) findViewById(R.id.ongoing_menu);
		failureLv.setAdapter(new MenuAdapter(this, reports));
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
			dbOpenHelper = new FailureDbHelper(this);
		}
		if (dbHelper == null) {
			dbHelper = new FailureDbFacade(dbOpenHelper.getWritableDatabase());
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
		reports = dbHelper.listAll();
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}

	public void sortByBdate() {
		reports = dbHelper.listAllSortedBy("b_date", 1);
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}

	public void sortByEdate() {
		reports = dbHelper.listAllSortedBy("e_date", 1);
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}

}
