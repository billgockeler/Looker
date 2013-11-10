package com.kogeto.looker.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class VideoPlayerActivity extends Activity implements OnBufferingUpdateListener, MediaPlayerControl, 
	OnPreparedListener, OnSeekCompleteListener, ShowControlsListener {

	private static final String TAG = "VideoPlayerActivity";
	
	public static final String DATASOURCE = "datasource";

	private VideoSurfaceView video_view = null;
	private ProgressBar loading_progress_bar = null;
	private ProgressBar seek_progress_bar = null;
	private MediaPlayer media_player = null;
	private MediaController media_controller = null;

	boolean prepared = false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    Bundle bundle = getIntent().getExtras();
	    String datasource = bundle.getString(DATASOURCE);
	    
	    requestWindowFeature ( Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	    this.media_player = new MediaPlayer();
        this.media_player.setOnBufferingUpdateListener(this);
        this.media_player.setOnPreparedListener(this);
        this.media_player.setOnSeekCompleteListener(this);
	    
		try {
			this.media_player.setDataSource(datasource);					
		} 
		catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		RelativeLayout parent = new RelativeLayout(this);
		this.setContentView(parent);
		
        this.media_controller = new MediaController(this);
        this.media_controller.setMediaPlayer(this);

		this.video_view = new VideoSurfaceView(this, this.media_player, this);
		parent.addView(video_view);
		
        this.media_controller.setAnchorView(this.video_view);

        this.loading_progress_bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int ten_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        params.setMargins(ten_dp, 0, ten_dp, 0);
        parent.addView(this.loading_progress_bar, params);
        
        this.seek_progress_bar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        parent.addView(this.seek_progress_bar, param);
	}

	
	
	
	protected void onResume() {
		super.onResume();
		video_view.onResume();
	}
	
	
	
    protected void onPause() {
    	super.onPause();
    	this.media_player.stop();
    }
    
    
    
    //// BUFFERING UPDATE ////
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "buffering %" + percent);
		this.loading_progress_bar.setProgress(percent);
		
	}
	
	
    //// PREPARED LISTENER ////
	@Override
	public void onPrepared(MediaPlayer mp) {
		this.prepared = true;
		
		this.seek_progress_bar.setVisibility(View.INVISIBLE);
        this.showControls(3000);
	}

	
	
	//// MEDIA PLAYER CONTROLS ////
    @Override
    public void start() {
        media_player.start();
    }

    @Override
    public void pause() {
    	media_player.pause();
    }

    @Override
    public int getDuration() {
        return media_player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return media_player.getCurrentPosition();
    }

    @Override
    public void seekTo(int i) {
    	media_player.seekTo(i);
		this.seek_progress_bar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isPlaying() {
        return media_player.isPlaying(); 
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }


    
    //// SEEK COMPLETE LISTENER ////
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		this.seek_progress_bar.setVisibility(View.INVISIBLE);
	}



	//// SHOW CONTROLS LISTENER ////
	@Override
	public void showControls(int duration) {
		if(this.prepared){
			this.loading_progress_bar.setVisibility(View.VISIBLE);
			this.media_controller.show(0);
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){
				public void run(){
					loading_progress_bar.setVisibility(View.INVISIBLE);
					media_controller.hide();
				}
			}, duration);
		}
	}
}
