package com.mm.tinylove.util;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class BytesToType {
	
	public static <T, K> byte[] marshalMaps(Map<T, K> ids) {
		return new Gson().toJson(ids).getBytes(StandardCharsets.UTF_8);
	}

	@SuppressWarnings("serial")
	public static <T, K> Map<T, K> unmarshalMaps(byte[] bs) {
		return new Gson().fromJson(new String(bs, StandardCharsets.UTF_8),
				new TypeToken<TreeMap<T, K>>() {
				}.getType());
	}
}
