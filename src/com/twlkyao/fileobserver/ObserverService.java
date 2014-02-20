package com.twlkyao.fileobserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Stack;

import android.R.anim;
import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;

public class ObserverService extends Service {
	private FileObserver mFileObserver;
	private ServiceBinder serviceBinder;
	
	private boolean debug = false; // Indicate whether is debug.
	private String tag = "ObserverService";
	private LogUtils logUtils = new LogUtils(debug, tag);
	private NotificationManager notificationManager; // NotificationManager.
	private int notification_number = 0; // The number of notifications.
	
	// You can define a mode to monitor.
	private int CHANGES_ONLY = FileObserver.CREATE | FileObserver.DELETE | FileObserver.CLOSE_WRITE
			| FileObserver.DELETE_SELF | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM | FileObserver.MOVED_TO;
	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(null == mFileObserver) {
			String filePath = Environment.getExternalStorageDirectory().getPath();
			
			mFileObserver = new RecursiveFileObserver(filePath); // mine
			mFileObserver.startWatching(); // Start watching.
		}
		
		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return serviceBinder;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(null != mFileObserver) mFileObserver.stopWatching(); //Stop watching.
	}

	class ServiceBinder extends Binder {
		public ObserverService getService() {
			return ObserverService.this;
		}
	}


	/**
	 * Monitor all events under certain directory recursively, can't monitor the hiden directory such as ".a".
	 * @author NetWork 
	 * @editor Shiyao Qi
	 * @date 2014.1.10
	 * @email qishiyao2008@126.com
	 * @attention
	 * 			You should protect the FileObserver not to be garbage collected,
	 * 			or it will not be able to detect the file or directory changes.
	 */
	class RecursiveFileObserver extends FileObserver {
		
		ArrayList<SingleFileObserver> mObservers; // An ArrayList of SingleFileObservers to observer files. 
		String mPath; // The file path to be observed.
		int mMask; // Observation mask, specify the file operation that we want to monitor.
		
		/**
		 * Constructor, that monitor all the events.
		 * @param path The file path to monitor.
		 */
		public RecursiveFileObserver(String path) {
			this(path, ALL_EVENTS); // Call the RecursiveFileObserver(String path, int mask) constructor.
		}
		
		/**
		 * Constructor, that monitor the specified events that specified by mask.
		 * @param path The file or directory to monitor.
		 * @param mask The event or events (added together) to monitor.
		 */
		public RecursiveFileObserver(String path, int mask) {
			super(path, mask); // Call super().
			mPath = path; // Initial mPath.
			mMask = mask; // Initial mMask.
		}
		
		@Override
		public void startWatching() {
			if (mObservers != null) // There are FileObservers working.
				return;
			
			// Instance a ArrayList<SingleFileObersver> if the mObservers is null.
			mObservers = new ArrayList<SingleFileObserver>();
			Stack<String> stack = new Stack<String>(); // Used to construct and store the full file path.
			stack.push(mPath);
			
			// Add all the files under mPath directory to FileObserver.
			while (!stack.isEmpty()) { // There are still file path in the stack.
				String parent = (String)stack.pop(); // Pop the top element as a file path.
				mObservers.add(new SingleFileObserver(parent, mMask)); // Add the file path to file observer.
				File path = new File(parent); // Construct a file according to the file path.
				File[] files = path.listFiles(); // Get all the files under the file path.
				
				/**
				 * (1)the file is not a directory
				 * (2)the file does not exist
				 * (3)the directory is read-protected
				 * (4)there is a IO error.
				 * if the directory is empty the result will be zero.
				 */
				if (null == files)
					continue;
				// Recursively look through the directory.
				for (File f : files) {
					// Push the subdirectories' path under the file path to stack for future use.
					if (f.isDirectory() && !f.getName().equals(".") // The directory is neither the current directory nor the parent directory.
							&& !f.getName().equals("..")) {
						stack.push(f.getPath());
					}
				}
			}
			
			// All the files under specified directory startWathching().
			for (int i = 0; i < mObservers.size(); i++) {
				SingleFileObserver sfo = (SingleFileObserver) mObservers.get(i);
				sfo.startWatching();
			}
		};
		
		@Override
		public void stopWatching() {
			if (mObservers == null) // All FileObservers are stopped.
				return;
			
			// Stop all FileObservers's watching.
			for (int i = 0; i < mObservers.size(); i++) {
				SingleFileObserver sfo = (SingleFileObserver) mObservers.get(i);
				sfo.stopWatching();
			}
			
			mObservers.clear(); // Remove all the FileObservers.
			mObservers = null; // Set the ArrayList to null.
		};
		
		/**
		 * Monitor the event of file under specified file path specified by path pararm.
		 * @param event The type of event which happened
		 * @param path The path, relative to the main monitored file or directory, of the file or directory which triggered the event
		 */
		/**
		 * ACCESS 即文件被访问
		 * MODIFY 文件被写入
		 * ATTRIB 文件属性被修改，如 chmod、chown、touch 等
		 * CLOSE_WRITE 文件被写之后关闭
		 * CLOSE_NOWRITE 文件被只读打开，关闭
		 * OPEN 文件被打开
		 * MOVED_FROM 文件或目录从被监视的目录移走，如 mv
		 * MOVED_TO 文件或目录从别处被移来，如 mv、cp
		 * CREATE 创建新文件或者目录
		 * DELETE 被监控目录下文件或者目录被删除，如 rm
		 * DELETE_SELF 被监控的文件或者目录被删除，即一个可执行文件在执行时删除自己，监控停止。
		 * MOVE_SELF 被监控的文件或者目录被移动，即一个可执行文件在执行时移动自己，监控继续。
		 * CLOSE 被监控的文件或者目录被关闭，等同于(IN_CLOSE_WRITE | IN_CLOSE_NOWRITE)
		 * ALL_EVENTS ： 包括上面的所有事件
		 */
		/**
		 * ACCESS	Event type: Data was read from a file, constant value 1.
		 * MODIFY	Event type: Data was written to a file, constant value 2.
		 * ATTRIB	Event type: Metadata (permissions, owner, timestamp) was changed explicitly, constant value 4
		 * CLOSE_NOWRITE	Event type: Someone had a file or directory open read-only, and closed it, Constant value 8
		 * CLOSE_WRITE	Event type: Someone had a file or directory open for writing, and closed it, constant value 16
		 * OPEN	Event type: A file or directory was opened, Constant value 32
		 * MOVED_FROM	Event type: A file or subdirectory was moved from the monitored directory, constant value 64
		 * MOVED_TO	Event type: A file or subdirectory was moved to the monitored directory, constant value 128
		 * CREATE	Event type: A new file or subdirectory was created under the monitored directory, constant value 256
		 * DELETE	Event type: A file was deleted from the monitored directory, Constant value 512
		 * DELETE_SELF	Event type: The monitored file or directory was deleted; monitoring effectively stops, constant value 1024
		 * MOVE_SELF	Event type: The monitored file or directory was moved; monitoring continues, constant value 2048
		 * ALL_EVENTS	Event mask: All valid event types, combined, Constant value 4095
		 */
		/**
		 * This method is invoked on a special FileObserver thread.
		 * It runs independently of any threads,
		 * so take care to use appropriate synchronization!
		 * Consider using post(Runnable) to shift event
		 * handling work to the main thread to avoid concurrency problems.
		 */
		@Override
		public void onEvent(int event, String path) { // The path here is a full path.
			
			// The param is the value | 0x40000000, before operate, first & FileObserver.ALL_EVENTS
			switch (event & FileObserver.ALL_EVENTS) {
			
			// If you want to do some time-costing work under onEvent, you'd better use a thread,
			// in case for that you will not receive the following event.
				case FileObserver.ACCESS: // 1
					logUtils.d(tag, "ACCESS: " + path);
					break;
				case FileObserver.MODIFY: // 2
					logUtils.d(tag, "MODIFY: " + path);
					break;
				case FileObserver.ATTRIB: // 4
					logUtils.d(tag, "ATTRIB: " + path);
					break;
				case FileObserver.CLOSE_NOWRITE: // 8
					logUtils.d(tag, "CLOSE_NOWRITE: " + path);
					break;
				case FileObserver.CLOSE_WRITE: // 16
					logUtils.d(tag, "CLOSE_WRITE: " + path);

					
					Notification notification = new Notification(android.R.drawable.ic_notification_clear_all,
							path, System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL; // The notification should be canceled when click the Clear all button.
					Intent i = new Intent(getApplicationContext(), ApkValidate.class);
					Bundle bundle = new Bundle();
					bundle.putString("filepath", path);
					i.putExtras(bundle);
					logUtils.d(tag, path);
					
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
					//PendingIntent
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
							0,
							i,
							PendingIntent.FLAG_UPDATE_CURRENT);
					
					notification.setLatestEventInfo(
					        getApplicationContext(),
					        getString(R.string.apk_downloaded),
					        path,
					        contentIntent);
					notificationManager.notify(notification_number++, notification);
					
					/*if(path.contains(".apk")){
						Log.d("Filter", path);
						File[] files = new File(path).listFiles(new myFileFilter());
					}*/
					break;
				case FileObserver.OPEN: // 32
					logUtils.d(tag, "OPEN: " + path);
					break;
				case FileObserver.MOVED_FROM: // 64
					logUtils.d(tag, "MOVED_FROM: " + path);
					/*if(path.contains("apk")){
						Log.d("Filter", path);
//						File[] files = new File(path).listFiles(new myFileFilter());
					}*/
					break;
				case FileObserver.MOVED_TO: // 128
					logUtils.d(tag, "MOVED_TO: " + path);
					/*if(path.contains("apk")){
						Log.d("Filter", path);
						File[] files = new File(path).listFiles(new myFileFilter());
					}*/
					break;
				case FileObserver.CREATE: // 256
					logUtils.d(tag, "CREATE: " + path);
					
					/*if(path.contains("apk")){
						Log.d("Filter", path);
						File[] files = new File(path).listFiles(new myFileFilter());
					}
					if(files.length != 0) {
						Log.d("FileFilter", "");
					}
					for(int i = 0; i < files.length; i++) {
						Log.d("FileFilter", files[i].getName());
					}*/
					
					break;
				case FileObserver.DELETE: // 512
					logUtils.d(tag, "DELETE: " + path);
					break;
				case FileObserver.DELETE_SELF: // 1024
					logUtils.d(tag, "DELETE_SELF: " + path);
					break;
				case FileObserver.MOVE_SELF: // 2048
					logUtils.d(tag, "MOVE_SELF: " + path);
					break;
				default: //FileObserver.ALL_EVENTS: // 4095
					logUtils.d(tag, "ALL_EVENTS: " + path);
					break;
			}
		}
		
		/**
		 * Monitor single directory and dispatch all events to its parent, with full path.
		 */
		class SingleFileObserver extends FileObserver {
			String mPath; // The file path to observer.
			
			public SingleFileObserver(String path) {
				this(path, ALL_EVENTS); // Call the SingleFileObserver(String path, int mask).
				mPath = path;
			}  
			
			public SingleFileObserver(String path, int mask) {
				super(path, mask);
				mPath = path;
			}
			
			@Override
			public void onEvent(int event, String path) {
				if(!mPath.endsWith(File.separator)) { // Fix the mPath string.
					mPath += File.separator;
				}
				String newPath = mPath + path; // Construct the full path.
				
				// To unify the operation on the file event.
				RecursiveFileObserver.this.onEvent(event, newPath); // Call the RecursiveFileObserver with the full path.
			}
		}
	}
	
	/**
	 * Filter the files according to the specified suffix of file.
	 * @author NetWork
	 * @editor Shiyao Qi
	 * @date 2013.1.10
	 * @email qishiyao2008@126.com
	 */
	class myFileFilter implements FileFilter{
		String filter; // The file filter flag.
		
		/**
		 * Constructor.
		 * @param filter The file filter flag to use.
		 */
		public myFileFilter(String filter) {
			this.filter = filter;
		}

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
		
			String filename = pathname.getName().toLowerCase(); // Get the file name and lower it.
			if(filename.contains(filter)){ // Filter the file according to their names.
				return false;
			}else{
				return true;
			}
		}
	}
}
