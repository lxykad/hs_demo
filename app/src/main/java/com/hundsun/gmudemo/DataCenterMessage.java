package com.hundsun.gmudemo;

public class DataCenterMessage {
	private  Object mUserInfo;
	private  Object mData;
	
	public void setUserInfo( Object userInfo ){
		mUserInfo = userInfo;
	}
	
	public Object getUserInfo(){
		return mUserInfo;
	}
	
	public <T> T getMessageData( T t){
		return (T) mData;
	}
	
	public void setMessageData( Object data){
		mData = data;
	}
}

