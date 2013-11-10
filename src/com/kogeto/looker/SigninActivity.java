package com.kogeto.looker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kogeto.looker.model.SigninResult;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;
import com.kogeto.looker.widget.AlertDialog;

public class SigninActivity extends Activity {
	
	public static final int SIGNIN_SUCCESS = 100;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		TextView forgot_text = (TextView)findViewById(R.id.forgot_text);
		forgot_text.setText( Html.fromHtml("<a href=\"http://www.kogeto.com\">Forgot Password?</a>"));
		forgot_text.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView cancel_button = (TextView)findViewById(R.id.left_button);
		cancel_button.setVisibility(View.VISIBLE);
		cancel_button.setText("Cancel");
		cancel_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				finish();
			}
		});
		
		TextView register_button = (TextView)findViewById(R.id.signin_button);
		register_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				signin(v);
			}
		});
	}

	
	
	public void signin(View v){
		SigninCredentials creds = new SigninCredentials();
		
		EditText email_edit = (EditText)findViewById(R.id.email_edit);
		creds.email = email_edit.getText().toString();

		EditText password_edit = (EditText)findViewById(R.id.password_edit);
		creds.password = password_edit.getText().toString();

		new SigninTask(new SigninTaskListener(){
			public void finished(){
            	Toast.makeText(SigninActivity.this, "You are now signed in", Toast.LENGTH_LONG).show();
				setResult(SIGNIN_SUCCESS);
				finish();
			}
		}).execute(creds);
	}
	
	
	
	class SigninCredentials {
		public String email = null;
		public String password = null;
	}
	
	
	
	////////////////////////// SIGNIN LISTENER TASK ////////////////////////// 
	private interface SigninTaskListener {
		public void finished();
	}

	
	
	//////////////////////////// SIGNIN TASK //////////////////////////// 
	private class SigninTask extends AsyncTask<SigninCredentials, Void, SigninResult> {

		private static final String TAG = "SigninTask";
		
		ProgressDialog progress;
		SigninTaskListener listener;
		
		
		
		public SigninTask(SigninTaskListener listener){
			this.listener = listener;
		}
		
		
		
		protected void onPreExecute(){	
			progress = new ProgressDialog(SigninActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			progress.setMessage("Signing you in...");
			progress.show();
		}
		
		
		
		protected SigninResult doInBackground(SigninCredentials... credentials) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.SIGNIN;
    		
    		if(credentials != null){
        		endpoint = endpoint.replace("{email}", credentials[0].email);
        		endpoint = endpoint.replace("{password}", credentials[0].password);
    		}

     		String url = host + endpoint;

    		SigninResult signin_result = null;
    		
	    	String json_string = new WebServices().get(SigninActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
	    			signin_result = gson.fromJson(json_string, SigninResult.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return signin_result;
		}

		
	    protected void onPostExecute(SigninResult signin_result) {
			progress.hide();

			if(signin_result != null){
	        	if(signin_result.stat.equalsIgnoreCase("fail")){
	        		
	    			final AlertDialog dialog = new AlertDialog(SigninActivity.this, "Sign in Error", signin_result.err.msg);
	    			
	    			dialog.show();

	    			dialog.setOKListener(new View.OnClickListener() {
	    				public void onClick(View v) {
	    					dialog.cancel();
	    				}
	    			});
	        	}
	        	else if(signin_result.stat.equalsIgnoreCase("ok")){
	        		
	        		if(signin_result.user.looker_token == null){
	        			final AlertDialog dialog = new AlertDialog(SigninActivity.this, "Sign in Error", "Token is null");
	        			
	        			dialog.show();

	        			dialog.setOKListener(new View.OnClickListener() {
	        				public void onClick(View v) {
	        					dialog.cancel();
	        				}
	        			});
	        		}
	        		else {
		        		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SigninActivity.this).edit();
		                editor.putString(Constants.PREFERENCES.LOOKER_TOKEN, signin_result.user.looker_token).commit();
		                
		                if(this.listener != null){
		                	this.listener.finished();
		                }
	        		}
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(SigninActivity.this, "Signin Error", "Unexpected error.");
				
				dialog.show();

				dialog.setOKListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
					}
				});
			}
	        
	    }
	    
	}
	
}
