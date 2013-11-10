package com.kogeto.tasks;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kogeto.looker.model.AssignmentResult;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;



///////////////////// UPLOAD VIDEO TASK /////////////////////
public class PutVideoTask extends AsyncTask<Void, Void, Integer> {
	
	private final static String TAG = "UploadVideoTask";
	
	Video m_video;
	Context m_context;
	TaskListener m_listener;
	AssignmentResult m_assignment;
	ProgressDialog m_progress_dialog;
	
	
	public PutVideoTask(Context context, Video video, AssignmentResult assignment, TaskListener listener) {
		m_context = context;
		m_video = video;
		m_assignment = assignment;
		m_listener = listener;
	}
	

	
	protected void onPreExecute(){
		m_progress_dialog = new ProgressDialog(m_context, ProgressDialog.THEME_HOLO_LIGHT);
    	m_progress_dialog.show();			
	}
	
	
	
	protected Integer doInBackground(Void... voids) {
		String url = Constants.WEB_SERVICES.UPLOAD_VIDEO;
		url = url.replace("{bucket}", m_assignment.bucket);
		url = url.replace("{assignment_id}", m_assignment.assignmentid);
		
		File video_file = new File(m_video.vurl);
 		

    	HttpResponse response = new WebServices().put(m_context, url, video_file, null);
    		    	
    	Log.d(TAG, "Uploading video with url: " + url);
    	
    	Integer status_code = 0;
        String status_reason;
    	
        try{
			//if we got a response then read that response into a string (a json string representing the shout object
	        if(response != null){
	        	status_code = response.getStatusLine().getStatusCode();
	        	status_reason = EntityUtils.toString(response.getEntity());
	        }
        }
	    catch(Exception e){
		    Log.e(TAG, e.getMessage());
	    }
        
	    		    
	    return status_code;
	}

	
	
    protected void onPostExecute(Integer status_code) {
		m_progress_dialog.hide();

		if(status_code != null){
        	if(status_code.intValue() == HttpStatus.SC_OK){
        		Toast.makeText(m_context, "Finished uploading video", Toast.LENGTH_LONG);
        	}
        	else {
        		Toast.makeText(m_context, "Failed uploading video", Toast.LENGTH_LONG);
        	}
        }
		else{
    		Toast.makeText(m_context, "Failed uploading video", Toast.LENGTH_LONG);
		}
    }
    
}

