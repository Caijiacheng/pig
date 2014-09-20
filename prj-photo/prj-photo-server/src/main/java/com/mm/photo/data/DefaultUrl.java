package com.mm.photo.data;

public class DefaultUrl implements IUrl{

	String url;
	public DefaultUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String url() {
		return url;
	}
	
	@Override
	public String toString() {
		return url;
	}

}
