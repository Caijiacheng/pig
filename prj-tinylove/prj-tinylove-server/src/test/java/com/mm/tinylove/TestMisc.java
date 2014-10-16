package com.mm.tinylove;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mm.tinylove.util.BytesToType;

public class TestMisc {

	static Logger LOG = LoggerFactory.getLogger(TestMisc.class);

	@Test
	public void testTranform() {
		ImmutableList<String> a = ImmutableList.of("1", "2", "3");

		List<Long> b = Lists.transform(a, new Function<String, Long>() {
			public Long apply(String input) {
				return null;
			}
		});

		LOG.error("{}", b);

	}

	static class ChangeEvent {

	}

	static class NewEvent {

	}

	static AtomicInteger newEventCnt = new AtomicInteger(0);
	static AtomicInteger changeEventCnt = new AtomicInteger(0);

	static class SubIns {
		@Subscribe
		public void newIns(NewEvent event) {
			newEventCnt.incrementAndGet();
		}

		@Subscribe
		public void changeIns(ChangeEvent event) {
			changeEventCnt.incrementAndGet();
		}
	}

	@Test
	public void testEventBus() {
		EventBus eb = new EventBus();

		eb.register(new SubIns());
		eb.register(new SubIns());

		eb.post(new NewEvent());
		eb.post(new ChangeEvent());

		Assert.assertEquals(newEventCnt.get(), 2);
		Assert.assertEquals(changeEventCnt.get(), 2);

	}

	@Test
	public void testDiffSystimeAndRedisTime() {
		// LOG.error("systime:{}, redistime{}", System.currentTimeMillis(),
		// Ins.getStorageService().time());
	}

	

	@Test
	public void testGsonToMap() {
		Map<String, Long> maps = ImmutableMap.of("hello", 1L, "bbbb", 2L);

		byte[] bs = BytesToType.marshalMaps(maps);

		Map<String, Long> maps2 = Maps.newTreeMap();
		maps2 = BytesToType.unmarshalMaps(bs);

		
		LOG.error("maps2={}", maps2.get("hello").getClass());
	}

//	static class AProtoc<T extends Message> {
//		T message;
//		TypeToken<T> genericType = new TypeToken<T>(getClass()) {
//		};
//		Builder b;
//
//		@SuppressWarnings("unchecked")
//		public AProtoc() {
//
//			LOG.error("genericType:{}", genericType.getRawType());
//			try {
//				
//				Method m = genericType.getRawType().getDeclaredMethod(
//						"getDefaultInstance");
//				message = (T) m.invoke(null, null);
//				b = message.newBuilderForType();
//			} catch (NoSuchMethodException | SecurityException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		public Builder getB()
//		{
//			return b;
//		}
//		
//		public <K extends Builder> K getBuilder()
//		{
//			return (K)b;
//		}
//
//	}
//
//	static class ABProtoc extends AProtoc<UserInfo> {
//
//
//	}
//
//	@Test
//	public void testABProtoc() {
//		ABProtoc pro = new ABProtoc();
//		Assert.assertNotNull(pro.getB());
//		pro.getBuilder();
//	}

}
