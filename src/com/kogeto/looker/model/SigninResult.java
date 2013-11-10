package com.kogeto.looker.model;

public class SigninResult extends Result {
	public User user;
	
	public class User {
		public String username;
		public String email;
		public String looker_token;
	}
}
