package com.twlkyao.fileobserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Check the apk whether it is in the authorized data base or not.
 * @author Shiyao Qi
 * @date 2014.1.9
 * @email qishiyao2008@126.com
 */
public class ApkOperation {
	
	private String tag = "ApkOperation";
	private boolean debug = false;
	private LogUtils logUtils = new LogUtils(debug, tag);
	
	public boolean ApkCheckInfo(String apkCheckUrl, String apkPath) {
		boolean flag = false; // Use the flag to indicate the result.
		FileOperation fileOperation = new FileOperation(); 
		
		// The application name and the version_name is optional.
//		String name; // To store the name of the application.
//		String version_name; // To store the version name of the application.
		
		String md5; // To store the md5 checksum of the application.
		String sha1; // To store the sha1 checksum of the application.
		
		// Get the md5 and sha1 checksum of the application file.
		md5 = fileOperation.fileToMD5(apkPath);
		sha1 = fileOperation.fileToSHA1(apkPath);
		
		HttpPost httpRequest =new HttpPost(apkCheckUrl); // Construct a new HttpPost instance according to the uri
		
		// use name-value pair to store the parameters to pass
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("name", name)); // Add the name-value pair of the apk file.
		params.add(new BasicNameValuePair("md5", md5)); // Add the name-value of the md5 checksum of the apk file.
		params.add(new BasicNameValuePair("sha1", sha1)); // Add the name-value of the sha1 checksum of the apk file.
		
		logUtils.d(tag, "params to send:" + params.toString());
//		System.out.println("params to send:" + params.toString());	// System out the params.
		
		try{
			
			// Encode the entity with utf8, and send the entity to the request
			httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8)); 
			
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		try{
			// Execute an HTTP request and ge the result
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); // execute the http request
			
			// Response status is ok
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){	
			
				// Get the response string and parse it
				HttpEntity entity = httpResponse.getEntity(); // Obtain the HTTP response entity
				if (entity != null) { // The entity obtained is not null
					String info = EntityUtils.toString(entity); // Convert the entity to string
					
					logUtils.d(tag, info);
//					System.out.println(info); // System out the returned info
					
					JSONObject jsonObject=null;
					
					jsonObject = new JSONObject(info); // Construct an JsonObject instance from the name-value Json string
					String return_flag = jsonObject.getString("flag"); // Get the value mapped by name:flag
					
					// Judge whether the validation if succeeded according to the flag
					if(return_flag.equals("true")) { // If the flag is true, put the encrypt_level and encrypt_key to the resultHashMap.
						flag = true;
					} else { // The flag is false.
						flag = false;
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
}
