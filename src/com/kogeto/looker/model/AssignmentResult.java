package com.kogeto.looker.model;

public class AssignmentResult extends Result {
	public String bucket;
	public String assignmentid;
	public long bucket_size;
	public String videokey;
	public int valid;
	
	public boolean isValid(){
		return valid == 1;
	}
}
