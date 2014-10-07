package com.mm.tinylove.view.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.mm.tinylove.event.MessageEvent;

public class EventTrigger {

	static Logger LOG = LoggerFactory.getLogger(EventTrigger.class);
	
	public EventTrigger() {
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void creatMessageTrigger(MessageEvent.Creater creator)
	{
//		LOG.error("createMessageTrigger");
		new MessageHotView().add(creator.getMsg());
		new MessageTSView().add(creator.getMsg());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void creatCommentTrigger(MessageEvent.AddComment changeMsg)
	{
		new MessageHotView().add(changeMsg.getMsg());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void creatPriseTrigger(MessageEvent.AddPrise changeMsg)
	{
		new MessageHotView().add(changeMsg.getMsg());
	}
	
}
