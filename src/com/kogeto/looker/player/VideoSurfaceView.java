package com.kogeto.looker.player;

import com.kogeto.looker.util.Settings;
import com.kogeto.looker.util.Settings.Video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

@SuppressLint("ViewConstructor")
class VideoSurfaceView extends GLSurfaceView implements OnTouchListener {

    private static final String TAG = "VideoSurfaceView";
	private static final int INVALID_POINTER_ID = -1;

    CylindricalRenderer renderer;
    private MediaPlayer media_player = null;
	private ShowControlsListener controls_listener;

    private float down_x;
    private float rotation = 0;
    private float position = 0;

    
    
    public VideoSurfaceView(Context context, MediaPlayer media_player, ShowControlsListener controls_listener) {
        super(context);

        setEGLContextClientVersion(2);
        setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        
        this.media_player = media_player;
        this.controls_listener = controls_listener;
        
        this.renderer = new CylindricalRenderer(context, Settings.Video.HD_1080.height, Settings.Video.HD_1080.width);
        this.setRenderer(this.renderer);
		this.setOnTouchListener(this);
    }
    

    
    @Override
    public void onResume() {
        queueEvent(new Runnable(){
        	public void run() {
        		renderer.setMediaPlayer(media_player);
        	}
        });

        super.onResume();
    }

    
    
    @Override
	public boolean onTouch(View v, MotionEvent ev) {
		
		
	    final int action = ev.getAction();
	    switch (action) {
		    case MotionEvent.ACTION_DOWN: {
		        final float x = ev.getX();
		        
		        // Remember where we started
		        down_x = x;
		        Log.d(TAG, "down=" + down_x);
		        break;
		    }
	        
		    case MotionEvent.ACTION_MOVE: {
		        final float x = ev.getX(0);
		        
		        // Calculate the distance moved
		        final float dx = (x - down_x)/5;
		        
		        rotation = position - dx;
		        renderer.setRotation((int)rotation);
		        
		        break;
		    }
		    
		    case MotionEvent.ACTION_UP: {
		        final float up_x = ev.getX();
		        Log.d(TAG, "up=" + up_x);
		        if((up_x - down_x) == 0){
		    		this.controls_listener.showControls(3000);
		        }
		        
		    	position = rotation;
		        break;
		    }
	    }
	    
	    return true;
	}

}
