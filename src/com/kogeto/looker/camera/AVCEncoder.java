/*
* Copyright (c) 2013, Kogeto and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
*/


package com.kogeto.looker.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Formatter;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;


public class AVCEncoder {
	private static final String TAG = "AVCEncoder";

	private MediaCodec media_codec;
	private BufferedOutputStream file_output_stream;
	
	int frame_count = 0;

	File output_file;
	String filename;


	public AVCEncoder(int width, int height, int bit_rate, int frame_rate, int color_format, String filename) throws EncoderConfigException { 
		this.filename = filename;
		
		try{
	        media_codec = MediaCodec.createEncoderByType("video/avc");
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
		    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bit_rate);
		    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frame_rate);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);
		    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		    mediaFormat.setInteger("stride", width);
		    mediaFormat.setInteger("slice-height", height);
		    media_codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		    media_codec.start();
		}
		catch(Exception e){
			throw new EncoderConfigException("This device does not support the required video encoder.");
		}
	}
	

	
	
	public void open(){
	    this.output_file = new File(Environment.getExternalStorageDirectory(), this.filename);

	    try {
	        this.file_output_stream = new BufferedOutputStream(new FileOutputStream(output_file));
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	
	

	public void close() {
		synchronized(media_codec){
		    try {
		        media_codec.stop();
		        media_codec.release();
				if (file_output_stream != null){
					file_output_stream.flush();
					file_output_stream.close();
				}
		    } 
		    catch (Exception e){ 
		        e.printStackTrace();
		    }
		}
	}
	
	
	
	//Encode a single video frame and write it to file
	public void encode(byte[] input, boolean end) {
		synchronized(media_codec){
		    try {
		    	
		    	//create input and output buffers
		        ByteBuffer[] inputBuffers = media_codec.getInputBuffers();
		        ByteBuffer[] outputBuffers = media_codec.getOutputBuffers();
		        
		        //get an available input buffer
		        int inputBufferIndex = media_codec.dequeueInputBuffer(-1);
		        
		        //place the frmae on the input buffer
		        if (inputBufferIndex >= 0) {
		            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
		            inputBuffer.clear();
		            inputBuffer.put(input);
		            
		            frame_count++;
		            Log.i(TAG, "Encoding frame # " + frame_count + ", size = " + input.length);
		            
		            if(end){
		            	media_codec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM );
		            }
		            else{
		            	media_codec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
		            }
		        }
		
		        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		        
		        //get the index of available output buffers (there may be more than one)
		        int outputBufferIndex = media_codec.dequeueOutputBuffer(bufferInfo, 0);
		        
		        //iterate through those buffers, read the frames and write them to file
		        while (outputBufferIndex >= 0) {
		        	
		        	//get the frame from the output buffer
		            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
		            byte[] outData = new byte[bufferInfo.size];
		            outputBuffer.get(outData);
		            		            
		            //write the encoded frame to the file output stream
		            this.file_output_stream.write(outData, 0, outData.length);
		            media_codec.releaseOutputBuffer(outputBufferIndex, false);
		            outputBufferIndex = media_codec.dequeueOutputBuffer(bufferInfo, 0);
		        }		        
		    } 
		    catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	
	

	private void list(){
		int count = MediaCodecList.getCodecCount();
		
		Log.d(TAG, "number of codecs is: " + count);
		
		for(int i = 0; i < count; i++){
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String name = info.getName();
			String[] types = info.getSupportedTypes();
			for(int ii = 0; ii < types.length; ii++){
				Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii].toString() + ", is encoder: " + info.isEncoder());
				MediaCodecInfo.CodecCapabilities c = info.getCapabilitiesForType(types[ii]);
				int[] formats = c.colorFormats;
				for(int a = 0; a < formats.length; a++){
					Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii].toString() + ",  supported color format: " + formats[a]);
				}
				
				CodecProfileLevel[] level = c.profileLevels;
				for(int b = 0; b < level.length; b++){
					int l = level[b].level;
					int p = level[b].profile;
					Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii].toString() + ",  level: " + level[b].level + ", profile: " + level[b].profile);
				}
			}
		}
		
	}

	

	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	
	public static String bytesToHexString(byte[] bytes) {
	    StringBuilder sb = new StringBuilder(bytes.length * 2);

	    Formatter formatter = new Formatter(sb);
	    for (byte b : bytes) {
	        formatter.format("%02x", b);
	    }

	    return sb.toString();
	}	
	
	
	
	public class EncoderConfigException extends Exception {
		public EncoderConfigException(String message){
			super(message);
		}
	}
}