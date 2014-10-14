package com.mm.tinylove.imp;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IStory;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Story;

public class DefaultStory extends FollowStorage<Story> implements IStory {

	public DefaultStory(long id) {
		super(id);
	}

	static DefaultStory create(IUser user, IPair pair) {
		DefaultStory s = new DefaultStory(INVAID_KEY);
		Story.Builder builder = s.getKBuilder();
		s.rebuildValueAndBrokenImmutable(builder.setUserid(user.id()).setPairid(
				pair.id()));
		return s;
	}

	static String MSG_TAG = ":messages";

	IRangeList<Long> getStorysMessagesIDs() {
		return LongRangeList.getIns(getKey() + MSG_TAG);
	}

	@Override
	public IRangeList<IMessage> message() {

		return new ObjectRangeList<IMessage>(getStorysMessagesIDs()) {
			public IMessage apply(Long id) {
				return Ins.getIMessage(id);
			}
		};
	}

	@Override
	public IPair pair() {
		return Ins.getIPair(value.getPairid());
	}

}
