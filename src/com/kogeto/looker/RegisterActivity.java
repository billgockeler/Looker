package com.kogeto.looker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kogeto.looker.model.RegistrationResult;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;
import com.kogeto.looker.widget.AlertDialog;

public class RegisterActivity extends Activity {

	public static final String SHARE_VIA = "share_via";
	
	private String m_share_via = "";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 Bundle bundle = getIntent().getExtras();
		 if(bundle != null){
			 m_share_via = bundle.getString(SHARE_VIA);
		 }
		
		setContentView(R.layout.activity_register);

		TextView instruction_text = (TextView)findViewById(R.id.instruction_text);
		String instruction_string = instruction_text.getText().toString();
		instruction_string = instruction_string.replace("{destination}", m_share_via);
		instruction_text.setText(instruction_string);
		
		TextView signin_button = (TextView)findViewById(R.id.right_button);
		signin_button.setVisibility(View.VISIBLE);
		signin_button.setText("Sign in");
		signin_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				signin(v);
			}
		});
		
		
		TextView cancel_button = (TextView)findViewById(R.id.left_button);
		cancel_button.setVisibility(View.VISIBLE);
		cancel_button.setText("Cancel");
		cancel_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				finish();
			}
		});
		
		TextView register_button = (TextView)findViewById(R.id.register_button);
		register_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				register(v);
			}
		});
	}
	
	
	public void signin(View v){
		Intent signin_intent = new Intent(this, SigninActivity.class);
		this.startActivityForResult(signin_intent, 0);
	}
	
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SigninActivity.SIGNIN_SUCCESS) {
        	finish();
        }
	}
	

	public void register(View v){
		RegisterCredentials creds = new RegisterCredentials();
		
		EditText firstname_edit = (EditText)findViewById(R.id.firstname_edit);
		EditText lastname_edit = (EditText)findViewById(R.id.lastname_edit);
		creds.username = 
				firstname_edit.getText().toString().trim().toLowerCase().replace(" ", "'") + 
				lastname_edit.getText().toString().trim().toLowerCase().replace(" ", "'");
		
		EditText email_edit = (EditText)findViewById(R.id.email_edit);
		creds.email = email_edit.getText().toString();

		EditText password_edit = (EditText)findViewById(R.id.password_edit);
		creds.password = password_edit.getText().toString();

		EditText confirm_edit = (EditText)findViewById(R.id.confirm_edit);
		creds.cpassword = confirm_edit.getText().toString();

		
		new RegisterTask(new RegisterTaskListener(){
			public void finished(){
            	Toast.makeText(RegisterActivity.this, "You are now registered", Toast.LENGTH_LONG).show();
				finish();
			}
		}).execute(creds);
	}
	
	
	
	class RegisterCredentials {
		public String username = null;
		public String email = null;
		public String password = null;
		public String cpassword = null;
	}
	
	
	
	//////////////////////// REGISTER LISTENER TASK ////////////////////////// 
	private interface RegisterTaskListener {
		public void finished();
	}

	
	
	//////////////////////////// REGISTER TASK //////////////////////////// 
	private class RegisterTask extends AsyncTask<RegisterCredentials, Void, RegistrationResult> {

		private static final String TAG = "RegisterTask";
		
		ProgressDialog progress;
		RegisterTaskListener listener;
		
		
		
		public RegisterTask(RegisterTaskListener listener){
			this.listener = listener;
		}
		
		
		
		protected void onPreExecute(){	
			progress = new ProgressDialog(RegisterActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			progress.show();
		}
		
		
		
		protected RegistrationResult doInBackground(RegisterCredentials... credentials) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.REGISTER;
    		
    		if(credentials != null){
        		endpoint = endpoint.replace("{username}", credentials[0].username);
        		endpoint = endpoint.replace("{email}", credentials[0].email);
        		endpoint = endpoint.replace("{password}", credentials[0].password);
        		endpoint = endpoint.replace("{cpassword}", credentials[0].cpassword);
    		}

     		String url = host + endpoint;

    		RegistrationResult registration = null;
    		
	    	String json_string = new WebServices().get(RegisterActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
	    			registration = gson.fromJson(json_string, RegistrationResult.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return registration;
		}

		
	    protected void onPostExecute(RegistrationResult registration) {
			progress.hide();

			if(registration != null){
	        	if(registration.stat.equalsIgnoreCase("fail")){
	        		
	    			final AlertDialog dialog = new AlertDialog(RegisterActivity.this, "Registration Error", registration.err.msg);
	    			
	    			dialog.show();

	    			dialog.setOKListener(new View.OnClickListener() {
	    				public void onClick(View v) {
	    					dialog.cancel();
	    				}
	    			});
	        	}
	        	else if(registration.stat.equalsIgnoreCase("ok")){
	        		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this).edit();
	                editor.putString(Constants.PREFERENCES.LOOKER_TOKEN, registration.looker_token).commit();
	                
	                if(this.listener != null){
	                	this.listener.finished();
	                }
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(RegisterActivity.this, "Registration Error", "Unexpected error");

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
