package com.mm.tinylove.imp;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.proto.Storage.Location;

public class DefaultLocation implements ILocation {

	float x;
	float y;

	public DefaultLocation(ILocation pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}

	public DefaultLocation(Location pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}

	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.x, this.y);
	}
	
	public boolean equals(Object obj) {

		if (obj instanceof ILocation) {
			ILocation o = (ILocation) obj;
			return ComparisonChain.start().compare(this.x, o.getX())
					.compare(this.y, o.getY()).result() == 0;
		}
		return super.equals(obj);
	};

	Location toLocation() {
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
