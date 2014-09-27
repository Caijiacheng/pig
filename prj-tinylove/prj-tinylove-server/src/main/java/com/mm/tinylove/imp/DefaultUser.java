package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IPrise;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.UserInfo;

public class DefaultUser extends ProtoStorage<UserInfo.Builder> implements
		IUser {

	public DefaultUser(long id) {
		super(id, UserInfo.newBuilder());
	}

	@Override
	public String name() {
		return getProto().getName();
	}

	@Override
	public List<IStory> userStorys() {

		return Lists.transform(getProto().getStorysList(),
				new Function<Long, IStory>() {
					public IStory apply(Long id) {
						return Ins.getIStory(id);
					}
				});
	}

	@Override
	public List<IComment> userComment() {
		return Lists.transform(getProto().getCommentsList(),
				new Function<Long, IComment>() {
					public IComment apply(Long id) {
						return Ins.getIComment(id);
					}
				});
	}

	@Override
	public List<IPair> userPairs() {
		return Lists.transform(getProto().getPairsList(),
				new Function<Long, IPair>() {
					public IPair apply(Long id) {
						return Ins.getIPair(id);
					}
				});
	}

	@Override
	public
	IMessage publishMsg(IPair pair, String content, String imgurl,
			String videourl) {
		
		
		
		
		return null;
	}

	@Override
	public IComment publishComment(IMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPrise publishPrise(IMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPrise publishPriseOfComment(IComment comment) {
		// TODO Auto-generated method stub
		return null;
	}

}
