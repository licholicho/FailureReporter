package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.failurereporter.R;

import failure.Failure;

public class OngoingActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ongoing, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		if (item.getItemId() == R.id.menu_add) {
			Intent i = new Intent(this, AddFailureActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		if (item.getItemId() == R.id.sort_title) {
			sortByTitle();
		}
		if (item.getItemId() == R.id.sort_bdate) {
			sortByBdate();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle("Report");
		menu.add(Menu.NONE, CONTEXT_SMS, Menu.NONE, "SMS");
		menu.add(Menu.NONE, CONTEXT_EMAIL, Menu.NONE, "E-mail");
		menu.add(Menu.NONE, CONTEXT_EXPORT, Menu.NONE, "Export");
		Log.i("lol", "asfs");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
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
	protected void setAdapter(){
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}

}

