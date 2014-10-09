package com.mm.tinylove.notify;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mm.tinylove.imp.AbstractNotify;
import com.mm.tinylove.proto.Storage.Notify.Builder;
import com.mm.tinylove.util.BytesToType;

abstract public class AbsBundleNotify extends AbstractNotify{

	public AbsBundleNotify(long id, Builder builder) {
		super(id, builder);
	}

	public void setBundle(String key, Long id)
	{
		bundle.put(key, id);
	}
	
	Map<String, Long> bundle = Maps.newTreeMap();
	
	abstract String[] verifyBundleKeys();
	
	@Override
	protected byte[] marshalNotifyValue() {
		for (String k : verifyBundleKeys())
		{
			Preconditions.checkArgument(bundle.containsKey(k));
		}
		return BytesToType.marshalMaps(bundle);
	}

	@Override
	protected void unmarshalNotifyValue(byte[] value) {
		bundle = BytesToType.unmarshalMaps(value);
	}
	
}
