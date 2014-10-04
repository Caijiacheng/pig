package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.collect.Lists;
import com.mm.tinylove.IUser;

public class UserStorage extends LongRangeList{
	
	static final String USER_RANGE_LIST = "UserRangeList";
	public UserStorage() {
		super(USER_RANGE_LIST);
	}
	
	static IUser creatAndSave()
	{
		DefaultUser user = DefaultUser.create();
		UserStorage users = new UserStorage();
		users.lpush(user.id());
		List<IStorage> iss = Lists.newArrayList();
		iss.add(user);
		iss.add(users);
		Ins.getStorageService().saveInTransaction(iss);
		return user;
	}
	
}
