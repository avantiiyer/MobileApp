package com.microsoft.band.sdk.sampleapp;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sdk.sampleapp.streaming.R;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.HeartRateConsentListener;

import android.app.ActivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

public class BandStreamingAppActivity extends Activity {

	private BandClient client = null;
	private TextView txtStatus;
	private TextView heartrate;
	private TextView temperature;
	private TextView sleep;
	private TextView placenum;
	private EditText newnum;
	private boolean isAsleep;
	private float hrate;
	private float accel;
	private float x=0;
	private float y=0;
	private float z=0;
	private float temp;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new appTask().execute();
		newnum = (EditText)findViewById(R.id.enternumber);
		heartrate = (TextView)findViewById(R.id.heartrate);
		temperature = (TextView)findViewById(R.id.temp);
		sleep = (TextView)findViewById(R.id.sleep);
		placenum = (TextView)findViewById(R.id.phonenumber);
		txtStatus = (TextView)findViewById(R.id.errors);

	}

	public void updateContact(View v){

		if (newnum.getText() != null) {
			placenum.setText(newnum.getText());
			//phone = newnum.getText().toString();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		txtStatus.setText("");

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (client != null) {
			try {
				client.getSensorManager().unregisterAccelerometerEventListeners();
				client.getSensorManager().unregisterHeartRateEventListeners();
				client.getSensorManager().unregisterSkinTemperatureEventListeners();
			} catch (BandIOException e) {
				appendToUI(e.getMessage());
			}
		}
	}

	private class appTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					appendToUI("Band is connected.\n");
					client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
					if(client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
						client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
					} else {
						// user has not consented yet, request it
						client.getSensorManager().requestHeartRateConsent(BandStreamingAppActivity.this, mHeartRateConsentListener);
					}


					client.getSensorManager().registerSkinTemperatureEventListener(mTemperatureListener);

				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
					case UNSUPPORTED_SDK_VERSION_ERROR:
						exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
						break;
					case SERVICE_ERROR:
						exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
						break;
					default:
						exceptionMessage = "Unknown error occured: " + e.getMessage();
						break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}

	private void appendToUI(final String string) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txtStatus.setText(string);

				if (!isAsleep && hrate <= 52 && accel <= 3) {
					sleep.setText("Your child is sleeping");
					isAsleep = true;
				} else if (isAsleep && hrate >= 64 && accel >= 5) {
					sleep.setText("Your child is awake");
					isAsleep = false;
					//sendNotification(awakemessage);
				}

				String rate = String.format("%.1f", hrate);
				heartrate.setText(rate);

				String tmptemp = String.format("%.1f", temp);
				temperature.setText(tmptemp);
			}
		});
	}

	private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
		@Override
		public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
			if (event != null) {
				accel = Math.abs(z-event.getAccelerationX()) + Math.abs(y-event.getAccelerationY()) + Math.abs(z-event.getAccelerationZ());
				y = event.getAccelerationY();
				x = event.getAccelerationX();
				z = event.getAccelerationZ();
				appendToUI("");
			}
		}
	};

	private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
		@Override
		public void onBandHeartRateChanged(final BandHeartRateEvent event) {
			if (event != null) {
				hrate = event.getHeartRate();
				if (hrate > 130 || (hrate < 55 &&!isAsleep)) {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					//callIntent.setData(Uri.parse("tel:"+phone));
					//startActivity(callIntent);
				}
					//sendNotification(hrmessage);
				appendToUI("");
			}

		}
	};

	private HeartRateConsentListener mHeartRateConsentListener = new HeartRateConsentListener() {
		@Override
		public void userAccepted(boolean b) {
			// handle user's heart rate consent decision
			if (b == true) {
				// Consent has been given, start HR sensor event listener
				new appTask().doInBackground();
			} else {
				// Consent hasn't been given
				appendToUI(String.valueOf(b));
			}
		}
	};


	private BandSkinTemperatureEventListener mTemperatureListener = new BandSkinTemperatureEventListener() {
		@Override
		public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent event) {
			if (event != null) {
				temp = (event.getTemperature()*9/5) + 32;
				appendToUI("");
			}
		}
	};
/*
	private void sendNotification(String message) {
		Intent intent = new Intent(this, NotificationService.class);
		intent.putExtra(NotificationService.NOTIFICATION_SERVICE, message);

		startService(intent);
	}
*/
	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n");
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}

		appendToUI("Band is connecting...\n");
		return ConnectionState.CONNECTED == client.connect().await();
	}
}

