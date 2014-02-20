/**
 * @Author:		Shiyao Qi
 * @Date:		2013.11.25
 * @Function:	Operations on file
 * @Email:		qishiyao2008@126.com
 */

package com.twlkyao.fileobserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
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

public class FileOperation {
	
	private String tag = "FileOperation"; // The logcat tag
	private LogUtils logUtils;
	// Define the hashmap to record the basic information of files during the session
//	private HashMap<String, String> session =new HashMap<String, String>();
	
	
	/**
	 * Find the specified files according to the filepath
	 * @param keyword The keyword
	 * @param filepath The filepath to start
	 * @return A list that contains the files that meet the conditions
	 */
	public ArrayList<File> findFileByName(String keyword, String filepath) {
		File files = new File(filepath); // Create a new file
		ArrayList<File> list = new ArrayList<File>(); // Used to store the search result
		for(File file : files.listFiles()) { // Recursively search the file
			
//			Log.d("ListFiles",file.getAbsolutePath()); // Log the files under the specified filepath
			
			if(file.isDirectory()) { // The variable file is a directory
				
//				Log.d("ListFile","Directory:"+file.getAbsolutePath()); // If the variable is a directory log it
				
				if(file.getName().contains(keyword)) { // If the filepath contains the keyword, add it to the list
					list.add(file);
				}
				if(file.canRead()) {  // Without this the program will collapse
					list.addAll(findFileByName(keyword, file.getAbsolutePath())); // Recursive into the filepath
				}
				
			} else { // The variable file is a file
				
//				Log.d("ListFile","File:"+file.getAbsolutePath());
				if(file.getName().contains(keyword)) { // If the file's name contains the keyword, add it to the list
					list.add(file);
				}
			}
		}
		return list;
	}
	
	/**
	 * Get the md5 value of the filepath specified file
	 * @param filePath The filepath of the file
	 * @return The md5 value
	 */
	public String fileToMD5(String filePath) {
	    InputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
	        byte[] buffer = new byte[1024]; // The buffer to read the file
	        MessageDigest digest = MessageDigest.getInstance("MD5"); // Get a MD5 instance
	        int numRead = 0; // Record how many bytes have been read
	        while (numRead != -1) {
	            numRead = inputStream.read(buffer);
	            if (numRead > 0)
	                digest.update(buffer, 0, numRead); // Update the digest
	        }
	        byte [] md5Bytes = digest.digest(); // Complete the hash computing
	        return convertHashToString(md5Bytes); // Call the function to convert to hex digits
	    } catch (Exception e) {
	        return null;
	    } finally {
	        if (inputStream != null) {
	            try {
	                inputStream.close(); // Close the InputStream
	            } catch (Exception e) { }
	        }
	    }
	}
	
	/**
	 * Get the sha1 value of the filepath specified file
	 * @param filePath The filepath of the file
	 * @return The sha1 value
	 */
	public String fileToSHA1(String filePath) {
	    InputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
	        byte[] buffer = new byte[1024]; // The buffer to read the file
	        MessageDigest digest = MessageDigest.getInstance("SHA-1"); // Get a SHA-1 instance
	        int numRead = 0; // Record how many bytes have been read
	        while (numRead != -1) {
	            numRead = inputStream.read(buffer);
	            if (numRead > 0)
	                digest.update(buffer, 0, numRead); // Update the digest
	        }
	        byte [] sha1Bytes = digest.digest(); // Complete the hash computing
	        return convertHashToString(sha1Bytes); // Call the function to convert to hex digits
	    } catch (Exception e) {
	        return null;
	    } finally {
	        if (inputStream != null) {
	            try {
	                inputStream.close(); // Close the InputStream
	            } catch (Exception e) { }
	        }
	    }
	}

	/**
	 * Convert the hash bytes to hex digits string
	 * @param hashBytes
	 * @return The converted hex digits string
	 */
	private static String convertHashToString(byte[] hashBytes) {
		String returnVal = "";
		for (int i = 0; i < hashBytes.length; i++) {
			returnVal += Integer.toString((hashBytes[i] & 0x0ff) + 0x100, 16).substring(1);
		}
		return returnVal.toLowerCase();
	}
	
	/**
	 * Retrieve encrypt key from remote key generator server
	 * according to the encrypt level of file.
	 * @param retrieveEncryptKeyUrl The url of the password generator server.
	 * @param encrypt_level The encrypt file of the file.
	 * @return The retrieved encrypt key.
	 */
	public String retrieveEncryptKey(String retrieveEncryptKeyUrl, int encrypt_level) {
		
		String encrypt_key = ""; // To store the retrieved encrypt_key.
		
		HttpPost httpRequest =new HttpPost(retrieveEncryptKeyUrl); // Construct a new HttpPost instance according to the uri
		
		// use name-value pair to store the parameters to pass
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("encrypt_level", String.valueOf(encrypt_level))); // Add the encrypt level name-value
		
		logUtils.d(tag, "params to send:" + params.toString());	// Log out the parameters
		
		try {
			// Encode the entity with utf8, and send the entity to the request
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
					
					logUtils.d(tag, info); // Log out the returned info
					
					JSONObject jsonObject=null;
					// Flag to indicate whether login succeeded, others to store the data from server
					
					String flag = ""; 	// The flag to indicate the upload status.
					
					jsonObject = new JSONObject(info); // Construct an JsonObject instance from the name-value Json string
					flag = jsonObject.getString("flag"); // Get the value mapped by name:flag							
					
					// Judge whether the validation if succeeded according to the flag
					if(flag.equals("success")) { // If the operation type is success, get the encrypt key.
						
						encrypt_key = jsonObject.getString("encrypt_key");	// Get the value mapped by name:encrypt_key
					}
				}
			} // Status code equal OK 
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return encrypt_key;
	}
	
	/**
	 * Upload the file information to specified url.
	 * @param strUploadFileInfoUrl The url of the file information server.
	 * @param user_id The ID of the user.
	 * @param file_md5 The md5 value of the file.
	 * @param file_sha1 The sha1 value of the file.
	 * @param encrypt_level The encrypt level of the file.
	 * @param encrypt_key The encrypt key of the file.
	 * @return True, if the file information uploaded successfully.
	 */
	public boolean uploadFileInfo(String strUploadFileInfoUrl, String user_id,
			String file_md5, String file_sha1, String encrypt_level, String encrypt_key){
		
		boolean status = false;	// The flag to indicate the return status.
		
		HttpPost httpRequest =new HttpPost(strUploadFileInfoUrl); // Construct a new HttpPost instance according to the uri
		
		// use name-value pair to store the parameters to pass
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("user_id", user_id)); 			// Add the user_id name-value
		params.add(new BasicNameValuePair("file_md5", file_md5)); 			// Add the file_md5 name-value
		params.add(new BasicNameValuePair("file_sha1", file_sha1)); 		// Add the file_sha1 name-value
		params.add(new BasicNameValuePair("encrypt_level", encrypt_level)); 	// Add the encrypt_level name_value
		params.add(new BasicNameValuePair("encrypt_key", encrypt_key)); 	// Add the encrypt_key name-value
		
		logUtils.d(tag, "params to send:" + params.toString());	// Log out the parameters
		
		try{
			
			// Encode the entity with utf8, and send the entity to the request
			httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8)); 
			
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			status = false;
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
					
					logUtils.d(tag, info); // Log out the returned info
					
					JSONObject jsonObject=null;
					// Flag to indicate whether login succeeded, others to store the data from server
					
					String flag = ""; 	// The flag to indicate the upload status.
					try {
						jsonObject = new JSONObject(info); // Construct an JsonObject instance from the name-value Json string
						flag = jsonObject.getString("flag"); // Get the value mapped by name:flag							
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						status = false;
					}
					
					// Judge whether the validation if succeeded according to the flag
					if(flag.equals("insert")
							|| flag.equals("update")) { // If the operation type is insert or update, set status as true
						status = true;
						String md5 = jsonObject.getString("file_md5");
						String sha1 = jsonObject.getString("file_sha1");
						logUtils.d(tag, "md5:" + md5 + "\nsha1:" + sha1);
					} else { // If the operation type is unknown or some other errors, set status as false
						status = false;
					}
				} else { // Entity is null
					status = false;
				}
			} // Status code equal ok 
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = false;
		}
		return status;
	}
	
	/**
	 * Check the file information from the specified url.
	 * @param strCheckFileInfoUrl The url of the check file inforamtion server.
	 * @param user_id The ID of the user.
	 * @param file_md5 The md5 value of the file.
	 * @param file_sha1 The sha1 value of the file.
	 * @return The HashMap that includes the encrypt_level and encrypt_key.
	 */
	public HashMap<String, String> checkFileInfo(String strCheckFileInfoUrl, String user_id,
			String file_md5, String file_sha1){
		
		HashMap<String, String> resultHashMap = new HashMap<String, String>();	// The flag to indicate the return status.
		
		HttpPost httpRequest =new HttpPost(strCheckFileInfoUrl); // Construct a new HttpPost instance according to the uri
		
		// use name-value pair to store the parameters to pass
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("user_id", user_id)); 			// Add the user_id name-value
		params.add(new BasicNameValuePair("file_md5", file_md5)); 			// Add the file_md5 name-value
		params.add(new BasicNameValuePair("file_sha1", file_sha1)); 		// Add the file_sha1 name-value
		
		logUtils.d(tag, "params to send:" + params.toString());	// Log out the parameters
		
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
					
					logUtils.d(tag, info); // Log out the returned info
					
					JSONObject jsonObject=null;
					
					// Flag to indicate whether login succeeded, others to store the data from server
					String flag = ""; 	// The flag to indicate the upload status.
					
					// The string to store the encrypt level(choose algorithm according to the encrypt level).
					String encrypt_level;
					
					// The string to store the encrypt key.
					String encrypt_key;
//					String sessionid = "";	// The session id.
					
					jsonObject = new JSONObject(info); // Construct an JsonObject instance from the name-value Json string
					flag = jsonObject.getString("flag"); // Get the value mapped by name:flag
//						sessionid = jsonObject.getString("sessionid"); // Get the value mapped by name:sessionid
					
					// Judge whether the validation if succeeded according to the flag
					if(flag.equals("true")) { // If the flag is true, put the encrypt_level and encrypt_key to the resultHashMap.
						
						// Set values to record the file-related information.
						encrypt_level = jsonObject.getString("encrypt_level");
						encrypt_key = jsonObject.getString("encrypt_key");
						
						resultHashMap.put("encrypt_level", encrypt_level);
						resultHashMap.put("encrypt_key", encrypt_key);
						
						logUtils.d(tag, "encrypt_key:" + encrypt_key + "encrypt_level" + encrypt_level);
						
					} else { // The flag is false.
						resultHashMap = null;
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultHashMap = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultHashMap = null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultHashMap = null;
		}
		return resultHashMap;
	}
}
