package com.example.project;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.failurereporter.R;

import database.FailureDbFacade;
import database.FailureDbHelper;
import failure.Failure;

public class HistoryActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);
		init();
	}
	
	@Override
	protected void init(){
		viewAll();
		failureLv = (ListView) findViewById(R.id.ongoing_menu);
		setAdapter();
	}
	
	@Override
	protected void viewAll(){
		reports = dbHelper.listAllDone();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Failure t = reports.get(current);
		switch (item.getItemId()) {
		case CONTEXT_SMS:
			sendSms();
			break;
		case CONTEXT_EMAIL:
			sendEmail();
			break;
		case CONTEXT_EXPORT: {
			if (BA.isEnabled() && (mChatService != null)
					&& (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)) {
				sendFailure(t);
			} else {
				Log.i("jest", "problem z polaczeniem");
			}
		}
			break;
		}

		return super.onContextItemSelected(item);
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
		if (item.getItemId() == R.id.menu_delete_all) {
			deleteAllDone();
		}
		if (item.getItemId() == R.id.on_off) {
			checkBluetooth();
		}
		if (item.getItemId() == R.id.connect) {
			Log.i("jest", "connect");
			if (!BA.isEnabled()) {
				Toast.makeText(getApplicationContext(),
						"Please, turn Bluetooth on", Toast.LENGTH_LONG).show();
			} else {
				openDeviceList();
				if (mChatService == null)
					setupChat();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	


	protected void setupDbEnv() {
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
		viewAll();
		setAdapter();
	}

	public void sortByBdate() {
		reports = dbHelper.listAllSortedBy("b_date", 1);
		setAdapter();
	}

	public void sortByEdate() {
		reports = dbHelper.listAllSortedBy("e_date", 1);
		setAdapter();
	}
	
	public void deleteAllDone(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	// Add the buttons
    	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   List<Failure> listToDelete = reports;//dbHelper.listAllDone();
    	        	   for (Failure f : listToDelete)
    	        		   dbHelper.delete(f);
    	        	   viewAll();
    	        	   setAdapter();
    	           }
    	       });
    	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {

    	           }
    	       });
    	builder.setMessage(R.string.sure_to_clear_history);
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
	@Override
	protected void setAdapter(){
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}
	
	@Override
	protected void sendSms() {
		StringBuilder body = new StringBuilder();
		body.append("FAILURE SOLVED!").append("\n");
		body.append(reports.get(current).getTitle()).append("\n");
		body.append(reports.get(current).getDescription()).append("\n");
		body.append("Notification date: ").append(
				reports.get(current).getBeginDateInString()).append("\n");
		body.append("Solution date: ").append(
				reports.get(current).getEndDateInString());
		Intent sendSms = new Intent(Intent.ACTION_VIEW);
		sendSms.putExtra("sms_body", body.toString());
		sendSms.setType("vnd.android-dir/mms-sms");
		startActivity(sendSms);
	}

	@Override
	protected void sendEmail() {
		StringBuilder body = new StringBuilder();
		body.append("FAILURE SOLVED!").append("\n");
		body.append(reports.get(current).getTitle()).append("\n");
		body.append(reports.get(current).getDescription()).append("\n");
		body.append("Notification date: ")
				.append(reports.get(current).getBeginDateInString()).append("\n");
		if (reports.get(current).done() == 1)
			body.append("Solution date: ").append(
					reports.get(current).getEndDateInString());
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		List<byte[]> photos = reports.get(current).getPhotos();
		if(!photos.isEmpty()) {
			for (int j = 0; j < photos.size(); j++) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(photos.get(j), 0, photos.get(j).length);
				String pathofBmp = Images.Media.insertImage(getContentResolver(), bitmap,"photo"+j, null);
				Uri bmpUri = Uri.parse(pathofBmp);
				i.putExtra(Intent.EXTRA_STREAM, bmpUri);
			}
		}
		i.putExtra(Intent.EXTRA_EMAIL, "");
		i.putExtra(Intent.EXTRA_SUBJECT, "NEW UNSOLVED FAILURE!");
		i.putExtra(Intent.EXTRA_TEXT, body.toString());
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		}
	}

}
