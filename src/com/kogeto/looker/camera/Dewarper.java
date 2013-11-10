/*
* Copyright (c) 2013, Kogeto and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
*/


package com.kogeto.looker.camera;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.Settings;



public class Dewarper {

	public static final String TAG = "Dewarper";
	
	int circle_center_x; 		//The x component of the center point of the circle
	int circle_center_y; 		//The y component of the center point of the circle
	int circle_outer_diameter; 	//The diameter of the image circle, not including the outer edge of the lens
	int circle_inner_diameter; 	//The diameter of the center of the image circle, including the outer edge
	int target_width; 			//The width of the image frame
	int target_height; 			//The height of the image frame
	int target_frame_size; 		//The size of the target frame 
	int[] dx_lookup; 			//The lookup table for x dewarping values
	int[] dy_lookup; 			//The lookup table for y dewarping values
	byte[] output;				//The reusable array for the output frame 
	int Oy, Ou, Ov, Iy, Iu, Iv; //The Y, U, V values for input and output frames
	
	

	public Dewarper(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//Read the calibration values from the preferences 
//		this.circle_center_x = prefs.getInt(Constants.PREFERENCES.CALIBRATION_X, 960)+470; 
//		this.circle_center_y = prefs.getInt(Constants.PREFERENCES.CALIBRATION_Y, 540)-407;
//		this.circle_inner_diameter = prefs.getInt(Constants.PREFERENCES.CALIBRATION_INNER_DIAMETER, 340)+200;
//		this.circle_outer_diameter = prefs.getInt(Constants.PREFERENCES.CALIBRATION_OUTER_DIAMETER, 666)+200;
		
		//Read the calibration values from the preferences 
		int calibration_x = prefs.getInt(Constants.PREFERENCES.CALIBRATION_X, 0); 
		this.circle_center_y = Math.abs(Settings.Camera.CURRENT.height - calibration_x); 

		this.circle_center_x = prefs.getInt(Constants.PREFERENCES.CALIBRATION_Y, 0);
		this.circle_inner_diameter = prefs.getInt(Constants.PREFERENCES.CALIBRATION_INNER_DIAMETER, 0);
		this.circle_outer_diameter = prefs.getInt(Constants.PREFERENCES.CALIBRATION_OUTER_DIAMETER, 0);
		
//		this.circle_center_y = 498; 
//		this.circle_center_x = 992;
//		this.circle_inner_diameter = 280;
//		this.circle_outer_diameter = 680;

		Log.d(TAG, "Calibration preferences read as x:" + calibration_x + ", y:" + circle_center_y + ", inner diameter:" + circle_inner_diameter +", outer diameter:" + circle_outer_diameter);
    	Log.d(TAG, "Calibration translated to  x:" + circle_center_x + ", y:" + circle_center_y + ", inner diameter:" + circle_inner_diameter +", outer diameter:" + circle_outer_diameter);

    	this.target_height = Settings.Video.HD_1080.height;
		this.target_width = Settings.Video.HD_1080.width;
		this.target_frame_size = target_width * target_height;
    	this.output = new byte[(int)(target_frame_size * 1.5)];
		
		this.dx_lookup = new int[this.target_width * this.target_height];
		this.dy_lookup = new int[this.target_width * this.target_height];

		this.createDewarpTables();	
	}
	
	
	
	//Create tables that hold lookup values to dewarp the image
	//the size of the dewarp table is the same size as the target video
	public void createDewarpTables(){
		for(int y = 0; y < target_height; y++){
			for(int x = 0; x < target_width; x++){     

				float angle = -((float)x / (float)target_width) * (2.0f * 3.141592f);
			    float distance = (circle_inner_diameter / 2.0f + y * (((circle_outer_diameter - circle_inner_diameter) / 2.0f) / target_height));
			    int dx = (int) (circle_center_x + Math.cos(angle) * distance);
			    int dy = (int) (circle_center_y + Math.sin(angle) * distance);
			    dx_lookup[x + y * target_width] = dx;
			    dy_lookup[x + y * target_width] = dy;
			}
		}
	}
	
	
		
	
	//dewarp the frame
	public byte[] dewarpYV12(byte[] input, int input_width, int input_height){

		final int input_frame_size = input_width * input_height;
        
    	for(int y = 0; y < target_height; y++){
	        for(int x = 0; x < target_width; x++){
	        	
	        	// Find the lookup location in source buffer from lookup table
	        	int dx = dx_lookup[y * target_width + x];
	        	int dy = dy_lookup[y * target_width + x];
	        	
	        	//get positions for each of the y, u, v components in the output frame
				Oy =  y * target_width + x;
				Ov = (y/2) * (target_width/2) + (x/2) + target_frame_size;
				Ou = (y/2) * (target_width/2) + (x/2) + target_frame_size + target_frame_size/4;

				//get positions for each of the y, u, v components in the input frame
				//using the dewarp lookup tables
	        	if( dx < input_width && dy < input_height ) {
					Iy =  dy * input_width + dx;
					Iv = (dy/2) * (input_width/2) + (dx/2) + input_frame_size;
					Iu = (dy/2) * (input_width/2) + (dx/2) + input_frame_size + input_frame_size/4;
	        	} 
	        	else {
	        		Iy = 0x00; //Y
		        	Iv = 0x00; // Cb (V) 
		        	Iu = 0x00; // Cr (U) 
	        	}
	        	
	        	output[Oy] = input[Iy]; // Y 
	        	output[Ov] = input[Iv]; // Cb (V) 
	        	output[Ou] = input[Iu]; // Cr (U)
	        }
	    }
    	
    	return output;
	}
	
	
	
	//write the x and y lookup tables to a file and print to screen (for debugging)
	public void debugTables(int[] dx, int[] dy){
		try {
			BufferedWriter x_writer = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/dx_lookup.csv"));
			x_writer.write(Arrays.toString(dx));
			x_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
		try {
			BufferedWriter y_writer = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/dy_lookup.csv"));
			y_writer.write(Arrays.toString(dy));
			y_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		Log.d(TAG, "dx_lookup=" + Arrays.toString(dx));
		Log.d(TAG, "dy_lookup=" + Arrays.toString(dy));
	}
}

