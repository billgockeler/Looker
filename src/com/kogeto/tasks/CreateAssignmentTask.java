package com.kogeto.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kogeto.looker.model.AssignmentResult;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;
import com.kogeto.looker.widget.AlertDialog;



///////////////////// CREATE ASSIGNMENT TASK /////////////////////
public class CreateAssignmentTask extends AsyncTask<Void, Void, AssignmentResult> {
	
	private final static String TAG = "CreateAssignmentTask";
	
	Context m_context;
	TaskListener m_listener;
	ProgressDialog m_progress_dialog;
	
	
	
	public CreateAssignmentTask(Context context, TaskListener listener) {
		m_context = context;
		m_listener = listener;
	}
	

	
	protected void onPreExecute(){
		m_progress_dialog = new ProgressDialog(m_context, ProgressDialog.THEME_HOLO_LIGHT);
    	m_progress_dialog.show();			
	}
	
	
	
	protected AssignmentResult doInBackground(Void... voids) {
		String host = Constants.WEB_SERVICES.HOST;
		String endpoint = Constants.WEB_SERVICES.CREATE_ASSIGNMENT;
		
 		String url = host + endpoint;

		AssignmentResult assignment_result = null;
    	String json_string = new WebServices().get(m_context, url);
    		    	
	    if(json_string != null){
	    	try{
	    		Gson gson = new GsonBuilder().create();
	    		assignment_result = gson.fromJson(json_string, AssignmentResult.class);
	         }
	    	catch(Exception e) {
	         	 Log.e(TAG, "Error parsing result: " + e.toString());
	        }    
	    }
	    		    
	    return assignment_result;
	    
	}

	
	
    protected void onPostExecute(AssignmentResult assignment_result) {
		m_progress_dialog.hide();

		if(assignment_result != null){
        	if(assignment_result.stat.equalsIgnoreCase("fail")){
        		
    			final AlertDialog dialog = new AlertDialog(m_context, "Error", assignment_result.err.msg);

    			dialog.show();

    			dialog.setOKListener(new View.OnClickListener() {
    				public void onClick(View v) {
    					dialog.cancel();
    					m_listener.finished(null);
    				}
    			});
    			
        	}
        	else if(assignment_result.stat.equalsIgnoreCase("ok")){
            	m_listener.finished(assignment_result);
        	}
        }
		else{
			final AlertDialog dialog = new AlertDialog(m_context, "Assignment Error", "Unexpected error.");
			
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

