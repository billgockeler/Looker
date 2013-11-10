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
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.kogeto.looker.camera.AVCEncoder.EncoderConfigException;
import com.kogeto.looker.util.Settings;
import com.kogeto.looker.widget.AlertDialog;

public class FrameProcessor {

	private static final String TAG = "FrameProcessor";
	
	private LinkedBlockingQueue<byte[]> frame_queue;
	private Dewarper dewarper;
	private AVCEncoder encoder;
	private boolean processing = false;	
	int enqueue_count = 0;

	
	
	public FrameProcessor(Context context) throws EncoderConfigException {
		this.frame_queue = new LinkedBlockingQueue<byte[]>();
		this.dewarper = new Dewarper(context);
        this.encoder = new AVCEncoder(Settings.Video.HD_1080.width, 
		   			  Settings.Video.HD_1080.height,
		   			  Settings.Video.HD_1080.bit_rate, 
		   			  Settings.Video.HD_1080.frame_rate, 
		   			  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
		   			  "kogeto.h264");
	}
	
	
	
	//Add a frame to the queue for processing
	public void addFrame(byte[] frame){
		this.frame_queue.offer(frame);
		enqueue_count++;
	}
	
	

	public void startProcessing(){
		
		this.processing = true;
    	this.encoder.open();
		
		Thread thread = new Thread(){
			int count = 0;
			byte[] raw_frame;
			byte[] processed_frame;
			
			public void run(){

				//continue processing the frames until told to stop and the queue is empty
				while(processing || !frame_queue.isEmpty()){
					
					raw_frame = (byte[]) frame_queue.poll();
					
					//if we got a frame from the queue then process it
					if(raw_frame != null){
						
						count++;

						//dewarp the frame
						processed_frame = dewarper.dewarpYV12(raw_frame, Settings.Camera.CURRENT.width, Settings.Camera.CURRENT.height);
						raw_frame = null;
				
					   	//convert the frame to a color format supported by the encoder
						processed_frame = YV12toYUV420SemiPlanar(processed_frame, Settings.Video.CURRENT.width, Settings.Video.CURRENT.height);

						//encode the frame
						encoder.encode(processed_frame, false);

						Log.d(TAG, "Processed " + count + " frames, " + frame_queue.size() + " frames remaining in queue");
					}
					else if(raw_frame == null && processed_frame != null){
						
					}
				}
				
				encoder.close();

				Log.d(TAG, "Finished processing all frames");
			}
		};
		
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	

	
    public void stopProcessing(){
    	this.processing = false;
    }
    
    

    //Convert by putting the corresponding U and V bytes together (interleaved).
    public byte[] YV12toYUV420SemiPlanar(final byte[] input, final int width, final int height) {

    	byte[] output = new byte[input.length];

        final int frameSize = width * height;
        final int qFrameSize = frameSize/4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i*2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i*2 + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }
    
    
    
	//write the x and y lookup tables to a file and print to screen (for debugging)
	public void debugTables(int[] dx, int[] dy){
		try {
			BufferedWriter x_writer = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/frame_processing.csv"));
			x_writer.write(Arrays.toString(dx));
			x_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
