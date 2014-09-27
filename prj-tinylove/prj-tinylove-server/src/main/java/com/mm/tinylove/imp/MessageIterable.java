package com.mm.tinylove.imp;

import java.util.Iterator;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.mm.tinylove.IMessage;

public class MessageIterable extends FluentIterable<IMessage>{

	
	@Override
	public Iterator<IMessage> iterator() {
		return new MessageIterator();
	}
	
	static class MessageIterator implements Iterator<IMessage>
	{

		long cur = Ins.getUniqService().curID(
				new DefaultMessage(-1).uniqKey().getBytes(Charsets.UTF_8));
		
		IMessage handle;
		
		@Override
		public boolean hasNext() {
			
			while(cur >= 0)
			{
				if (!Ins.getStorageService().exist(
						new DefaultMessage(
								cur).getKey().getBytes(Charsets.UTF_8)))
				{
					cur = cur - 1;
				}
			}
			if (cur < 0)
			{
				return false;
			}
			
			return true;
		}

		@Override
		public IMessage next() {
			IMessage msg =  Ins.getIMessage(cur);
			cur = cur - 1;
			return msg;
		}

		@Override
		public void remove() {
			
		}
		
	}

}
