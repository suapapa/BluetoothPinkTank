package net.suapapa.tankcon;

import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

public class MainController extends Activity implements
		TextToSpeech.OnInitListener, OnClickListener {
	// Debugging
	private static final String TAG = "MainController";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private TankConService mTankConService = null;

	// Layout Views

	private TextView mStatus;
	private TextToSpeech mTts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setOnClickListener((ViewGroup)findViewById(R.id.tblButtons));

		mStatus = (TextView) findViewById(R.id.txtStatus);
		mTts = new TextToSpeech(this, this);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupTankCon() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mTankConService == null)
				mTankConService = new TankConService(this, mHandler);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mTankConService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				if (mTankConService == null)
					mTankConService = new TankConService(this, mHandler);
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.KOREAN);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {
				// Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.

				// The TTS engine has been successfully initialized.
				// Allow the user to press the button for the app to speak
				// again.
				// mAgainButton.setEnabled(true);
				// Greet the user.
				sayHello();
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	private View[] getChildViews(ViewGroup group) {
		int childCount = group.getChildCount();
		final View[] childViews = new View[childCount];
		for (int index = 0; index < childCount; index++) {
			childViews[index] = group.getChildAt(index);
		}
		return childViews;
	}

	private void setOnClickListener(ViewGroup group) {
		View[] childViews = getChildViews(group);
		for (View view : childViews) {
			if (view instanceof Button) {
				view.setOnClickListener(this);
				// ViewGroup일경우는 재귀호출 한다!
			} else if (view instanceof ViewGroup) {
				setOnClickListener((ViewGroup) view);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v instanceof Button) {
			if (v.getId() == R.id.btnF)
				sendToque(255, 255);
			else if (v.getId() == R.id.btnB)
				sendToque(-255, -255);
			else if (v.getId() == R.id.btnL)
				sendToque(-255, 255);
			else if (v.getId() == R.id.btnR)
				sendToque(255, -255);
			else {
				sendToque(0, 0);
				sayRandom(quoteStop);
			}
		}
	}

	private static final Random RANDOM = new Random();
	private static final String[] quoteStop = { "장비를 정지합니다.", "정지하겠습니다.",
			"안돼!!", "모든게 제대로 되어가는군" };

	private void sayHello() {
		String hello = "오늘은 중요한 날이야.";
		mTts.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void sayRandom(String[] quateCandidate) {
		// Select a random hello.
		int lenSpk = quateCandidate.length;
		String strSpk = quateCandidate[RANDOM.nextInt(lenSpk)];
		mTts.speak(strSpk, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void sendToque(int left, int right) {
		// Check that we're actually connected before trying anything
		if (mTankConService.getState() != TankConService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		mTankConService.toque(left, right);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case TankConService.STATE_CONNECTED:
					mStatus.setText(R.string.status_connected_to);
					mStatus.append(mConnectedDeviceName);
					break;
				case TankConService.STATE_CONNECTING:
					mStatus.setText(R.string.status_connecting);
					break;
				case TankConService.STATE_LISTEN:
				case TankConService.STATE_NONE:
					mStatus.setText(R.string.status_not_connected);
					break;
				}
				break;
			// TODO: remove MESSAGE_READ
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
}