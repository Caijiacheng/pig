package com.mm.tinylove.imp;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mm.tinylove.IComment;
import com.mm.tinylove.IMessage;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Location;

public class TestStorageService {

	@Before
	public void setup() {
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}

	@Test
	public void testLoadAndSave() {

		IUser user = UserStorage.createUserAndSave();
		//DefaultPair pair = DefaultPair.create("hello", user);
		IPair pair = user.createPair("hello");


		IMessage msg = user.publishMsg(pair, "hello", new DefaultLocation(Location
				.newBuilder().setX(1.0f).setY(1.2f).build()), "", "");

		
		IComment comment = user.publishComment(msg, "1");

		DefaultComment comment1 = Ins.getStorageService().load(
				new DefaultComment(comment.id()));

		Assert.assertEquals("1", comment1.content());
		Assert.assertEquals(comment.content(), comment1.content());
		Assert.assertEquals(msg.id(), comment1.getProto().getMsgid());
		Assert.assertEquals(user.id(), comment1.getProto().getUserid());
	}

	@Test
	public void testSaveInTransicaion() {
		
		IUser user = UserStorage.createUserAndSave();

		IPair pair = user.createPair("hh");

		IMessage msg = user.publishMsg(pair, "hello", new DefaultLocation(Location
				.newBuilder().setX(1.0f).setY(1.2f).build()), "", "");
		
		
		IComment comment = user.publishComment(msg, "1");
		IComment comment1 = user.publishComment(msg, "12");
		

		DefaultComment comment_load = Ins.getStorageService().load(
				new DefaultComment(comment.id()));
		DefaultComment comment1_load = Ins.getStorageService().load(
				new DefaultComment(comment1.id()));
		Assert.assertEquals(comment_load.content(), comment.content());
		Assert.assertEquals(comment1_load.content(), comment1.content());
	}

	
	static Logger LOG = LoggerFactory.getLogger(TestStorageService.class);
	
	@Test
	public void testSaveInTransicationInRunnable() {
		StorageSaveRunnable r = new StorageSaveRunnable() {

			Map<String, IComment> bundle = Maps.newHashMap();

			@Override
			protected Object onSaveTransactionRun() {

				IUser user = UserStorage.createUserAndSave();
				//DefaultPair pair = DefaultPair.create("hello", user);
				IPair pair = user.createPair("hello");

				IMessage msg = user.publishMsg(pair, "hello", new DefaultLocation(Location
						.newBuilder().setX(1.0f).setY(1.2f).build()), "", "");
				
				IComment comment = user.publishComment(msg, "1");
				IComment comment1 = user.publishComment(msg, "12");

				LongRangeList commentStorage = new LongRangeList(
						"commentStorage");
				commentStorage.lpush(comment.id());
				commentStorage.lpush(comment1.id());

				bundle.put("comment", comment);
				bundle.put("comment1", comment1);

				return bundle;
			}

		};
		r.run();
		
		LOG.error("r.result()={}", r.getResult());
		@SuppressWarnings("unchecked")
		Map<String, IComment> ret = (Map<String, IComment>) r
				.getResult();
		DefaultComment comment_load = Ins.getStorageService().load(
				new DefaultComment(ret.get("comment").id()));
		DefaultComment comment1_load = Ins.getStorageService().load(
				new DefaultComment(ret.get("comment1").id()));
		Assert.assertEquals(comment_load.content(), ret.get("comment")
				.content());
		Assert.assertEquals(comment1_load.content(), ret.get("comment1")
				.content());
		LongRangeList commentStorage1 = new LongRangeList("commentStorage");

		Assert.assertEquals(commentStorage1.size(), 2);

	}

	
	@Test
	public void testSetOper() {

		String key = "tSetOper";

		DefaultStorageService service = new DefaultStorageService();
		service.sadd(key, ImmutableSet.of(1L, 2L, 3L));

		Assert.assertEquals(service.sall(key).size(), 3);
		Set<Long> t = service.srandmem(key, 5);

		Assert.assertTrue(t.contains(1L));
		Assert.assertTrue(t.contains(2L));
		Assert.assertTrue(t.contains(3L));

		service.srem(key, 1L);

		Assert.assertEquals(service.sall(key).size(), 2);

	}

}
