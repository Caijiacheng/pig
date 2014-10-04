package com.mm.tinylove.imp;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mm.tinylove.proto.Storage.Location;

public class TestStorageService {

	@Before
	public void setup() {
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}

	@Test
	public void testLoadAndSave() {
		DefaultComment comment = DefaultComment.create(1L, 2L, "1");
		Ins.getStorageService().save(comment);

		DefaultComment comment1 = Ins.getStorageService().load(
				new DefaultComment(comment.id()));

		Assert.assertEquals("1", comment1.content());
		Assert.assertEquals(comment.content(), comment1.content());
		Assert.assertEquals(1L, comment1.getProto().getMsgid());
		Assert.assertEquals(2L, comment1.getProto().getUserid());
	}

	@Test
	public void testSaveInTransicaion() {
		DefaultComment comment = DefaultComment.create(1L, 2L, "1");
		DefaultComment comment1 = DefaultComment.create(2L, 3L, "12");

		LongRangeList commentStorage = new LongRangeList("commentStorage");
		commentStorage.lpush(comment.id());
		commentStorage.lpush(comment1.id());

		List<IStorage> iss = Lists.newArrayList();

		iss.add(comment);
		iss.add(comment1);
		iss.add(commentStorage);

		Ins.getStorageService().saveInTransaction(iss);

		DefaultComment comment_load = Ins.getStorageService().load(
				new DefaultComment(comment.id()));
		DefaultComment comment1_load = Ins.getStorageService().load(
				new DefaultComment(comment1.id()));
		Assert.assertEquals(comment_load.content(), comment.content());
		Assert.assertEquals(comment1_load.content(), comment1.content());
		LongRangeList commentStorage1 = new LongRangeList("commentStorage");

		Assert.assertEquals(commentStorage1.size(), 2);
	}

	@Test
	public void testSaveAndLoadMessage() {
		DefaultMessage msg = DefaultMessage.create();
		msg.getProto().setUserid(1L).setPairid(2L).setContent("hello")
				.setLocation(Location.newBuilder().setX(1).setY(2).build())
				.setPhotouri("http://hello.comjkjjhjhjhj")
				.setTimestamp(System.currentTimeMillis());

		Ins.getStorageService().save(msg);
		DefaultMessage msg1 = Ins.getStorageService().load(
				new DefaultMessage(msg.id()));

		Assert.assertEquals(msg, msg1);
		Assert.assertEquals(msg.location().getX(), 1f);
		Assert.assertEquals(msg.location().getY(), 2f);

	}

}
