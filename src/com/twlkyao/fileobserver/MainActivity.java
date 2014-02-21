package com.twlkyao.fileobserver;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

public class MainActivity extends Activity {

	private boolean debug = true;
	private String tag = "MainActivity";
	public LogUtils logUtils = new LogUtils(debug, tag);
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		logUtils.d("MainActivity", "into");
		
		Intent intent = new Intent(MainActivity.this, ObserverService.class);
		startService(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		logUtils.d("MainActivity", "out");
	}
}
