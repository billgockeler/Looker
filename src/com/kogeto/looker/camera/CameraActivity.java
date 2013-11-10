package com.kogeto.looker.camera;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kogeto.looker.R;
import com.kogeto.looker.camera.AVCEncoder.EncoderConfigException;
import com.kogeto.looker.camera.CameraPreview.FocusListener;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.myvideos.VideoDetailsActivity;
import com.kogeto.looker.player.VideoPlayerActivity;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.widget.AlertDialog;
import com.kogeto.looker.widget.MessageDialog;
import com.kogeto.tasks.CreateMovieTask;
import com.kogeto.tasks.CreateMovieTask.CreateMovieTaskListener;

public class CameraActivity extends Activity {

    private final static String TAG = "CameraActivity";

    private PowerManager.WakeLock m_wake_lock;    
    private CameraPreview m_camera_preview;
    private CalibrateView m_calibrate_view;
    private Camera m_camera;

	private AudioRecorder m_audio_recorder = new AudioRecorder();

    private ToggleButton m_calibrate_toggle;
    private ToggleButton m_record_toggle;
    private TextView m_share_button;
    private TextView m_timer_text;

	private Timer m_timer;
	private Video m_video;
	private int click_count;
    
	MediaPlayer m_unlock_player;
	MediaPlayer m_lock_player;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
	    requestWindowFeature ( Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
        PowerManager power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        m_wake_lock = power_manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG); 
        m_wake_lock.acquire(); 

	    m_audio_recorder = new AudioRecorder();	    
		m_lock_player = MediaPlayer.create(this, R.raw.lock);
		m_unlock_player = MediaPlayer.create(this, R.raw.unlock);

		setContentView(R.layout.activity_camera);
		
	    m_camera_preview = (CameraPreview)findViewById(R.id.camera_view);
        m_calibrate_view = (CalibrateView)findViewById(R.id.calibrate_view);
	    m_calibrate_toggle = (ToggleButton)findViewById(R.id.calibrate_toggle);
	    m_record_toggle = (ToggleButton)findViewById(R.id.record_toggle);
	    m_timer_text = (TextView)findViewById(R.id.timer);
	    
		m_calibrate_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				m_calibrate_view.setCalibrating(isChecked);
				if(isChecked){
					m_lock_player.start();
					
					m_timer_text.setVisibility(View.INVISIBLE);
					m_record_toggle.setVisibility(View.INVISIBLE);
				}
				else{
					m_unlock_player.start();
					
					m_calibrate_view.set();
					m_timer_text.setVisibility(View.VISIBLE);
					m_record_toggle.setVisibility(View.VISIBLE);
				}
			}
			
		});
		
		m_record_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					startRecording();
				}
				else{
					stopRecording();
				}
			}
		});
	
    	m_share_button = (TextView)findViewById(R.id.right_button);
    	m_share_button.setVisibility(View.GONE);
    	m_share_button.setText("Share");
    	m_share_button.setOnClickListener(new OnClickListener(){
    		public void onClick(View v) {
    			share();
    		}
    	});
    	
    	TextView cancel_button = (TextView)findViewById(R.id.left_button);
    	cancel_button.setVisibility(View.VISIBLE);
    	cancel_button.setText("Cancel");
    	cancel_button.setOnClickListener(new OnClickListener(){
    		public void onClick(View v) {
    			finish();
    		}
    	});
    	
    	ImageView logo_image = (ImageView)findViewById(R.id.logo_image);
        logo_image.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if(click_count == 5){
                	click_count = 0;
                	showCameraSettings();
                }
                return true;
            }
        });
        
        logo_image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	click_count++;
                return ;
            }
        });
	}


	
	@Override
    public void onResume() {
		super.onResume();
        
        m_camera = Camera.open();
        m_camera_preview.setCamera(m_camera);

        if (m_wake_lock == null) {
           PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
           m_wake_lock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
           m_wake_lock.acquire();
        }
    }



    public void onPause() {
		super.onPause();

    	if(m_camera_preview.isRecording()){
    		m_camera_preview.stopRecording();
    		m_audio_recorder.stopRecording();
    	}

        if (m_camera != null) {
        	m_camera.release();
        	m_camera = null;
        	m_camera_preview.setCamera(null);
        }
        
        if (m_wake_lock != null) {
            m_wake_lock.release();
            m_wake_lock = null;
        }
    }


    
    public void play(View v) {
    	Bundle bundle = new Bundle();
    	bundle.putString(VideoPlayerActivity.DATASOURCE, Environment.getExternalStorageDirectory() + "/kogeto.mp4");
		Intent video_player_intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
		video_player_intent.putExtras(bundle);
        startActivity(video_player_intent);
    }	
    
    
    
    public void share(){
    	if(m_video != null){
			Bundle bundle = new Bundle();
			bundle.putParcelable(VideoDetailsActivity.VIDEO_OBJECT, m_video);
			Intent video_details_intent = new Intent(this, VideoDetailsActivity.class);
			video_details_intent.putExtras(bundle);
		    startActivity(video_details_intent);
    	}
    }
    
    
    
    //stop recording
    public void stopRecording() {
		m_audio_recorder.stopRecording();
		m_camera_preview.stopRecording();
//		m_record_toggle.clearAnimation();
		if(m_timer != null){
			m_timer.cancel();
		}
		
		new CreateMovieTask(this, new CreateMovieTaskListener(){
			public void finished(Video video){
				m_video = video;
				
				if(video != null){
					m_share_button.setVisibility(View.VISIBLE);
				}
				else{
					m_share_button.setVisibility(View.GONE);
				}
			}
		}).execute();
    }

   
    
    //start recording
    public void startRecording() {
    	boolean calibrated = checkCalibration();
    	
    	if(!calibrated){
    		m_record_toggle.setChecked(false);
    		return;
    	}
    	
		m_timer_text.setText("00:00");
		m_camera_preview.focus(new FocusListener(){
			public void focused() {
				try{
					m_camera_preview.startRecording();
					m_audio_recorder.startRecording();
					//startAnimation();
					m_timer = new Timer();
					m_timer.schedule(new TimerTask(){
						long start = SystemClock.uptimeMillis();
						public void run() {
							runOnUiThread(new Runnable(){
								public void run(){
									long elapsed = SystemClock.uptimeMillis() - start;
	
									int secs = (int) (elapsed / 1000);
									int mins = secs / 60;
									secs = secs % 60;
									m_timer_text.setText("" + String.format("%02d", mins) + ":"
											+ String.format("%02d", secs));
								}
							});
						}
						
					}, 0, 1000);
				}
		    	catch(EncoderConfigException e){
		    		m_record_toggle.setChecked(false);
		    		stopRecording();
		    		
					final AlertDialog dialog = new AlertDialog(CameraActivity.this, "Recording Error", e.getMessage());
					
					dialog.show();
			
					dialog.setOKListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.cancel();
						}
					});
		    	}
			}
		});
    }
    
    
    
    //start the record button animation
    public void startAnimation(){
	    final Animation animation = new AlphaAnimation(1.0f, 0.5f); // Change alpha from fully visible to invisible
	    animation.setDuration(1000); // duration - half a second
	    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
	    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
	    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
	    m_record_toggle.startAnimation(animation);
    }

    
    
    //see if the camera has been calibrated, if not then prompt the user to do so
    public boolean checkCalibration(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//verify that the camera was calibrated 
		boolean calibrated = prefs.getBoolean(Constants.PREFERENCES.CALIBRATED, false); 
    	
    	if(!calibrated){
			final MessageDialog dialog = new MessageDialog(this, "Not Calibrated", "The camera needs to be calibrated with the lens before recording. Would you like to do that now?");

			dialog.show();
			
			dialog.setPositiveListener(new View.OnClickListener() {
				public void onClick(View v) {
					m_calibrate_view.setCalibrating(true);
		    		m_calibrate_toggle.setChecked(true);
					m_timer_text.setVisibility(View.INVISIBLE);
					m_record_toggle.setVisibility(View.INVISIBLE);
					dialog.cancel();
				}
			});
			
			dialog.setNegativeListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialog.cancel();
				}
			});
			
			return false;
    	}
    	else{
    		return true;
    	}
    }
    
    
    
    public void showCameraSettings(){
    	RelativeLayout settings_layout = (RelativeLayout)findViewById(R.id.settings_layout);
    	settings_layout.setVisibility(View.VISIBLE);
       StringBuilder builder = new StringBuilder();
    	
        Camera.Parameters parameters = m_camera.getParameters();
        
        //PREVIEW FORMATS
        builder.append("PREVIEW FORMATS:\n");
        List<Integer> preview_formats = parameters.getSupportedPreviewFormats();
        for(Integer preview_format : preview_formats){
        	switch((int)preview_format){
        		case(ImageFormat.JPEG):
        			builder.append("JPEG\n");
        		case(ImageFormat.NV16):
        			builder.append("NV16\n");
        		case(ImageFormat.NV21):
        			builder.append("NV21\n");
        		case(ImageFormat.RGB_565):
        			builder.append("RGB\n");
        		case(ImageFormat.UNKNOWN):
        			builder.append("UNKNOWN\n");
        		case(ImageFormat.YUY2):
        			builder.append("YUY2\n");
        		case(ImageFormat.YV12):
        			builder.append("YV12\n");
        	}
        }
        
        //PREVIEW FPS RANGE
        builder.append("\nPREVIEW FPS RANGE\n");
        List<int[]> fps_ranges = parameters.getSupportedPreviewFpsRange();
        for(int[] ranges : fps_ranges){
        	builder.append(Arrays.toString(ranges) + "\n");
        }
        
        //PREVIEW SIZES
        builder.append("\nPREVIEW SIZES\n");
        List<Camera.Size> camera_sizes = parameters.getSupportedPreviewSizes();
        for(Camera.Size camera_size : camera_sizes){
        	builder.append("width " + camera_size.width + ", height " + camera_size.height + "\n");
        }
        
        //FOCUS MODES
        builder.append("\nFOCUS MODES\n");
        List<String> focus_modes = parameters.getSupportedFocusModes();
        for(String focus_mode : focus_modes){
        	builder.append(focus_mode + "\n");
        }
        
        //CODECS
        builder.append("\nCODECS");
		int count = MediaCodecList.getCodecCount();
		
		for(int i = 0; i < count; i++){
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String name = info.getName();
			String[] types = info.getSupportedTypes();
			for(int ii = 0; ii < types.length; ii++){
				builder.append("\nname: " + name + "\ntype: " + types[ii].toString() + "\nis encoder: " + info.isEncoder() + "\n");
			}
		}
        
        
        final TextView settings_text = (TextView)findViewById(R.id.camera_settings_text);
        settings_text.setText(builder.toString());
        settings_text.setMovementMethod(new ScrollingMovementMethod());
        
        TextView close_button = (TextView)findViewById(R.id.close_button);
        close_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
            	RelativeLayout settings_layout = (RelativeLayout)findViewById(R.id.settings_layout);
            	settings_layout.setVisibility(View.GONE);
            	settings_text.setText("");
			}
        });
        
        
    }

}