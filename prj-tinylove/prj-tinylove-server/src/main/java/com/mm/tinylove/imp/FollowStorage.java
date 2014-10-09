package com.mm.tinylove.imp;

import com.google.common.collect.Collections2;
import com.google.protobuf.Message;
import com.mm.tinylove.IFollowObject;
import com.mm.tinylove.IRandSet;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IUser;

public class FollowStorage<T extends Message.Builder> extends ProtoStorage<T> implements IFollowObject{

	public FollowStorage(long id, T ins) {
		super(id, ins);
	}

	static String TAG_OBJ_FOLLOWERS = ":follwers";

	IRangeList<Long> getObjectsFollowers() {
		return new LongRangeList(getKey() + TAG_OBJ_FOLLOWERS);
	}	
	
	@Override
	public IRandSet<IUser> followers() {
		
		//Collections2.transform(fromCollection, function)
		
//		return new ImmutableObjectRangeList<IUser>(getObjectsFollowers()) {
//			public IUser apply(Long id) {
//				return Ins.getIUser(id);
//			}
//		};
	}

}
