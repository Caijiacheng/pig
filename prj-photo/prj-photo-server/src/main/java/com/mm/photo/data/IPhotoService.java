package com.mm.photo.data;

public interface IPhotoService {

	IUrl upload(IPhoto photo);
	
	IPhoto download(IUrl url);
	
}
