package com.mm.account.data;

public interface IPhotoService {

	IUrl upload(IPhoto photo);
	
	IPhoto download(IUrl url);
	
}
