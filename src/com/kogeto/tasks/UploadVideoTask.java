package com.kogeto.tasks;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kogeto.looker.R;
import com.kogeto.looker.SigninActivity;
import com.kogeto.looker.db.VideosDataSource;
import com.kogeto.looker.model.AssignmentResult;
import com.kogeto.looker.model.Result;
import com.kogeto.looker.model.UploadVideoResult;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;
import com.kogeto.looker.widget.AlertDialog;
import com.kogeto.looker.widget.MessageDialog;



///////////////////// UPLOAD VIDEO TASK /////////////////////
public class UploadVideoTask extends AsyncTask<Void, Integer, UploadVideoResult> {
	
	private final static String TAG = "UploadVideoTask";
	
	Video m_video;
	Activity m_activity;
	UploadVideoTaskListener m_listener;
	ProgressDialog m_progress_dialog;
	
	
	
	public UploadVideoTask(Activity activity, Video video, UploadVideoTaskListener listener) {
		m_video = video;
		m_activity = activity;
		m_listener = listener;
	}
	

	
	protected void onPreExecute(){
		m_progress_dialog = new ProgressDialog(m_activity, ProgressDialog.THEME_HOLO_LIGHT);
		m_progress_dialog.setMessage("Uploading video...");
		m_progress_dialog.setIndeterminate(false);
		m_progress_dialog.setMax(100);
		m_progress_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_progress_dialog.setProgressDrawable(m_activity.getResources().getDrawable(R.drawable.looker_progressbar));
		m_progress_dialog.show();			
	}
	
	
	
	protected UploadVideoResult doInBackground(Void... voids) {
		
		
		Gson gson = new GsonBuilder().create();
		String host = Constants.WEB_SERVICES.HOST;
		UploadVideoResult video_result;
 		
		//////////////// CREATE ASSIGNMENT /////////////////
        m_activity.runOnUiThread(new Runnable() {
            public void run() {
            	m_progress_dialog.setMessage("Creating assignment");
            }
        });

        String create_url = host + Constants.WEB_SERVICES.CREATE_ASSIGNMENT;
		
		AssignmentResult create_assignment = null;
    	String json_string = new WebServices().get(m_activity, create_url);
    		    	
	    if(json_string != null){
	    	try{
	    		create_assignment = gson.fromJson(json_string, AssignmentResult.class);
	         }
	    	catch(Exception e) {
	         	 Log.e(TAG, "Error creating assignment: " + e.getMessage());
	         	 
	         	 video_result = new UploadVideoResult();
	         	 video_result.stat = Result.FAIL;
	         	 video_result.err.msg = "Error in create assignment:\n" + e.getMessage();
	         	 return video_result;
	        }    
	    }
	    
	    if(create_assignment == null){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.msg = "Error in create assignment. Assignment is null";
	    	 return video_result;
	    }
	    else if(!create_assignment.isOk()){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.code = create_assignment.err.code;
	    	 video_result.err.msg = "Error in create assignment:\n" + create_assignment.err.msg;
        	 return video_result;
	    }
	    
	    
        ////////////////// CHECK ASSIGNMENT ////////////////////
        m_activity.runOnUiThread(new Runnable() {
            public void run() {
            	m_progress_dialog.setMessage("Checking assignment");
            }
        });

		String check_url = host + Constants.WEB_SERVICES.CHECK_ASSIGNMENT;
		check_url = check_url.replace("{assignment_id}", create_assignment.assignmentid);
		
		AssignmentResult check_assignment = null;
    	json_string = new WebServices().get(m_activity, check_url);
    	
	    if(json_string != null){
	    	try{
	    		check_assignment = gson.fromJson(json_string, AssignmentResult.class);
	         }
	    	catch(Exception e) {
	         	 Log.e(TAG, "Error in check assignment: " + e.toString());
	         	 
	        	 video_result = new UploadVideoResult();
	        	 video_result.stat = Result.FAIL;
	        	 video_result.err.msg = "Error in check assignment:\n" + e.getMessage();
	        	 return video_result;
	        }    
	    }
	    
	    if(check_assignment == null){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.msg = "Error in check assignment. Assignment is null";
	    	 return video_result;
	    }
	    else if(!check_assignment.isOk() || !check_assignment.isValid() ){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.code = check_assignment.err.code;
	    	 video_result.err.msg = "Error in check assignment:\n" + check_assignment.err.msg;
       	 return video_result;
	    }
	    
	    ///////////////////// UPLOAD VIDEO ////////////////////	    
        m_activity.runOnUiThread(new Runnable() {
            public void run() {
            	m_progress_dialog.setMessage("Uploading video");
            }
        });

        String upload_url = Constants.WEB_SERVICES.UPLOAD_VIDEO;
		upload_url = upload_url.replace("{bucket}", create_assignment.bucket);
		upload_url = upload_url.replace("{assignment_id}", create_assignment.assignmentid);
		
		File video_file = new File(m_video.vurl);
		
    	HttpResponse response = new WebServices().put(m_activity, upload_url, video_file, new ProgressListener(){
    		public void progress(int percent){
    			publishProgress(percent);
    		}
    	});
    		    	
    	Integer status_code = 0;
        String status_reason = null;
    	
        try{
			//if we got a response then read that response into a string 
	        if(response != null){
	        	status_code = response.getStatusLine().getStatusCode();
	        	status_reason = EntityUtils.toString(response.getEntity());
	        	
	            if(status_code != HttpStatus.SC_OK){
	            	video_result = new UploadVideoResult();
	            	video_result.stat = Result.FAIL;
	            	video_result.err.msg = "Error during upload:\n" + status_reason;
	            	return video_result;
	            }
	        }
        }
	    catch(Exception e){
        	 Log.e(TAG, "Error in uploading file: " + e.getMessage());
         	 
        	 video_result = new UploadVideoResult();
        	 video_result.stat = Result.FAIL;
        	 video_result.err.msg = "Error during upload:\n" + e.getMessage();
        	 return video_result;
	    }
        
        //////////////////// END ASSIGNMENT ////////////////////
        //end the assignment for the video and get the video key
        m_activity.runOnUiThread(new Runnable() {
            public void run() {
            	m_progress_dialog.setMessage("Ending assignment");
            }
        });

        String end_url = host + Constants.WEB_SERVICES.END_ASSIGNMENT;
		end_url = end_url.replace("{assignment_id}", create_assignment.assignmentid);
		
		AssignmentResult end_assignment = null;
    	json_string = new WebServices().get(m_activity, end_url);
    	
	    if(json_string != null){
	    	try{
	    		end_assignment = gson.fromJson(json_string, AssignmentResult.class);
	         }
	    	catch(Exception e) {
	         	 Log.e(TAG, "Error in end assignment: " + e.toString());
	         	 
	        	 video_result = new UploadVideoResult();
	        	 video_result.stat = Result.FAIL;
	        	 video_result.err.msg = "Error in create assignment:\n" + e.getMessage();
	        	 return video_result;
	        }    
	    }
	    
	    if(end_assignment == null){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.msg = "Error in end assignment. Assignment is null";
	    	 return video_result;
	    }
	    if(!end_assignment.isOk() || end_assignment.videokey == null){
	    	 video_result = new UploadVideoResult();
	    	 video_result.stat = Result.FAIL;
	    	 video_result.err.code = end_assignment.err.code;
	    	 video_result.err.msg = "Error in end assignment:\n" + end_assignment.err.msg;
	    	 return video_result;
	    }

	    //insert the server video key into the video object
	    m_video.videokey = end_assignment.videokey;
	    
	    //save the video (with the new video key) to the local db
        VideosDataSource datasource = new VideosDataSource(m_activity);
        datasource.open();
        datasource.update(m_video);
        datasource.close();
        
   	 	video_result = new UploadVideoResult();
   	 	video_result.stat = Result.OK;
   	 	video_result.video = m_video;
   	 	
        return video_result;
	}

	
	
	  protected void onProgressUpdate(Integer... percent) {        
		  m_progress_dialog.setProgress(percent[0]);
	    }
	
	
    protected void onPostExecute(UploadVideoResult video_result) {
		m_progress_dialog.hide();

		if(video_result != null && video_result.video != null){
			Toast.makeText(m_activity, "Upload completed", Toast.LENGTH_LONG);
			if(m_listener != null){
				m_listener.finished(video_result);
			}
        }
		else{
			//error 109 is if the user needs to re-login so clear their token and ask to login again
			if(video_result.err.code == 109){
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_activity);
				preferences.edit().putString(Constants.PREFERENCES.LOOKER_TOKEN, null).commit();
				
				final MessageDialog dialog = new MessageDialog(m_activity, "Session Expired", "Your session has expired or you signed in with another device. You'll have to sign in again before sharing this video.\n\nWould you like to do that now?");

				dialog.show();
				
				dialog.setPositiveListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent signin_intent = new Intent(m_activity, SigninActivity.class);
						m_activity.startActivity(signin_intent);
					}
				});
				
				dialog.setNegativeListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
					}
				});
			}
			else{
				
				final AlertDialog dialog = new AlertDialog(m_activity, "Upload Error", video_result.err.msg);
	
				dialog.show();
	
				dialog.setOKListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
						m_listener.finished(null);
					}
				});
			}
			
		}
    }
    
    

    ///////////// UPLOAD VIDEO TAK LISTENER ///////////////////
    public interface UploadVideoTaskListener{
		public void finished(UploadVideoResult result);
    }
    
    
    //////////// PROGRESS LISTENER /////////////
    public interface ProgressListener{
    	public void progress(int percent);
    }
    
}

