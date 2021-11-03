package it.unisalento.iit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import it.unisalento.iit.uart.ConnectActivity;
import it.unisalento.iit.utility.IntentHelper;

public class SplashscreenActivity extends Activity {

	private static final int DELAY = 2000; //millisecondi

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		// Aspetta il DELAY e apre UARTActivity
		new Handler().postDelayed(() -> {
			IntentHelper.apriMainActivity(SplashscreenActivity.this);
			finish();
		}, DELAY);
	}

	@Override
	public void onBackPressed() { /* Vuoto perchè non si deve poter uscire dallo SplashScreen*/	}
}
