package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mm.tinylove.IComment;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IPrise;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.error.NotExistException;
import com.mm.tinylove.proto.Storage.Location;
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

	
	IRangeList<Long> getUserStorysIDs()
	{
		return new LongRangeList(getKey()+":storys");
	}
	
	IRangeList<Long> getUserCommentIDs()
	{
		return new LongRangeList(getKey()+":comments");
	}
	
	IRangeList<Long> getUserPairsIDs()
	{
		return new LongRangeList(getKey()+":pairs");
	}
	
	@Override
	public IRangeList<IStory> userStorys() {

		return Lists.transform(getUserStorysIDs(),
				new Function<Long, IStory>() {
					public IStory apply(Long id) {
						return Ins.getIStory(id);
					}
				});
	}

	@Override
	public List<IComment> userComment() {
		return Lists.transform(getUserCommentIDs(),
				new Function<Long, IComment>() {
					public IComment apply(Long id) {
						return Ins.getIComment(id);
					}
				});
	}

	@Override
	public List<IPair> userPairs() {
		return Lists.transform(getUserPairsIDs(),
				new Function<Long, IPair>() {
					public IPair apply(Long id) {
						return Ins.getIPair(id);
					}
				});
	}

	
	//这里一定要小心save顺序的问题.不同的顺序,在硬件故障的时候,可能会引起关联表的混乱
	@Override
	public
	IMessage publishMsg(final IPair pair, String content, ILocation location, String imgurl,
			String videourl) {
		
		DefaultMessage message = DefaultMessage.create();
		message.value.setPairid(pair.id());
		message.value.setContent(content);
		message.value.setPhotouri(imgurl == null ? "" : imgurl);
		message.value.setVideouri(videourl == null ? "" : videourl);
		//check ipair in the pairs
		if (!Iterables.any(userPairs(), new Predicate<IPair>() {
			public boolean apply(IPair p)
			{
				if (p.equals(pair))
				{
					return true;
				}
				return false;
			}
			
		}))
		{
			throw new NotExistException("Not exist IPair: " + pair);
		}
		
		IStory relate = null;
		for (IStory story : userStorys())
		{
			if (story.pair().equals(pair))
			{
				relate = story;
				break;
			}
		}
		if (relate == null)//new
		{
			relate = DefaultStory.create();
		}
		message.value.setStoryid(relate.id());
		message.value.setLocation(
				new DefaultLocation(location).toLocation());
				
		Ins.getStorageService().save(message);
		
		//relate.message().add(message);
		//relate.save()
		//add to MessageStorage
		//messageStorage.save()
		//if relate is new:
		//	add to user story
		//add to user
		
		return message;
	}

	@Override
	public IComment publishComment(IMessage msg, String content) {
		
		
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
