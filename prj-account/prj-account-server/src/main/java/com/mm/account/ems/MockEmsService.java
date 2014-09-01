package com.mm.account.ems;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mm.account.error.EMSException;

public class MockEmsService extends CacheEmsService {

	
	static Logger LOG = LoggerFactory.getLogger(MockEmsService.class);
	
	static class MockEms extends PojoEms
	{
		@Override
		public String toString() {
			return super.toString();
		}
	}
	
	@Override
	public void send(IEms ems) {
		LOG.warn("i am sending EMS: {}", ems);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public IEms newEms(String phonenum) {
		
		MockEms ems = new MockEms();
		ems._phonenum = 
				phonenum;
		ems._code = 
				Integer.toString(ThreadLocalRandom.current().nextInt(8999) + 1000);
		return ems;
	}
	
	@Override
	public IEms getEms(String phone)
	{
		try {
			return _emsCache.get(phone);
		} catch (ExecutionException e) {
			throw new EMSException(e);
		}
	}
	
	@Override
	public boolean checkEmsVaild(IEms ems)
	{
		return Objects.equals(ems, _emsCache.getIfPresent(ems.phonenum()));
	}
	

}
