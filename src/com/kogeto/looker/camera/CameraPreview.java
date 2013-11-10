package com.kogeto.looker.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kogeto.looker.R;
import com.kogeto.looker.camera.AVCEncoder.EncoderConfigException;
import com.kogeto.looker.util.Settings;
import com.kogeto.looker.widget.AlertDialog;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	
	private static final String TAG = "CameraView";
	
    private SurfaceHolder m_surface_holder;
    private Camera m_camera;
    private boolean recording;
    private FrameProcessor frame_processor;
    Context m_context;

    private MediaPlayer m_focusing_player;
    private MediaPlayer m_start_player;
    private MediaPlayer m_stop_player;
    
    
    public CameraPreview(Context context, AttributeSet attributes) {
        super(context, attributes);

        this.m_context = context;
        this.m_surface_holder = getHolder();
        this.m_surface_holder.addCallback(this);
        this.recording = false;

    	m_focusing_player = MediaPlayer.create(m_context, R.raw.focusing);
    	m_start_player = MediaPlayer.create(m_context, R.raw.record_start);
    	m_stop_player = MediaPlayer.create(m_context, R.raw.record_stop);
    }

    
    
    public void setCamera(Camera camera){
    	m_camera = camera;
    }
    
    
    
	public boolean isRecording(){
    	return this.recording;
    }
    
    

    public void focus(final FocusListener listener){
    	m_focusing_player.start();
    	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_context);

        int top = preferences.getInt("focus_top", 500);
        int left = preferences.getInt("focus_left", 500);
        int bottom = preferences.getInt("focus_bottom", 700);
        int right = preferences.getInt("focus_right", 700);

    	final Rect focus_rect = new Rect(
    			left * 2000/this.getWidth() - 1000,
    			top * 2000/this.getHeight() - 1000,
    			right * 2000/this.getWidth() - 1000,
    			bottom * 2000/this.getHeight() - 1000);
    	  
    	final List<Camera.Area> focus_list = new ArrayList<Camera.Area>();
    	Camera.Area focusArea = new Camera.Area(focus_rect, 1000);
    	focus_list.add(focusArea);
    	  
    	Parameters parameters = m_camera.getParameters();
    	parameters.setFocusAreas(focus_list);
    	parameters.setMeteringAreas(focus_list);
    	m_camera.setParameters(parameters);
    	
    	m_camera.autoFocus(new AutoFocusCallback(){
    		public void onAutoFocus(boolean arg0, Camera arg1) {
    		   if(listener != null){
    			   listener.focused();
    		   }
    		}
		});
    }
    
    
    
    public void surfaceCreated(SurfaceHolder holder) {
        if(this.m_camera != null){
	        try {
	            this.m_camera.setPreviewDisplay(holder);
	            this.m_camera.setPreviewCallbackWithBuffer(this);
	        } catch (IOException exception) {
	            m_camera.release();
	            m_camera = null;
	        }
        }
    }

    
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    	Camera.Parameters parameters = this.m_camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.YV12);
        parameters.setPreviewFpsRange(15000, 15000);
        parameters.setPreviewSize(Settings.Camera.CURRENT.width, Settings.Camera.CURRENT.height);  
        this.m_camera.setParameters(parameters);

        //set the orientation 90 if in portrait 
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){   
        	m_camera.setDisplayOrientation(90);
        }        
        
        int buffer_size = (Settings.Camera.CURRENT.width * Settings.Camera.CURRENT.height) * 3 / 2;
        
        //add frame buffers to the callback queue
        for(int i = 0; i < 5; i++){
        	byte[] frame = new byte[buffer_size];
        	m_camera.addCallbackBuffer(frame);
        }

        m_camera.startPreview();
    }



    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
        	if(m_camera != null){
        		m_camera.stopPreview();
	            m_camera.setPreviewCallback(null);
	            this.stopRecording();
        	}
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }


    
    public void startRecording() throws EncoderConfigException {
    	m_start_player.start();
		
    	this.frame_processor = new FrameProcessor(m_context);
    	this.frame_processor.startProcessing();
    	this.recording = true;
    }
    
    
    
    public void stopRecording(){
		m_stop_player.start();

    	this.recording = false;
    	if(this.frame_processor != null){
    		this.frame_processor.stopProcessing();
    	}
    }
    
    

	public void onPreviewFrame(byte[] data, Camera camera) {
		if(recording){
			
			//make a copy of the data(frame) that was delivered from the camera 
			//and add the frame back to the camera callback buffer
			byte[] frame = new byte[data.length]; 
			System.arraycopy(data, 0, frame, 0, data.length);
			
			this.frame_processor.addFrame(frame);
		}

		camera.addCallbackBuffer(data);
	}
	
	
	
	///////////// FOCUS LISTENER /////////////
	public interface FocusListener{
		public void focused();
	}
}   
