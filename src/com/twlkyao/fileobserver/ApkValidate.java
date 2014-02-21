package com.twlkyao.fileobserver;

import java.io.File;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.widget.Toast;

public class ApkValidate extends Activity {

	private FileOperation fileOperation = new FileOperation();
	private ApkOperation apkOperation = new ApkOperation();
	private String filePath;
	private String tag = "ApkValidation";
	private boolean debug = false;
	private LogUtils logUtils = new LogUtils(debug, tag);
	private String apkCheckUrl = "http://10.0.2.2/cloud/check_apk_info"; // Change this to your service address.
	
	// Deal with the time-consuming matters
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(),
						R.string.apk_passed,
						Toast.LENGTH_SHORT).show();
				break;
			case 0:
				Builder builder = new AlertDialog.Builder(ApkValidate.this);
				builder.setTitle(R.string.apk_validate_title);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setMessage(R.string.apk_failed);
				builder.setPositiveButton(R.string.btn_delete, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						File file = new File(filePath);
						if(file.exists()) {
							file.delete();
						
							logUtils.d(tag, "File deleted");
						}
					}
				});
				builder.setNegativeButton(R.string.btn_ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						logUtils.d(tag, "No operation");
					}
					
				});
				builder.create();
				builder.show();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apk_validate);
		
		Bundle bundle = getIntent().getExtras();
		logUtils.d(tag, filePath);
		
		filePath = bundle.getString("filepath");
		
		
		
		final Message msg = Message.obtain();
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean flag = apkOperation.ApkCheckInfo(apkCheckUrl, filePath);
				if(flag) {
					msg.what = 1; // The apk passed validation.
				} else {
					msg.what = 0; // The apk failed validation.
				}
				handler.sendMessage(msg);
			}
		});
		
		thread.start(); // Start the thread.
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.apk_validate, menu);
		return true;
	}

}
