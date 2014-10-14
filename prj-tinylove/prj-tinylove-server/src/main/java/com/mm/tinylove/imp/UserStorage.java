package com.mm.tinylove.imp;

import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IUser;

public class UserStorage extends LongRangeList{
	
	static final String USER_RANGE_LIST = "UserRangeList";
	private UserStorage() {
		super(USER_RANGE_LIST);
	}
	
	
	static ThreadLocal<UserStorage> TL_USER_STORAGE = new ThreadLocal<UserStorage>()
			{
				protected UserStorage initialValue() {
					return new UserStorage();
				};
			};
	
	static public UserStorage getIns()
	{
		return TL_USER_STORAGE.get();
	}
	
	public static IUser createUserAndSave()
	{
		
		StorageSaveRunnable r = new StorageSaveRunnable() {
			
			@Override
			protected Object onSaveTransactionRun() {
				DefaultUser user = DefaultUser.create();
				UserStorage users = new UserStorage();
				users.lpush(user.id());
				return user;
			}
		};
		r.run();
		return (IUser)r.getResult();
	}
	
	public IRangeList<IUser> userList() {
		return new ObjectRangeList<IUser>(this) 
				{
				public IUser apply(Long id)
				{
					return Ins.getIUser(id);
				}
		};
	}
	
}
