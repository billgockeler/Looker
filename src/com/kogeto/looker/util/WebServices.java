package com.kogeto.looker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kogeto.tasks.UploadVideoTask.ProgressListener;

public class WebServices {

	public static final String TAG = "WebServices";

	
	
	public synchronized String get(Context context, String url){
		
		//see if a user token is required, if so add it to the request
		if(url.contains("{looker_token}")){			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			String user_token = preferences.getString(Constants.PREFERENCES.LOOKER_TOKEN, "");
			url = url.replace("{looker_token}", user_token);
		}
		
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet http_get = new HttpGet(url);
		
		String result_string = null;
		
        try{
            
            HttpResponse response = client.execute(http_get);
			StatusLine status_line = response.getStatusLine();
			int status_code = status_line.getStatusCode();
			if(status_code == HttpStatus.SC_OK){
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = reader.readLine()) != null){
					builder.append(line);
				}
				
				result_string = builder.toString();
			}
			else{
				Log.e(TAG, "GET with url: " + url + " failed with status code: " + status_code);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	
		return result_string;
	}
	
	
	
	
	public synchronized HttpResponse put(Context context, String url, File file, final ProgressListener listener){
		HttpResponse response = null;
		
		try{
			//see if a user token is required, if so add it to the request
			if(url.contains("{looker_token}")){			
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				String user_token = preferences.getString(Constants.PREFERENCES.LOOKER_TOKEN, "");
				url = url.replace("{looker_token}", user_token);
			}
			
			String host = new URL(url).getHost();

			HttpClient httpclient = new DefaultHttpClient();
			HttpPut http_put = new HttpPut(url);	
			http_put.setHeader("Content-Type", "video/mp4");
			http_put.setHeader("Host", host);
      		http_put.setEntity(new FileEntity(file, "binary/octet-stream"){
      			
      			@Override
      			public void writeTo(final OutputStream outstream) throws IOException {
      				long bytes_total = file.length();
      				long bytes_written = 0;
      				int percent = 0;
      				
      				if (outstream == null) {
      		        	throw new IllegalArgumentException("Output stream may not be null");
      		        }
      		        
      		        InputStream instream = new FileInputStream(this.file);
      		        try {
      		        	byte[] tmp = new byte[4096];
      		        	int l;
      		        	while ((l = instream.read(tmp)) != -1) {
      		        		bytes_written += tmp.length;
      		        		percent = (int)((bytes_written/new Long(bytes_total).floatValue()) * 100);
      		        		Log.e(TAG, "Bytes written: " + bytes_written + ", percent complete: " + percent);
      		        		listener.progress(percent);
      		        		outstream.write(tmp, 0, l);
      		        	}
      		        	outstream.flush();
      		        } 
      		        finally {
      		        	instream.close();
      		        }      			    
      			}
      		});
      		
			Log.d(TAG, "uploading file to url: " + url);

      		response = httpclient.execute(http_put);		
		}
		catch(Exception e){
			Log.e(TAG, "PUT with url: " + url + " failed with exception: " + e.getMessage());
			e.printStackTrace();
		}
	        
        return response;
	}
}
