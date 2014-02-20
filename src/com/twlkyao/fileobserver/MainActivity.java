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

	private ObserverService observerService;
	private boolean debug = false;
	private String tag = "MainActivity";
	public LogUtils logUtils = new LogUtils(debug, tag);
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		logUtils.d("MainActivity", "into");
		
		Intent intent = new Intent(MainActivity.this, ObserverService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unbindService(conn);
		logUtils.d("MainActivity", "out");
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		
		// Get service object operation.
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			observerService =  ((ObserverService.ServiceBinder) service).getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			observerService = null;
		}
	};

}
