package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mm.tinylove.IComment;
import com.mm.tinylove.ILocation;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.INotify;
import com.mm.tinylove.IObject;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.error.NotExistException;
import com.mm.tinylove.event.CommentEvent;
import com.mm.tinylove.event.MessageEvent;
import com.mm.tinylove.proto.Storage.UserInfo;

public class DefaultUser extends FollowStorage<UserInfo.Builder> implements
		IUser {

	public DefaultUser(long id) {
		super(id, UserInfo.newBuilder());
	}

	static DefaultUser create() {
		return new DefaultUser(INVAID_KEY);
	}

	@Override
	public String name() {
		return getProto().getName();
	}

	static final String STORYS_TAG = ":storys";
	static final String COMMENT_TAG = ":comments";
	static final String PAIRS_TAG = ":pairs";
	static final String MSG_PRISE_TAG = ":msg:prise";
	static final String COMMENT_PRISE_TAG = ":comment:prise";
	static final String NOTIFY_TAG = ":notify";

	IRangeList<Long> getUserStorysIDs() {
		return new LongRangeList(getKey() + STORYS_TAG);
	}

	IRangeList<Long> getUserCommentIDs() {
		return new LongRangeList(getKey() + COMMENT_TAG);
	}

	IRangeList<Long> getUserPairsIDs() {
		return new LongRangeList(getKey() + PAIRS_TAG);
	}

	IRangeList<Long> getUserMsgPriseIDs() {
		return new LongRangeList(getKey() + MSG_PRISE_TAG);
	}

	IRangeList<Long> getUserCommentPriseIDs() {
		return new LongRangeList(getKey() + COMMENT_PRISE_TAG);
	}
	
	IRangeList<Long> getUserNotifyIDs()
	{
		return new LongRangeList(getKey() + NOTIFY_TAG);
	}

	@Override
	public IRangeList<IStory> userStorys() {

		return new ImmutableObjectRangeList<IStory>(getUserStorysIDs()) {
			public IStory apply(Long id) {
				return Ins.getIStory(id);
			}
		};
	}

	@Override
	public IRangeList<IComment> userComments() {
		return new ImmutableObjectRangeList<IComment>(getUserCommentIDs()) {
			public IComment apply(Long id) {
				return Ins.getIComment(id);
			}
		};
	}

	@Override
	public IRangeList<IPair> userPairs() {
		return new ImmutableObjectRangeList<IPair>(getUserPairsIDs()) {
			public IPair apply(Long id) {
				return Ins.getIPair(id);
			}
		};
	}

	@Override
	public IRangeList<IMessage> msgPrise() {
		return new ImmutableObjectRangeList<IMessage>(getUserMsgPriseIDs()) {
			public IMessage apply(Long id) {
				return Ins.getIMessage(id);
			}
		};
	}

	@Override
	public IRangeList<IComment> commentPrise() {
		return new ImmutableObjectRangeList<IComment>(getUserCommentPriseIDs()) {
			public IComment apply(Long id) {
				return Ins.getIComment(id);
			}
		};
	}

	@Override
	public IRangeList<INotify<?>> userNotifys() {
		return null;
	}
	
	/**
	 * 这里需要处理各个存储的id关联关系.比较复杂.最终所有的存储数据用事务一起存到数据库中
	 */
	@Override
	public IMessage publishMsg(final IPair pair, String content,
			ILocation location, String imgurl, String videourl) {

		List<IStorage> ins_to_save = Lists.newArrayList();

		DefaultMessage message = DefaultMessage.create();
		ins_to_save.add(message);
		message.getProto().setPairid(pair.id());
		message.getProto().setUserid(this.id());
		message.getProto().setContent(content);
		message.getProto().setPhotouri(imgurl == null ? "" : imgurl);
		message.getProto().setVideouri(videourl == null ? "" : videourl);
		// message.getProto().setTimestamp(System.currentTimeMillis());
		message.getProto().setTimestamp(Ins.getStorageService().time()); // storage

		// check ipair in the pairs
		if (!Iterables.any(getUserPairsIDs().all(), new Predicate<Long>() {
			public boolean apply(Long p) {
				if (p.equals(pair.id())) {
					return true;
				}
				return false;
			}

		})) {
			throw new NotExistException("Not exist IPair: " + pair);
		}

		DefaultStory relate = null;

		for (IStory story : userStorys().all()) {
			if (story.pair().id() == pair.id()) {
				relate = (DefaultStory) story;
				break;
			}
		}

		if (relate == null)// new
		{
			relate = DefaultStory.create(this.id(), pair.id());
			LongRangeList l_storys = (LongRangeList) getUserStorysIDs();
			l_storys.lpush(relate.id());
			ins_to_save.add(relate);
			ins_to_save.add(l_storys);
		}
		message.value.setStoryid(relate.id());
		message.value.setLocation(new DefaultLocation(location).toLocation());
		// add message to story
		LongRangeList msgids = (LongRangeList) relate.getStorysMessagesIDs();
		msgids.lpush(message.id());
		ins_to_save.add(msgids);

		MessageStorage msgstorage = new MessageStorage();
		msgstorage.lpush(message.id());
		ins_to_save.add(msgstorage);

		DefaultPair d_pair = (DefaultPair) pair;
		if (d_pair.creator().id() != this.id()) {
			// use common pair. so, maybe we need to add to the list
			LongRangeList userids = (LongRangeList) d_pair.getPairsUserIDs();

			if (!Iterables.any(userids.all(), new Predicate<Long>() {
				public boolean apply(Long id) {
					return id.longValue() == DefaultUser.this.id();
				}
			})) {
				userids.lpush(this.id());
				ins_to_save.add(userids);
			}
		}

		Ins.getStorageService().saveInTransaction(ins_to_save);

		Ins.getEventBus().post(new MessageEvent.Creater(message));

		return message;
	}

	@Override
	public IComment publishComment(IMessage msg, String content) {
		DefaultMessage d_msg = (DefaultMessage) msg;

		IRangeList<Long> commentids = d_msg.getMsgCommentsIds();

		DefaultComment comment = DefaultComment.create(msg.id(), this.id(),
				content);
		commentids.lpush(comment.id());

		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add((LongRangeList) commentids);
		ins_to_save.add(comment);
		Ins.getStorageService().saveInTransaction(ins_to_save);

		Ins.getEventBus().post(new MessageEvent.AddComment(msg, comment));

		return comment;
	}

	@Override
	public void publishPrise(IMessage msg) {
		// check: 我是否已经赞过这个msg
		for (Long msgid : getUserMsgPriseIDs().all()) {
			if (msgid.longValue() == msg.id()) {
				return;
			}
		}
		DefaultMessage d_msg = (DefaultMessage) msg;
		LongRangeList prise_ids = (LongRangeList) d_msg.getMsgPriserIds();
		prise_ids.lpush(this.id());
		LongRangeList user_msg_prise = (LongRangeList) this
				.getUserMsgPriseIDs();
		user_msg_prise.lpush(msg.id());
		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add(prise_ids);
		ins_to_save.add(user_msg_prise);
		Ins.getStorageService().saveInTransaction(ins_to_save);

		Ins.getEventBus().post(new MessageEvent.AddPrise(msg, this));
	}

	@Override
	public void publishPriseOfComment(IComment comment) {
		for (Long commentid : getUserCommentPriseIDs().all()) {
			if (commentid.longValue() == comment.id()) {
				return;
			}
		}
		DefaultComment d_comment = (DefaultComment) comment;
		LongRangeList prise_ids = (LongRangeList) d_comment
				.getCommentPriserIds();
		prise_ids.lpush(this.id());
		LongRangeList user_comment_prise = (LongRangeList) this
				.getUserCommentPriseIDs();
		user_comment_prise.lpush(d_comment.id());
		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add(prise_ids);
		ins_to_save.add(user_comment_prise);
		Ins.getStorageService().saveInTransaction(ins_to_save);

		Ins.getEventBus().post(new CommentEvent.AddPrise(d_comment, this));
	}

	@Override
	public IPair createPair(String name) {
		DefaultPair pair = DefaultPair.create(name, this.id());
		LongRangeList pair_list = (LongRangeList) getUserPairsIDs();
		pair_list.lpush(pair.id());

		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add(pair);
		ins_to_save.add(pair_list);
		Ins.getStorageService().saveInTransaction(ins_to_save);
		return pair;
	}

	@Override
	public void follow(IObject obj) {
		LongRangeList follower_list = (LongRangeList) ((FollowStorage<?>) obj)
				.getObjectsFollowers();
		follower_list.lpush(this.id());
		Ins.getStorageService().save(follower_list);
	}

	@Override
	public void unfollow(IObject obj) {
		LongRangeList follower_list = (LongRangeList) ((FollowStorage<?>) obj)
				.getObjectsFollowers();
		
		Ins.getLongRangeService().removeElement(follower_list.key, obj.id());
	}

	

}
