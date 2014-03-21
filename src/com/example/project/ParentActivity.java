package com.example.project;

import java.io.IOException;
import java.util.List;

import utils.Utils;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.failurereporter.R;

import database.FailureDbFacade;
import database.FailureDbHelper;
import failure.Failure;
import failure.Serializer;

public class ParentActivity extends Activity {

	protected ListView failureLv;
	protected List<Failure> reports;
	protected FailureDbHelper dbOpenHelper = null;
	public static FailureDbFacade dbHelper = null;
	protected static int current = -1;
	protected BluetoothAdapter BA;
	protected static final int REQUEST_CONNECT_DEVICE = 1;
	protected static final int REQUEST_ENABLE_BT = 2;
	protected BluetoothService mChatService = null;
	protected StringBuffer mOutStringBuffer;
	protected String mConnectedDeviceName;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	protected static final int CONTEXT_SMS = 1;
	protected static final int CONTEXT_EMAIL = 2;
	protected static final int CONTEXT_EXPORT = 3;
	//protected String specialString = "_";
	//protected static int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ongoing);
		setupDbEnv();
		BA = BluetoothAdapter.getDefaultAdapter();
	//	init();
	}

	
	protected void init(){
		viewAll();
		failureLv = (ListView) findViewById(R.id.ongoing_menu);
		setAdapter();
	}


	protected void setupDbEnv() {
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
		reports = dbHelper.listAllSortedBy("b_date", 0);
		setAdapter();
	}

	public static void setCurrent(int i) {
		current = i;
	}



	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		// Context menu
		menu.setHeaderTitle("Report");
		menu.add(Menu.NONE, CONTEXT_SMS, Menu.NONE, "SMS");
		menu.add(Menu.NONE, CONTEXT_EMAIL, Menu.NONE, "E-mail");
		menu.add(Menu.NONE, CONTEXT_EXPORT, Menu.NONE, "Export");
	}

	protected void sendSms() {
		StringBuilder body = new StringBuilder();
		body.append("NEW UNSOLVED FAILURE!").append("\n");
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

	protected void sendEmail() {
		StringBuilder body = new StringBuilder();
		body.append("INFO ABOUT UNSOLVED FAILURE!").append("\n");
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

	protected void checkBluetooth() {
		if (!BA.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Turning on...",
					Toast.LENGTH_LONG).show();
		} else {
			BA.disable();
			Toast.makeText(getApplicationContext(), "Turning off...",
					Toast.LENGTH_LONG).show();
		}
	}

	protected void openDeviceList() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		serverIntent.putExtra("message", "lol2");
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
				Utils.log("BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	protected final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					Toast.makeText(getApplicationContext(),
							R.string.title_connected_to, Toast.LENGTH_LONG)
							.show();
					break;
				case BluetoothService.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(),
							R.string.title_connecting, Toast.LENGTH_LONG)
							.show();
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					Toast.makeText(getApplicationContext(),
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
				 */
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Failure t = null;
				try {
					t = (Failure) Serializer.deserialize(readBuf);
				} catch (ClassNotFoundException e) {
					Utils.loge("Class not found");
					e.printStackTrace();
				} catch (IOException e) {
					Utils.loge("io expception");
					e.printStackTrace();
				}
				// construct a string from the valid bytes in the buffer
				if (!dbHelper.findByTitle(t.getTitle()).isEmpty()) {
					Failure found = dbHelper.findByTitle(t.getTitle()).get(0);
					long originalId = found.getId();
					found = t;
					found.setId(originalId);
					dbHelper.update(found);
					viewAll();
					setAdapter();
				} else {
					dbHelper.insert(t);
					viewAll();
					setAdapter();
				}
				break;
			}
		}
	};

	protected void setupChat() {
		mChatService = new BluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");

	}

	protected void setAdapter() {
		failureLv.setAdapter(new MenuAdapter(ParentActivity.this,
				reports));
	}
	
	protected void viewAll(){
		reports = dbHelper.listAll();			
	}
	
	protected void sendPhoto (byte[] photo){
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}
	
			Toast.makeText(this, "send photo ", Toast.LENGTH_SHORT).show();
			mChatService.write(photo);

			mOutStringBuffer.setLength(0);
			// Reset out string buffer to zero and clear the edit text field
			// mOutEditText.setText(mOutStringBuffer);
		}
	
		
	protected void sendFailure(Failure failure) {
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}
		if (failure != null) {
			if (!failure.getPhotos().isEmpty())
				failure.setPhotosEmpty();
			byte[] send = null;
			try {
				send = Serializer.serialize(failure);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mChatService.write(send);

			mOutStringBuffer.setLength(0);
	}
	}

}
