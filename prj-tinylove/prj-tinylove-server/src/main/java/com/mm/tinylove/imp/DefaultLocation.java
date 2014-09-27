package com.mm.tinylove.imp;

import com.mm.tinylove.ILocation;
import com.mm.tinylove.proto.Storage.Location;

public class DefaultLocation implements ILocation{
	
	
	Location pos;
	public DefaultLocation(Location pos) {
		this.pos = pos;
	}
	
	
	
}
