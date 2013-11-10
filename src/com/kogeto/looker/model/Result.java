package com.kogeto.looker.model;


public class Result {
	public static final String OK = "ok";
	public static final String FAIL = "fail";
	
	public String stat;

	public Error err;
	
	public Result(){
		this.err = new Error();
	}

	public class Error{
		public int code;
		public String msg;
	}
	
	
    public boolean isOk(){
    	return stat.equalsIgnoreCase(OK);
    }
}
