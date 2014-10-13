package com.mm.tinylove.imp;

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
import com.mm.tinylove.event.MessageEvent;
import com.mm.tinylove.proto.Storage.Msg;
import com.mm.tinylove.proto.Storage.UserInfo;

public class DefaultUser extends FollowStorage<UserInfo> implements IUser {

	public DefaultUser(long id) {
		super(id);
	}

	static DefaultUser create() {
		DefaultUser user = new DefaultUser(INVAID_KEY);
		user.rebuildValueAndBrokenImmutable(user.getBuilder());
		return user;
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

	IRangeList<Long> getUserNotifyIDs() {
		return new LongRangeList(getKey() + NOTIFY_TAG);
	}

	@Override
	public IRangeList<IStory> userStorys() {

		return new ObjectRangeList<IStory>(getUserStorysIDs()) {
			public IStory apply(Long id) {
				return Ins.getIStory(id);
			}
		};
	}

	@Override
	public IRangeList<IComment> userComments() {
		return new ObjectRangeList<IComment>(getUserCommentIDs()) {
			public IComment apply(Long id) {
				return Ins.getIComment(id);
			}
		};
	}

	@Override
	public IRangeList<IPair> userPairs() {
		return new ObjectRangeList<IPair>(getUserPairsIDs()) {
			public IPair apply(Long id) {
				return Ins.getIPair(id);
			}
		};
	}

	@Override
	public IRangeList<IMessage> msgPrised() {
		return new ObjectRangeList<IMessage>(getUserMsgPriseIDs()) {
			public IMessage apply(Long id) {
				return Ins.getIMessage(id);
			}
		};
	}

	@Override
	public IRangeList<IComment> commentPrise() {
		return new ObjectRangeList<IComment>(getUserCommentPriseIDs()) {
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
	public IMessage publishMsg(final IPair pair, final String content,
			final ILocation location, final String imgurl, final String videourl) {

		StorageSaveRunnable r = new StorageSaveRunnable() {

			@Override
			Object onSaveTransactionRun() {

				DefaultMessage message = DefaultMessage.create();

				Msg.Builder msgbuilder = message.getKBuilder();
				msgbuilder.setPairid(pair.id()).setUserid(id())
						.setContent(content)
						.setPhotouri(imgurl == null ? "" : imgurl)
						.setVideouri(videourl == null ? "" : videourl)
						.setTimestamp(Ins.getStorageService().time());
				// check ipair in the pairs

				if (!userPairs().exist(pair)) {
					throw new NotExistException(
							"Not exist IPair in userPairs(): " + pair);
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
					relate = DefaultStory.create(id(), pair.id());
					userStorys().lpush(relate);
				}

				msgbuilder.setStoryid(relate.id());
				msgbuilder.setLocation(new DefaultLocation(location)
						.toLocation());
				message.rebuildValueAndBrokenImmutable(msgbuilder);

				// add message to story
				relate.message().lpush(message);

				MessageStorage msgstorage = new MessageStorage();
				msgstorage.lpush(message.id());

				DefaultPair d_pair = (DefaultPair) pair;
				if (d_pair.creator().id() != id()) {
					// use common pair. so, maybe we need to add to the list
					if (!d_pair.user().exist(DefaultUser.this)) {
						d_pair.user().sadd(DefaultUser.this);
					}
				}

				return message;
			}

			void onSuccess() {
				Ins.getEventBus().post(
						new MessageEvent.Creater((IMessage) getResult()));
			}
		};

		r.run();
		return (IMessage) r.getResult();

	
	}

	@Override
	public IComment publishComment(final IMessage msg, final String content) {

		StorageSaveRunnable r = new StorageSaveRunnable() {

			@Override
			Object onSaveTransactionRun() {
				DefaultComment comment = DefaultComment.create(msg.id(), id(),
						content);
				msg.comments().lpush(comment);
				userComments().lpush(comment);
				return comment;
			}

			void onSuccess() {
				Ins.getEventBus()
						.post(new MessageEvent.AddComment(msg,
								(IComment) getResult()));
			}
		};

		r.run();

		return (IComment) r.getResult();
	}

	@Override
	public void publishPrise(final IMessage msg) {

		new StorageSaveRunnable() {
			@Override
			Object onSaveTransactionRun() {
				if (msgPrised().exist(msg)) {
					return null;
				}
				msg.prisers().lpush(DefaultUser.this);
				msgPrised().lpush(msg);
				return null;
			}

			void onSuccess() {
				Ins.getEventBus().post(
						new MessageEvent.AddPrise(msg, DefaultUser.this));
			}

		}.run();

	}

	@Override
	public void publishPriseOfComment(final IComment comment) {

		new StorageSaveRunnable() {
			Object onSaveTransactionRun() {
				if (commentPrise().exist(comment)) {
					return null;
				}
				comment.prisers().lpush(DefaultUser.this);
				commentPrise().lpush(comment);
				return null;
			}
		}.run();

	}

	@Override
	public IPair createPair(final String name) {
		
		StorageSaveRunnable r = new StorageSaveRunnable()
		{

			@Override
			Object onSaveTransactionRun() {
				
				DefaultPair pair = DefaultPair.create(name, id());
				userPairs().lpush(pair);
				return pair;
			}
			
		};
		r.run();
		return (IPair)r.getResult();
	}

	@Override
	public void follow(IObject obj) {
	}

	@Override
	public void unfollow(IObject obj) {
	}

}
