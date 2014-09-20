package com.mm.photo.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IPhotoService {

//	IUrl upload(IPhoto photo);
//	
//	IPhoto download(IUrl url);
	
	boolean isExist(IUrl url);
	
	public IPhoto get(IUrl url);
	
	OutputStream getPhotoOutput(IUrl url)  throws IOException;
	InputStream getPhotoInput(IUrl url)  throws IOException;
	
}
