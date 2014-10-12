package com.mm.tinylove.imp;

import com.google.protobuf.Message;
import com.mm.tinylove.IFollowObject;
import com.mm.tinylove.IRandSet;
import com.mm.tinylove.IUser;

public class FollowStorage<T extends Message.Builder> extends ProtoStorage<T>
		implements IFollowObject {

	public FollowStorage(long id, T ins) {
		super(id, ins);
	}

	static String TAG_OBJ_FOLLOWERS = ":follwers";

	IRandSet<Long> getObjectsFollowers() {
		return new LongRandSet(getKey() + TAG_OBJ_FOLLOWERS);
	}

	@Override
	public IRandSet<IUser> followers() {
		return new ObjectRandSet<IUser>(getObjectsFollowers(), this) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}

		};
	}

}
