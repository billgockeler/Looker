package com.kogeto.looker.camera;

import com.kogeto.looker.util.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
 
public class CalibrateView extends View implements OnTouchListener {
	
	private static final String TAG = "CalibrateView";
    private static final int INVALID_POINTER_ID = -1;
 
	private static final int BLACK = Color.argb(150, 0, 0, 0);
	private static final int WHITE = Color.argb(255, 255, 255, 255);
    
	private Paint m_background_paint;
    private Paint m_donut_paint;
	private Paint m_on_paint;
	private Paint m_off_paint;
	private Point m_screen_size;
	private Context m_context;
	
    private float m_last_touch_x;
    private float m_last_touch_y;
    private float m_position_x;
    private float m_position_y;
    private float m_draw_radius;
    private int m_active_pointer;
    
    private ScaleGestureDetector m_scale_detector;
    private float m_scale_factor = 0.8f;
    private float m_circle_ratio = 0.4f;
	private float m_stroke = 500;
	
	private boolean m_focused = false;
	private boolean m_calibrating = false;
	
	
	public CalibrateView(Context context) {
		super(context);
		init(context);
	}
		
	
	
	public CalibrateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	 
	
	
	public CalibrateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	 
	

	public void init(Context context){
		this.m_context = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int outer_diameter  = (prefs.getInt(Constants.PREFERENCES.CALIBRATION_OUTER_DIAMETER, 666)/2);
		int inner_diameter  = (prefs.getInt(Constants.PREFERENCES.CALIBRATION_INNER_DIAMETER, 340)/2);
		int delta = outer_diameter - inner_diameter;

		//Read the calibration values from the preferences 
		m_position_x 	= prefs.getInt(Constants.PREFERENCES.CALIBRATION_X, 0); 
		m_position_y 	= prefs.getInt(Constants.PREFERENCES.CALIBRATION_Y, 0);
		m_draw_radius   = inner_diameter + (delta/2);
		m_circle_ratio  = delta / m_stroke;                                                                                                                                                                                                                                                 ;
		
		//if we don't have x and y coordinates then position the donut in the center of the screen
		if(m_position_x == 0 || m_position_y == 0){
			WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			m_screen_size = new Point();
			display.getSize(m_screen_size);
			m_position_x =  m_screen_size.x/2;
			m_position_y =  m_screen_size.y/2;
		}
		
		m_background_paint = new Paint();
		m_background_paint.setStyle(Paint.Style.STROKE);
		m_background_paint.setStrokeWidth(m_stroke);
		m_background_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); 
		
		m_on_paint = new Paint();
		m_on_paint.setColor(BLACK);
		m_on_paint.setStyle(Paint.Style.FILL);
		
		m_off_paint = new Paint();
		m_off_paint.setColor(WHITE);
		m_off_paint.setStyle(Paint.Style.FILL);

		this.setOnTouchListener(this);
	    m_scale_detector = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	

	public void setCalibrating(boolean calibrating){
		this.m_calibrating = calibrating;
        invalidate();
	}

	
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(m_calibrating){
			m_donut_paint = m_on_paint;
		}
		else{
			m_donut_paint = m_off_paint;
		}
		
		m_background_paint.setStrokeWidth(m_stroke * m_circle_ratio);
		
		canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), m_donut_paint);
		canvas.drawCircle(m_position_x, m_position_y, m_draw_radius, m_background_paint);
    	Log.d(TAG, "x:" + m_position_x + ", y:" + m_position_y + ", radius:" + m_draw_radius +", stroke width:" + (m_stroke * m_circle_ratio));
    	
	}
	
	public void set(){
		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.m_context).edit();
		
		int stroke = (int)(m_stroke * m_circle_ratio);
		int outer_diameter = ((int)this.m_draw_radius * 2) + (stroke);
		int inner_diameter = outer_diameter - (stroke * 2);
		
		//set the preferences for the donut calibration
        editor.putInt(Constants.PREFERENCES.CALIBRATION_X, ((int)this.m_position_x)).commit();
        editor.putInt(Constants.PREFERENCES.CALIBRATION_Y, (int)this.m_position_y).commit();
        editor.putInt(Constants.PREFERENCES.CALIBRATION_OUTER_DIAMETER, outer_diameter).commit();
        editor.putInt(Constants.PREFERENCES.CALIBRATION_INNER_DIAMETER, inner_diameter).commit();

        //determine the focus are based on the donut position and set the preferences for the focus
        int middle = (outer_diameter - inner_diameter) / 2;
		int focus_center_y = (int)this.m_position_y + middle;
		int focus_center_x = (int)this.m_position_x + middle;
		
        editor.putInt(Constants.PREFERENCES.FOCUS_TOP, (int)focus_center_y - 50).commit();
        editor.putInt(Constants.PREFERENCES.FOCUS_LEFT, (int)focus_center_x - 50).commit();
        editor.putInt(Constants.PREFERENCES.FOCUS_RIGHT, (int)focus_center_x + 50).commit();
        editor.putInt(Constants.PREFERENCES.FOCUS_BOTTOM, (int)focus_center_y + 50).commit();
        
    	//the camera has now been calibrated
		editor.putBoolean(Constants.PREFERENCES.CALIBRATED, true).commit();
		m_focused = true;
		
		Dewarper d = new Dewarper(m_context);
	}
	
	
	
	public boolean onTouch(View v, MotionEvent ev) {
    	if(m_calibrating){
	        m_scale_detector.onTouchEvent(ev);
	    	
		    final int action = ev.getAction();
		    switch (action) {
			    case MotionEvent.ACTION_DOWN: {
			        final float x = ev.getX();
			        final float y = ev.getY();
			        
			        
			        // Remember where we started
			        m_last_touch_x = x;
			        m_last_touch_y = y;
			        m_active_pointer = ev.getPointerId(0);
			        break;
			    }
		        
			    case MotionEvent.ACTION_MOVE: {
			    	final int pointer = ev.findPointerIndex(m_active_pointer);
			        final float x = ev.getX(pointer);
			        final float y = ev.getY(pointer);
			        
			        if (!m_scale_detector.isInProgress()) {
				        // Calculate the distance moved
				        final float dx = x - m_last_touch_x;
				        final float dy = y - m_last_touch_y;
				        
				        // Move the object
				        m_position_x += dx;
				        m_position_y += dy;
				        
				        // Invalidate to request a redraw
				        invalidate();
			        }
			        
			        // Remember this touch position for the next move event
			        m_last_touch_x = x;
			        m_last_touch_y = y;
			        
			        
			        break;
			    }
			    
			    case MotionEvent.ACTION_UP: {
			        m_active_pointer = INVALID_POINTER_ID;
			        break;
			    }
			        
			    case MotionEvent.ACTION_CANCEL: {
			    	m_active_pointer = INVALID_POINTER_ID;
			        break;
			    }
			    
			    case MotionEvent.ACTION_POINTER_UP: {
			    	
			        // Extract the index of the pointer that left the touch sensor
			        final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			        
			        final int pointer_id = ev.getPointerId(pointerIndex);
			        if (pointer_id == m_active_pointer) {
			        	
			            // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
			            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			            m_last_touch_x = ev.getX(newPointerIndex);
			            m_last_touch_y = ev.getY(newPointerIndex);
			            m_active_pointer = ev.getPointerId(newPointerIndex);
			        }
			        break;
			    }
			    
		    }
    	}
		    
		return true;
	}
    
    
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {
        	
        	
         	m_scale_factor *= detector.getScaleFactor();
         	m_draw_radius = m_scale_factor * 300;
         	m_circle_ratio = (float)(m_scale_factor * 0.4);
         	
            // Don't let the circle get too small or too large.
            m_scale_factor = Math.max(0.6f, Math.min(m_scale_factor, 1.5f));

            invalidate();
            return true;
        }
    }

	 
}