package com.kogeto.looker.player;

public class Mat4f {

	
	public static void loadYRotation(float radians, float[] mout) {
	    float cosrad = (float)Math.cos(radians);
	    float sinrad = (float)Math.sin(radians);
	   
	    mout[0] = cosrad;
	    mout[1] = 0.0f;
	    mout[2] = -sinrad;
	    mout[3] = 0.0f;
	   
	    mout[4] = 0.0f;
	    mout[5] = 1.0f;
	    mout[6] = 0.0f;
	    mout[7] = 0.0f;
	   
	    mout[8] = sinrad;
	    mout[9] = 0.0f;
	    mout[10] = cosrad;
	    mout[11] = 0.0f;    
	   
	    mout[12] = 0.0f;
	    mout[13] = 0.0f;
	    mout[14] = 0.0f;
	    mout[15] = 1.0f;
	}
	
	
	
	public static void loadTranslation(float[] v, float[] mout) {
	    mout[0] = 1.0f;
	    mout[1] = 0.0f;
	    mout[2] = 0.0f;
	    mout[3] = 0.0f;
	   
	    mout[4] = 0.0f;
	    mout[5] = 1.0f;
	    mout[6] = 0.0f;
	    mout[7] = 0.0f;
	   
	    mout[8] = 0.0f;
	    mout[9] = 0.0f;
	    mout[10] = 1.0f;
	    mout[11] = 0.0f;    
	   
	    mout[12] = v[0];
	    mout[13] = v[1];
	    mout[14] = v[2];
	    mout[15] = 1.0f;
	}
	
	
	
	public static void loadPerspective(float fov_radians, float aspect, float zNear, float zFar, float[] mout){
	    float f = (float)(1.0f / Math.tan(fov_radians/2.0f));
	   
	    mout[0] = f / aspect;
	    mout[1] = 0.0f;
	    mout[2] = 0.0f;
	    mout[3] = 0.0f;
	   
	    mout[4] = 0.0f;
	    mout[5] = f;
	    mout[6] = 0.0f;
	    mout[7] = 0.0f;
	   
	    mout[8] = 0.0f;
	    mout[9] = 0.0f;
	    mout[10] = (zFar+zNear) / (zNear-zFar);
	    mout[11] = -1.0f;
	   
	    mout[12] = 0.0f;
	    mout[13] = 0.0f;
	    mout[14] = 2 * zFar * zNear /  (zNear-zFar);
	    mout[15] = 0.0f;
	}
	
	
	
	public static void multiplyMat4f(float[] a, float[] b, float[] mout) {
	    mout[0]  = a[0] * b[0]  + a[4] * b[1]  + a[8] * b[2]   + a[12] * b[3];
	    mout[1]  = a[1] * b[0]  + a[5] * b[1]  + a[9] * b[2]   + a[13] * b[3];
	    mout[2]  = a[2] * b[0]  + a[6] * b[1]  + a[10] * b[2]  + a[14] * b[3];
	    mout[3]  = a[3] * b[0]  + a[7] * b[1]  + a[11] * b[2]  + a[15] * b[3];
	 
	    mout[4]  = a[0] * b[4]  + a[4] * b[5]  + a[8] * b[6]   + a[12] * b[7];
	    mout[5]  = a[1] * b[4]  + a[5] * b[5]  + a[9] * b[6]   + a[13] * b[7];
	    mout[6]  = a[2] * b[4]  + a[6] * b[5]  + a[10] * b[6]  + a[14] * b[7];
	    mout[7]  = a[3] * b[4]  + a[7] * b[5]  + a[11] * b[6]  + a[15] * b[7];
	 
	    mout[8]  = a[0] * b[8]  + a[4] * b[9]  + a[8] * b[10]  + a[12] * b[11];
	    mout[9]  = a[1] * b[8]  + a[5] * b[9]  + a[9] * b[10]  + a[13] * b[11];
	    mout[10] = a[2] * b[8]  + a[6] * b[9]  + a[10] * b[10] + a[14] * b[11];
	    mout[11] = a[3] * b[8]  + a[7] * b[9]  + a[11] * b[10] + a[15] * b[11];
	 
	    mout[12] = a[0] * b[12] + a[4] * b[13] + a[8] * b[14]  + a[12] * b[15];
	    mout[13] = a[1] * b[12] + a[5] * b[13] + a[9] * b[14]  + a[13] * b[15];
	    mout[14] = a[2] * b[12] + a[6] * b[13] + a[10] * b[14] + a[14] * b[15];
	    mout[15] = a[3] * b[12] + a[7] * b[13] + a[11] * b[14] + a[15] * b[15];
	}	
	
}
