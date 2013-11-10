package com.kogeto.looker.myvideos;

import java.io.File;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kogeto.looker.R;
import com.kogeto.looker.RegisterActivity;
import com.kogeto.looker.db.VideosDataSource;
import com.kogeto.looker.model.RegistrationResult;
import com.kogeto.looker.model.Result;
import com.kogeto.looker.model.UploadVideoResult;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.player.VideoPlayerActivity;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.StringUtil;
import com.kogeto.looker.util.WebServices;
import com.kogeto.looker.widget.AlertDialog;
import com.kogeto.looker.widget.MessageDialog;
import com.kogeto.tasks.UploadVideoTask;
import com.kogeto.tasks.UploadVideoTask.UploadVideoTaskListener;

public class VideoDetailsActivity extends Activity {
	
	public static final String TAG = "VideoDetailsActivity";

	public static final String VIDEO_OBJECT = "video_object";
	
	EditText m_video_description_edit;
	EditText m_video_title_edit;	
	
	boolean m_editing = false;
	Video m_video;
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_details);
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null){
			m_video = bundle.getParcelable(VIDEO_OBJECT);
		}
		
		m_video_title_edit = (EditText) findViewById(R.id.title_edit);
		m_video_title_edit.setText(m_video.title);

		m_video_description_edit = (EditText) findViewById(R.id.description_edit);
		m_video_description_edit.setText(m_video.description);
		
		TextView duration_text = (TextView)findViewById(R.id.duration_text);
		duration_text.setText("Length " + m_video.duration);
		
		TextView created_text = (TextView)findViewById(R.id.created_text);
		created_text.setText("Posted " + StringUtil.getDateString(m_video.date_added));

		ImageView thumbnail_image = (ImageView) findViewById(R.id.thumbnail_image); 

		AQuery image_manager = new AQuery(this);
		image_manager.id(thumbnail_image).image(m_video.turl, true, true, 0, 0);
		
		ImageView play_image = (ImageView) findViewById(R.id.play_image); 
		play_image.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		    	Bundle bundle = new Bundle();
		    	bundle.putString(VideoPlayerActivity.DATASOURCE, m_video.vurl);
				Intent video_player_intent = new Intent(VideoDetailsActivity.this, VideoPlayerActivity.class);
				video_player_intent.putExtras(bundle);
		        startActivity(video_player_intent);
			}
		});
		
    	ImageView more_button = (ImageView)findViewById(R.id.more_button);
		more_button.setVisibility(View.VISIBLE);
		more_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				showMoreActions();
			}
		});
		
		TextView videos_button = (TextView)findViewById(R.id.left_button);
		videos_button.setVisibility(View.VISIBLE);
		videos_button.setText("Videos");
		videos_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				finish();
			}
		});
		
		TextView delete_button = (TextView)findViewById(R.id.delete_button);
		delete_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				delete();
			}
		});
		
		TextView cancel_button = (TextView)findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				hideMoreActions();
			}
		});
		
	}
	

	
	protected void onPause(){
		String title = m_video_title_edit.getText().toString();
		String description = m_video_description_edit.getText().toString();
		
		if(!title.equalsIgnoreCase(m_video.title) || !description.equalsIgnoreCase(m_video.description)){
			m_video.title = title;
			m_video.description = description;
			
			VideosDataSource datasource = new VideosDataSource(this);
			datasource.open();
			datasource.update(m_video);
			datasource.close();
		}
		
		super.onPause();
	}
	
	

    public boolean signedIn(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String user_token = preferences.getString(Constants.PREFERENCES.LOOKER_TOKEN, null);
		
		if(user_token == null){
			Intent video_player_intent = new Intent(getApplicationContext(), RegisterActivity.class);
	        startActivity(video_player_intent);
	        return false;
    	}
    	else{
    		return true;
    	}
    }

    
    
    public void showMoreActions(){
    	View video_actions = findViewById(R.id.video_actions);
    	video_actions.setVisibility(View.VISIBLE);
	}
	
	
	
	public void hideMoreActions(){
    	View video_actions = findViewById(R.id.video_actions);
    	video_actions.setVisibility(View.INVISIBLE);
	}
	
	
	
	public void delete(){
		final MessageDialog dialog = new MessageDialog(this, "Delete?", "Are you sure you want to delete the video?");

		dialog.show();

		dialog.setPositiveListener(new View.OnClickListener() {
			public void onClick(View v) {
				//delete the video and thumbnail files and the database entry
				File video_file = new File(m_video.vurl);
				video_file.delete();
				
				File thumbnail_file = new File(m_video.turl);
				thumbnail_file.delete();
				
				VideosDataSource datasource = new VideosDataSource(VideoDetailsActivity.this);
				datasource.open();
				datasource.delete(m_video);
				datasource.close();
				finish();
				dialog.cancel();
			}
		});
		
		dialog.setNegativeListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
			}
		});
	}
	
	
	
	//check if the user is signed
	private boolean isSignedIn(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String user_token = preferences.getString(Constants.PREFERENCES.LOOKER_TOKEN, null);
		
		if(user_token != null){
			return true;
		}
		else{
	        return false;
    	}
	}
	
	
	
	//open the register activity
	private void register(String share_type){
		Bundle bundle = new Bundle();
    	bundle.putString(RegisterActivity.SHARE_VIA, share_type);
    	Intent intent = new Intent(this, RegisterActivity.class);
		intent.putExtras(bundle);
        startActivity(intent);
	}
	
	
	
	//share the video on facebook
	public void share_facebook(View v){
		if(isSignedIn()){
			
			if(m_video.videokey != null && !m_video.videokey.equalsIgnoreCase("")){
				postFacebook();
			}
			else{
				new UploadVideoTask(this, m_video, new UploadVideoTaskListener(){
					public void finished(final UploadVideoResult result){
						if(result != null && result.video != null && result.video.videokey != null){
							m_video = result.video;
							postFacebook();
						}
					}
				}).execute();
			}
		}
		else{
			register("Facebook");
		}
	}
	
	
	
	//post the video on facebook
	private void postFacebook(){
    	boolean authenticated = Constants.FACEBOOK.facebook.isSessionValid();
    	
    	if(!authenticated){
    		authorizeFacebook();
		}
    	else{
    		new ShareFacebookTask().execute();
    	}
		
	}
	
	
	
	//share the video on twitter
	public void share_twitter(View v){
		
		if(isSignedIn()){
			
			if(m_video.videokey != null && !m_video.videokey.equalsIgnoreCase("")){
				postTwitter();
			}
			else{
				new UploadVideoTask(this, m_video, new UploadVideoTaskListener(){
					public void finished(final UploadVideoResult result){
						if(result != null && result.video != null && result.video.videokey != null){
							m_video = result.video;
							postTwitter();
						}
					}
				}).execute();
			}
		}
		else{
			register("Twitter");
		}
	}
	
	
	
	private void postTwitter(){
		//check if we have a valid twitter session
		boolean authenticated = twitterAuthenticated();
    	
		//if not, then start the twitter authorization process
    	if(!authenticated){
    		authorizeTwitter();
		}
    	//if so, then start a task to complete the share
    	else{
    		new ShareTwitterTask().execute();
    	}
	}

	
	
	//share the video on the kogeto website
	public void share_kogeto(View v){

		if(isSignedIn()){
			
			if(m_video.videokey != null && !m_video.videokey.equalsIgnoreCase("")){
				promptViewVideo();
			}
			else{
				new UploadVideoTask(this, m_video, new UploadVideoTaskListener(){
					public void finished(final UploadVideoResult result){
						if(result != null && result.video != null && result.video.videokey != null){
							m_video = result.video;
							promptViewVideo();
						}
					}
				}).execute();
			}
		}
		else{
			register("Kogeto");
		}
	}

	
	
	//share the video via email
	public void share_email(View v){
		if(isSignedIn()){
			
			if(m_video.videokey != null && !m_video.videokey.equalsIgnoreCase("")){
				sendEmail();
			}
			else{
				new UploadVideoTask(this, m_video, new UploadVideoTaskListener(){
					public void finished(final UploadVideoResult result){
						if(result != null && result.video != null && result.video.videokey != null){
							m_video = result.video;
							sendEmail();
						}
					}
				}).execute();
			}
		}
		else{
			register("email");
		}
	}
	
	
	
	//send am email with a link to the video
	private void sendEmail(){
		StringBuilder builder = new StringBuilder("I created this video on Looker and wanted to share it with you. The video can be viewed ");
		
		Spanned formatted_message = Html.fromHtml(builder.append("<a href=" + Constants.WEB_SERVICES.DOTSPOT_HOST + m_video.videokey + ">here</a>").toString());

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Check Out My Looker Video");
		intent.putExtra(Intent.EXTRA_TEXT, formatted_message);
		
		try {
		    startActivity(Intent.createChooser(intent, "Send mail..."));
		} 
		catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}		
	}
	
	
	
	//show the user a dialog to ask if they want to view the video on the web
	private void promptViewVideo(){
		final MessageDialog dialog = new MessageDialog(VideoDetailsActivity.this, "Upload Complete", 
				"Your video can be found at " + Constants.WEB_SERVICES.DOTSPOT_HOST + m_video.videokey + "\n\n" +
				"Would you like to view it now?");

		dialog.show();
		
		dialog.setPositiveListener("Yes", new View.OnClickListener() {
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WEB_SERVICES.DOTSPOT_HOST + m_video.videokey));
				startActivity(browserIntent);
				dialog.cancel();
			}
		});
		
		dialog.setNegativeListener("No", new View.OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
			}
		});		
	}
	
	
	
    //FACEBOOK
    public void authorizeFacebook(){
		try {
			Constants.FACEBOOK.facebook.authorize(this, Constants.FACEBOOK.PERMISSIONS, new DialogListener() {
			    public void onComplete(Bundle values) {
			    	Log.d(TAG, "addFacebook.onComplete: values=" + values.toString());
			    	SessionStore.save(Constants.FACEBOOK.facebook, VideoDetailsActivity.this);
			    	
			    	String access_token = Constants.FACEBOOK.facebook.getAccessToken();
			    	
			        new AddFacebookTask().execute(access_token);
			        
			    	Log.d(TAG, "addFacebook.onComplete: ");
			    }
			    public void onFacebookError(FacebookError error) {
			    	Log.e(TAG, "addFacebook.onFacebookError: " + error);
			    }
			    public void onError(DialogError error) {
			    	Log.e(TAG, "addFacebook.onError: " + error);
			    }
			    public void onCancel() {
			    	Log.e(TAG, "addFacebook.onCancel: ");
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Constants.FACEBOOK.facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
   
    
    //TWITTER
    public void authorizeTwitter(){
		try {
			Intent i = new Intent(this, PrepareRequestTokenActivity.class);
			startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


    
	public boolean twitterAuthenticated() {
		try{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
	
			try{
				AccessToken a = new AccessToken(token,secret);
				Twitter twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(Constants.TWITTER.CONSUMER_KEY, Constants.TWITTER.CONSUMER_SECRET);
				twitter.setOAuthAccessToken(a);
			
		        twitter.getId();
				return true;
			} 
			catch (Exception e) {
				return false;
			}
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    	return false;
	    }
	}

	
	
	/////////////// SEND USER'S FACEBOOK ID AND TOKEN TO THE KOGETO SERVER /////////////////
    private class AddFacebookTask extends AsyncTask<String, Void, Result> {
    	
    	ProgressDialog progress_dialog;

		protected void onPreExecute(){
			progress_dialog = new ProgressDialog(VideoDetailsActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
	    	progress_dialog.show();			
		}
		
		protected Result doInBackground(String... access_token) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.ADD_FACEBOOK;
    		endpoint = endpoint.replace("{facebookid}", Constants.FACEBOOK.FACEBOOK_ID);
    		
    		if(access_token != null && access_token[0] != null){
    			endpoint = endpoint.replace("{access_token}", access_token[0]);
    		}

     		String url = host + endpoint;

    		Result result = null;
	    	String json_string = new WebServices().get(VideoDetailsActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
		    		result = gson.fromJson(json_string, Result.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return result;
		}

		
	    protected void onPostExecute(RegistrationResult registration) {
			progress_dialog.hide();

			if(registration != null){
	        	if(registration.stat.equalsIgnoreCase("fail")){
	        		
					final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Registration Error", registration.err.msg);
					
					dialog.show();

					dialog.setOKListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.cancel();
						}
					});
	        	}
	        	else if(registration.stat.equalsIgnoreCase("ok")){
	            	Toast.makeText(VideoDetailsActivity.this, "Facebook share authorized", Toast.LENGTH_LONG).show();
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Registration Error", "Unexpected error.");
				
				dialog.show();

				dialog.setOKListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
					}
				});
			}
	    }
	}

    
    
	////////////////// SEND TWITTER ID, TOKEN AND SECRET TO THE KOGETO SERVER ///////////////////
    public class AddTwitterTask extends AsyncTask<String, Void, Result> {
    	
    	ProgressDialog progress_dialog;
    	
    	String m_twitter_id;
    	
    	
    	
    	public AddTwitterTask(String twitter_id){
    		m_twitter_id = twitter_id;
    	}

		protected void onPreExecute(){
			progress_dialog = new ProgressDialog(VideoDetailsActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
	    	progress_dialog.show();			
		}
		
		protected Result doInBackground(String... access_token) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.ADD_TWITTER;

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(VideoDetailsActivity.this);
			
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
    		
    		endpoint = endpoint.replace("{twitterid}", m_twitter_id);
    		endpoint = endpoint.replace("{twitter_token}", token);
    		endpoint = endpoint.replace("{twitter_secret}", secret);
    		
    		if(access_token != null && access_token[0] != null){
    			endpoint = endpoint.replace("{access_token}", access_token[0]);
    		}

     		String url = host + endpoint;

    		Result result = null;
	    	String json_string = new WebServices().get(VideoDetailsActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
		    		result = gson.fromJson(json_string, Result.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return result;
		}

		
	    protected void onPostExecute(RegistrationResult registration) {
			progress_dialog.hide();

			if(registration != null){
	        	if(registration.stat.equalsIgnoreCase("fail")){
	        		
					final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Registration Error", registration.err.msg);
					
					dialog.show();

					dialog.setOKListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.cancel();
						}
					});
	        	}
	        	else if(registration.stat.equalsIgnoreCase("ok")){
	            	Toast.makeText(VideoDetailsActivity.this, "Facebook share authorized", Toast.LENGTH_LONG).show();
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Registration Error", "Unexpected error.");
				
				dialog.show();

				dialog.setOKListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
					}
				});
			}
	        
	    }
	    
	}

    

    ////////////////// SHARE THE VIDEO ON FACEBOOK ///////////////////
    private class ShareFacebookTask extends AsyncTask<String, Void, Result> {
    	
    	ProgressDialog progress_dialog;

		protected void onPreExecute(){
			progress_dialog = new ProgressDialog(VideoDetailsActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
	    	//progress_dialog.setMessage("Getting Facebook ID...");
	    	//progress_dialog.setInverseBackgroundForced(true);
	    	progress_dialog.show();			
		}
		
		protected Result doInBackground(String... access_token) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.SHARE_FACEBOOK;
    		endpoint = endpoint.replace("{video_key}", m_video.videokey);
    		endpoint = endpoint.replace("{facebook_message}", "Check out the video I just captured with the Looker App " + Constants.WEB_SERVICES.DOTSPOT_HOST + m_video.videokey);
    		endpoint = endpoint.replace(" ", "%20");
    		
     		String url = host + endpoint;

    		Result result = null;
	    	String json_string = new WebServices().get(VideoDetailsActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
		    		result = gson.fromJson(json_string, Result.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return result;
		}

		
	    protected void onPostExecute(Result result) {
			progress_dialog.hide();

			if(result != null){
	        	if(result.stat.equalsIgnoreCase("fail")){
	        		
					final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Share Error", result.err.msg);
					
					dialog.show();

					dialog.setOKListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.cancel();
						}
					});
	        	}
	        	else if(result.stat.equalsIgnoreCase("ok")){
	            	Toast.makeText(VideoDetailsActivity.this, "Shared on Facebook", Toast.LENGTH_SHORT).show();
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Share Error", "Unexpected error.");
				
				dialog.show();

				dialog.setOKListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();
					}
				});
			}
	    }
	}

    

    ////////////////// SHARE THE VIDEO ON TWITTER ///////////////////
    private class ShareTwitterTask extends AsyncTask<String, Void, Result> {
    	
    	ProgressDialog progress_dialog;

		protected void onPreExecute(){
			progress_dialog = new ProgressDialog(VideoDetailsActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
	    	//progress_dialog.setMessage("Getting Facebook ID...");
	    	//progress_dialog.setInverseBackgroundForced(true);
	    	progress_dialog.show();			
		}
		
		protected Result doInBackground(String... access_token) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.SHARE_FACEBOOK;
    		endpoint = endpoint.replace("{video_key}", m_video.videokey);
    		endpoint = endpoint.replace("{twitter_message}", "Check out the video I just captured with the Looker App " + Constants.WEB_SERVICES.DOTSPOT_HOST + m_video.videokey);

     		String url = host + endpoint;

    		Result result = null;
	    	String json_string = new WebServices().get(VideoDetailsActivity.this, url);
	    		    	
		    if(json_string != null){
		    	try{
		    		Gson gson = new GsonBuilder().create();
		    		result = gson.fromJson(json_string, Result.class);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return result;
		}

		
	    protected void onPostExecute(Result result) {
			progress_dialog.hide();

			if(result != null){
	        	if(result.stat.equalsIgnoreCase("fail")){
	        		
					final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Share Error", result.err.msg);
					
					dialog.show();

					dialog.setOKListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.cancel();
						}
					});
	        	}
	        	else if(result.stat.equalsIgnoreCase("ok")){
	            	Toast.makeText(VideoDetailsActivity.this, "Shared on Twitter", Toast.LENGTH_SHORT).show();
	        	}
	        }
			else{
				final AlertDialog dialog = new AlertDialog(VideoDetailsActivity.this, "Share Error", "Unexpected error.");
				
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
