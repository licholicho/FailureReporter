package com.example.project;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.failurereporter.R;

import database.FailureDbFacade;
import database.FailureDbHelper;
import failure.Failure;
import failure.Serializer;

public class OngoingActivity extends Activity {

	private ListView failureLv;
	private List<Failure> reports;
	private FailureDbHelper dbOpenHelper = null;
	public static FailureDbFacade dbHelper = null;
	private static int current = -1;
	private BluetoothAdapter BA;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private BluetoothChatService mChatService = null;
	private StringBuffer mOutStringBuffer;
	private String mConnectedDeviceName;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);

		setupDbEnv();

		reports = dbHelper.listAll();
		Log.i("start", "pzeszlo");
		failureLv = (ListView) findViewById(R.id.ongoing_menu);
		BA = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("start", "onstart");
		failureLv.setAdapter(new MenuAdapter(this, reports));
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
		reports = dbHelper.listAllSortedBy("b_date", 0);
		failureLv.setAdapter(new MenuAdapter(this, reports));
	}

	public static void setCurrent(int i) {
		current = i;
	}

	final int CONTEXT_SMS = 1;
	final int CONTEXT_EMAIL = 2;
	final int CONTEXT_EXPORT = 3;

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
			if (BA.isEnabled()
					&& (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)) {
				Log.i("jest", "wysylam");
				// sendMessage("lololo");
				sendFailure(t);
			} else {
				Log.i("jest", "problem z polaczeniem");
			}
		}
			break;
		}

		return super.onContextItemSelected(item);
	}

	private void sendSms() {
		StringBuilder body = new StringBuilder();
		body.append("NEW UNSOLVED FAILURE!").append("\n");
		body.append(reports.get(current).getTitle()).append("\n");
		body.append(reports.get(current).getDescription()).append("\n");
		body.append("Notification date: ").append(
				reports.get(current).getBeginDateInString());
		body.append("Solution date: ").append(
				reports.get(current).getEndDateInString());
		Intent sendSms = new Intent(Intent.ACTION_VIEW);
		sendSms.putExtra("sms_body", body.toString());
		sendSms.setType("vnd.android-dir/mms-sms");
		startActivity(sendSms);
	}

	private void sendEmail() {
		StringBuilder body = new StringBuilder();
		body.append("NEW UNSOLVED FAILURE!").append("\n");
		body.append(reports.get(current).getTitle()).append("\n");
		body.append(reports.get(current).getDescription()).append("\n");
		body.append("Notification date: ")
				.append(reports.get(current).getBeginDateInString()).append("\n");
		if (reports.get(current).done() == 1)
			body.append("Solution date: ").append(
					reports.get(current).getEndDateInString());
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, "");
		i.putExtra(Intent.EXTRA_SUBJECT, "NEW UNSOLVED FAILURE!");
		i.putExtra(Intent.EXTRA_TEXT, body.toString());
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(OngoingActivity.this,
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void checkBluetooth() {
		if (!BA.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Turned on",
					Toast.LENGTH_LONG).show();
		} else {
			BA.disable();
			Toast.makeText(getApplicationContext(), "Turned off",
					Toast.LENGTH_LONG).show();
		}
	}

	private void openDeviceList() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		serverIntent.putExtra("message", "lol2");
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("jest", "onActivityResult " + requestCode);
		// openDeviceList();
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = BA.getRemoteDevice(address);
				// Attempt to connect to the device
				// mChatService.start();
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:

			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session

				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d("jest", "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("jest", "handler");
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					Toast.makeText(OngoingActivity.this,
							R.string.title_connected_to, Toast.LENGTH_LONG)
							.show();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					Toast.makeText(OngoingActivity.this,
							R.string.title_connecting, Toast.LENGTH_LONG)
							.show();
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					Toast.makeText(OngoingActivity.this,
							R.string.title_not_connected, Toast.LENGTH_LONG)
							.show();
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				/*
				 * String writeMessage = new String(writeBuf);
				 * Toast.makeText(OngoingActivity.this, "w " + writeMessage,
				 * Toast.LENGTH_SHORT).show();
				 */
				Log.i("jest", "write ");
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Failure t = null;
				try {
					t = (Failure) Serializer.deserialize(readBuf);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// construct a string from the valid bytes in the buffer
				String readMessage = t.getTitle();// new String(readBuf, 0,
													// msg.arg1);
				if (!dbHelper.findByTitle(t.getTitle()).isEmpty()) {
					Log.i("db","znaleziono "+t.getTitle());
					Toast.makeText(OngoingActivity.this, "znaleziono "+t.getTitle(),
							Toast.LENGTH_SHORT).show();
					Failure found = dbHelper.findByTitle(t.getTitle()).get(0);
					long originalId = found.getId();
					found = t;
					found.setId(originalId);
					Toast.makeText(OngoingActivity.this, "nowe "+found.getDescription(),
							Toast.LENGTH_SHORT).show();
					dbHelper.update(found);
					reports = dbHelper.listAll();
					failureLv.setAdapter(new MenuAdapter(OngoingActivity.this,
							reports));
				} else {
					Log.i("db","nie znaleziono "+t.getTitle());
					Toast.makeText(OngoingActivity.this, "nie znaleziono "+t.getTitle(),
							Toast.LENGTH_SHORT).show();
					dbHelper.insert(t);
					reports = dbHelper.listAll();
					failureLv.setAdapter(new MenuAdapter(OngoingActivity.this,
							reports));
				}

				Toast.makeText(OngoingActivity.this, "r " + readMessage,
						Toast.LENGTH_SHORT).show();
				Log.i("jest", "read " + readMessage);
				break;
			}
		}
	};

	private void setupChat() {
		Log.i("jest", "setupChat");
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");

	}

	/*
	 * private void sendMessage(String message) { Log.i("jest",
	 * "poczatek send! " + message); // Check that we're actually connected
	 * before trying anything if (mChatService.getState() !=
	 * BluetoothChatService.STATE_CONNECTED) { Toast.makeText(this, "not",
	 * Toast.LENGTH_SHORT).show(); Log.i("jest", "zleeeeeee"); return; }
	 * 
	 * // Check that there's actually something to send if (message.length() >
	 * 0) { Log.i("jest", "dopszeeeeee"); // Get the message bytes and tell the
	 * BluetoothChatService to write byte[] send = message.getBytes();
	 * 
	 * Toast.makeText(this, "send " + message, Toast.LENGTH_SHORT).show();
	 * mChatService.write(send);
	 * 
	 * mOutStringBuffer.setLength(0); // Reset out string buffer to zero and
	 * clear the edit text field // mOutEditText.setText(mOutStringBuffer); } }
	 */
	private void sendFailure(Failure failure) {

		Log.i("jest", "poczatek send failure!");
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, "not", Toast.LENGTH_SHORT).show();
			return;
		}
		if (failure != null) {
			Log.i("jest", "send failure dobrze " + failure.getTitle());
			byte[] send = null;
			try {
				send = Serializer.serialize(failure);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, "send ", Toast.LENGTH_SHORT).show();
			mChatService.write(send);

			mOutStringBuffer.setLength(0);
			// Reset out string buffer to zero and clear the edit text field
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

}
