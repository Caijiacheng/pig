package com.mm.photo.data;

import java.io.IOException;
import java.io.InputStream;

public interface IPhoto {
	InputStream data() throws IOException ;
	
	String uniqname();
	
	boolean isExist();
	
	long length();
	
}
