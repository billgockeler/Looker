package com.kogeto.looker.player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;


class CylindricalRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private final static String TAG = "CylindricalRenderer";

    private static final int FLOAT_SIZE = 4;
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private SurfaceTexture surface;
    private boolean update_surface = false;
    
	private float[] st_matrix = new float[16];

	private int st_matrix_handle;
    private int mvp_matrix_handle;
	private int program_handle;
    private int position_handle;
    private int texture_handle;
    private int texture_id;
    
    private int viewport_width;
    private int viewport_height;
    private float height;
    private int video_width;
    private int video_height;
    private int rotation = 1;
    private int indices_length;
    private short[] indices;
    FloatBuffer vertices;
    
    private MediaPlayer media_player;
    
	private final String vertex_shader =
	        "attribute vec4 position;\n" +
	        "attribute vec4 textureCoordinate;\n" +
	        "uniform mat4 uMVPMatrix;\n" +
	        "uniform mat4 uSTMatrix;\n" +
	        "varying vec2 vTextureCoord;\n" +
	        "void main() {\n" +
	        "  gl_Position = uMVPMatrix * position;\n" +
	        "  vTextureCoord = (uSTMatrix * textureCoordinate).xy;\n" +
	        "}\n";

	
	private final String fragment_shader =
	        "#extension GL_OES_EGL_image_external : require\n" +
	        "varying mediump vec2 vTextureCoord;\n" +
	        "uniform samplerExternalOES sTexture;\n" +
	        "void main() {\n" +
	        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
	        "}\n";

	

    public CylindricalRenderer(Context context, int video_height, int video_width) {
        this.video_height = video_height;
        this.video_width = video_width;

        Matrix.setIdentityM(st_matrix, 0);
    }

    
    
    public void setRotation(int rotation){
    	this.rotation = rotation;
    }
    
    
    
    public void setMediaPlayer(MediaPlayer player) {
        this.media_player = player;
    }
    
    

    private void buildMesh() {
    	
    	int number_of_sides = 16; //num sides
    	float radius = 1.0f; //radius
    	float spread = 360.0f; //full degrees to draw
    	float startAngle = -90.0f; //0 starts dead on in center of us, draws clockwise
      
    	//shouldn't need to touch these
    	height = (float)((2 * Math.PI * radius) * video_height / video_width);//scaling height right now to fill frame (instead of zooming in?) 158
    	int squareVertices_length = (number_of_sides + 1) * 10;
    	float[] squareVertices = new float[squareVertices_length];
    	this.indices_length = number_of_sides * 6;
    	this.indices = new short[this.indices_length];//new GLushort[n*6];// = {2,1,0};
      
    	for(int i = 0; i <= number_of_sides; i++) {
            double a = (startAngle + i * (spread/number_of_sides)) * Math.PI / 180; // degrees in radians
            
            int index= i * 10; //position we are at in vertices array
            
            //top set of vertices
            squareVertices[index]   = (float)(radius * Math.sin(a));
            squareVertices[index+1] = 0.0f + (height/2.0f);
            squareVertices[index+2] = (float)(radius * Math.cos(a));
            
            //texture coords. right now it projects on outside so we reverse the mapping
            squareVertices[index+3] = 1.0f - i * (spread/number_of_sides) / spread;
            squareVertices[index+4] = 0.0f;
            
            //bottom vertices
            squareVertices[index+5] = (float)(radius * Math.sin(a));
            squareVertices[index+6] = 0.0f - (height/2.0f);
            squareVertices[index+7] = (float)(radius * Math.cos(a));
            
            //texture coords. right now it projects on outside so we reverse the mapping
            squareVertices[index+8] = 1.0f - i * (spread/number_of_sides) / spread;
            squareVertices[index+9] = 1.0f;
            
            //skip last
            if( i < number_of_sides ) {
    			int startIndex = i * 2;
    			index = (i) * 6;
    			this.indices[index]   = (short)startIndex;
    			this.indices[index+1] = (short)(startIndex+1);
    			this.indices[index+2] = (short)(startIndex+2);
    			  
    			this.indices[index+3] = (short)(startIndex+1);
    			this.indices[index+4] = (short)(startIndex+2);
    			this.indices[index+5] = (short)(startIndex+3);
            }
    	}
    	
    	this.vertices = ByteBuffer.allocateDirect(squareVertices.length * (Float.SIZE/8)).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
    	this.vertices.put(squareVertices);
    	
    }    
    

    
    public void onDrawFrame(GL10 glUnused) {

        synchronized(this) {
            if (update_surface) {
                surface.updateTexImage();
	            surface.getTransformMatrix(st_matrix);
                update_surface = false;
            }
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		checkGlError("glClearColor");

		GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		checkGlError("glClear");

        GLES20.glUseProgram(this.program_handle);
        checkGlError("glUseProgram");

	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		checkGlError("glActiveTexture");
	    
	    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, this.texture_id);
		checkGlError("glBindTexture");

    	this.vertices.position(0);
    	GLES20.glVertexAttribPointer(this.position_handle, 3, GLES20.GL_FLOAT, false, 5 * FLOAT_SIZE, this.vertices);
		checkGlError("glVertexAttribPointer");
	    GLES20.glEnableVertexAttribArray(this.position_handle);
	    checkGlError("glEnableVertexAttribArray");
  	  
    	// Load the texture coordinate
    	this.vertices.position(3);
    	GLES20.glVertexAttribPointer(this.texture_handle, 2, GLES20.GL_FLOAT, false, 5 * FLOAT_SIZE, this.vertices);
		checkGlError("glVertexAttribPointer");
	    GLES20.glEnableVertexAttribArray(this.texture_handle);
	    checkGlError("glEnableVertexAttribArray texture_handle");
        
		//create view matrices
		float angleOfView = 45.0f;
		float modelZ = (float)((height/2.0f) / Math.tan(angleOfView/2 * Math.PI / 180.0) -1.0f);
		float nearPoint = modelZ;
		float farPoint = modelZ + 3.0f;
		float[] proj = new float[16];
		float[] modelview = new float[16];
		float[] modelviewProj = new float[16];
	    float[] rotate = new float[16];
	    float[] translate = new float[16];
  	    float trans[] = {0.0f,0.0f,-modelZ};

		Mat4f.loadPerspective((float)(angleOfView * Math.PI / 180.0f),((float)viewport_width/(float)viewport_height), nearPoint, farPoint, proj);
		Mat4f.loadYRotation((float)(rotation * Math.PI / 180), rotate);
		Mat4f.loadTranslation(trans, translate);  
	    Mat4f.multiplyMat4f(translate, rotate, modelview);  
	  
		// projection matrix * modelview matrix
		Mat4f.multiplyMat4f(proj, modelview, modelviewProj);
		
		//flip the image
		Matrix.scaleM(modelviewProj, 0, 1.0f, -1.0f, 1.0f);
		
	    // update uniform values
	    GLES20.glUniformMatrix4fv(mvp_matrix_handle, 1, false, modelviewProj, 0);
		checkGlError("glUniformMatrix4fv");
	    
	    GLES20.glUniformMatrix4fv(st_matrix_handle, 1, false, st_matrix, 0);
		checkGlError("glUniformMatrix4fv");

	    ShortBuffer indices_buffer = ByteBuffer.allocateDirect(indices.length * (Integer.SIZE/8)).order(ByteOrder.nativeOrder()).asShortBuffer();
    	indices_buffer.put(indices).position(0);
	    
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices_length, GLES20.GL_UNSIGNED_SHORT, indices_buffer);
		checkGlError("glDrawElements");
	    
		GLES20.glFinish();
    }

    
    
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    	this.viewport_width = width;
    	this.viewport_height = height;
    }

    
    
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

    	this.buildMesh();
    	
        //get program handle which will later be used to pass in values to the program.
        this.program_handle = createProgram(vertex_shader, fragment_shader);
        if (this.program_handle == 0) {
            return;
        }
        
        this.position_handle = GLES20.glGetAttribLocation(this.program_handle, "position");
		checkGlError("glGetAttribLocation position");
		if (this.position_handle == -1) {
			throw new RuntimeException("Could not get attrib location for position");
		}

        this.texture_handle = GLES20.glGetAttribLocation(this.program_handle, "textureCoordinate");
		checkGlError("glGetAttribLocation textureCoordinate");
		if (this.texture_handle == -1) {
			throw new RuntimeException("Could not get attrib location for textureCoordinate");
		}
     
        this.mvp_matrix_handle = GLES20.glGetUniformLocation(this.program_handle, "uMVPMatrix");
		checkGlError("glGetUniformLocation uMVPMatrix");
		if (this.mvp_matrix_handle == -1) {
			throw new RuntimeException("Could not get uniform location for uMVPMatrix");
		}

	    this.st_matrix_handle = GLES20.glGetUniformLocation(this.program_handle, "uSTMatrix");
	    checkGlError("glGetUniformLocation uSTMatrix");
	    if (st_matrix_handle == -1) {
	        throw new RuntimeException("Could not get attrib location for uSTMatrix");
	    }	

	    //generate one texture pointer and bind it as an external texture.
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
		checkGlError("glGenTextures");

        this.texture_id = textures[0];

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("glBindTexture");

        //set the texture parameters
	    GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        
        //sets the surface to be used as the sink for the video portion of the media. 
        this.surface = new SurfaceTexture(this.texture_id);
        this.surface.setOnFrameAvailableListener(this);

        //release the local reference to the server-side surface - we don't need the reference any more
        Surface s = new Surface(this.surface);
        this.media_player.setSurface(s);
        this.media_player.setScreenOnWhilePlaying(true);
        s.release();

        //start the media server
        try {
        	this.media_player.prepare();
        } catch (IOException t) {
            Log.e(TAG, "media player prepare failed");
        }

        synchronized(this) {
            this.update_surface = false;
        }

        this.media_player.start();
    }
    
    

    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        this.update_surface = true;
    }

    
    //creates a program object and returns a non-zero value by which it can be referenced. 
    //A program object is an object to which shader objects can be attached. 
    //This provides a mechanism to specify the shader objects that will be linked to create a program. 
    //It also provides a means for checking the compatibility of the shaders that will be used to 
    //create a program (for instance, checking the compatibility between a vertex shader and a fragment shader). 
    //When no longer needed as part of a program object, shader objects can be detached.
    private int createProgram(String vertex_shader, String fragment_shader) {
        
    	//load and compile the vertex shader
    	int vertex_shader_handle = loadShader(GLES20.GL_VERTEX_SHADER, vertex_shader);
        if (vertex_shader_handle == 0) {
            return 0;
        }
        
        //load and compile the fragment(pixel) shader
        int pixel_shader_handle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment_shader);
        if (pixel_shader_handle == 0) {
            return 0;
        }

        //create an empty program object and get a reference id to it
        int program = GLES20.glCreateProgram();
		checkGlError("glCreateProgram");
        
        //if we successfully created the program then continue setup
        if (program != 0) {
        	
        	//attach the vertex shader to the program
            GLES20.glAttachShader(program, vertex_shader_handle);
            checkGlError("glAttachShader");
            
        	//attach the fragment shader to the program
            GLES20.glAttachShader(program, pixel_shader_handle);
            checkGlError("glAttachShader");
            
            //attempt to link it
            GLES20.glLinkProgram(program);
    		checkGlError("glLinkProgram");
           
            //verify if the link was successful
            int[] link_status = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link_status, 0);
    		checkGlError("glGetProgramiv");
           
            //if not, then delete the program
            if (link_status[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            
            if(program == 0){
            	throw new RuntimeException("Error creating OpenGL program");
            }
            
            this.validateProgram(program);
        }
        
        
        return program;
    }

    
    
    private int loadShader(int shaderType, String source) {

    	int shader_handle = GLES20.glCreateShader(shaderType);
        
        if (shader_handle != 0) {
            GLES20.glShaderSource(shader_handle, source);
            GLES20.glCompileShader(shader_handle);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader_handle, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader_handle));
                GLES20.glDeleteShader(shader_handle);
                shader_handle = 0;
            }
        }
        
        return shader_handle;
    }

    
    
	private static boolean validateProgram(int program_handle) {
		
		GLES20.glValidateProgram(program_handle);
		
		final int[] validate_status = new int[1];
		
		GLES20.glGetProgramiv(program_handle, GLES20.GL_VALIDATE_STATUS, validate_status, 0);

		Log.d(TAG, "Program validated: " + (validate_status[0] == GLES20.GL_TRUE ? "true" : "false") + ", validation message: " + GLES20.glGetProgramInfoLog(program_handle));

		return validate_status[0] != 0;
	}

	
	private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}

