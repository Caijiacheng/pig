package com.mm.tinylove.view;

import java.nio.charset.StandardCharsets;

import com.mm.tinylove.IMessage;
import com.mm.tinylove.imp.Ins;


/**
 * only sort by id
 * @author apple
 *
 */

public abstract class MessageView extends SortSetStorage<IMessage>{

	
	public MessageView(String key) {
		super(key);
		Ins.getEventBus().register(this);
	}

	@Override
	byte[] marshalValue(IMessage obj) {
		return String.valueOf(obj.id()).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	IMessage unmarshalValue(byte[] bs) {
		long id = Long.parseLong(new String(bs, StandardCharsets.UTF_8));
		return Ins.getIMessage(id);
	}
	
	abstract protected double getScore(IMessage obj);
	
	public void add(IMessage obj)
	{
		add(obj, getScore(obj));
	}
	

	
	
}
