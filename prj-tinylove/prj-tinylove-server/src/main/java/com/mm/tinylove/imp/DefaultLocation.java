package com.mm.tinylove.imp;

import com.mm.tinylove.ILocation;
import com.mm.tinylove.proto.Storage.Location;

public class DefaultLocation implements ILocation{
	
	float x;
	float y;
	
	public DefaultLocation(ILocation pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}
	
	public DefaultLocation(Location pos)
	{
		this.x = pos.getX();
		this.y = pos.getY();
	}
	
	
	Location toLocation()
	{
		return Location.newBuilder().setX(x).setY(y).build();
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}
	
}
