package com.mm.tinylove.util;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class BytesToType {

	@SuppressWarnings("serial")
	@Deprecated
	public static <T, K> byte[] marshalMaps(Map<T, K> ids) {
		return new Gson().toJson(ids, new TypeToken<Map<T, K>>() {
		}.getType()).getBytes(StandardCharsets.UTF_8);
	}

	// bug here Map<String, Long>
	@SuppressWarnings("serial")
	@Deprecated
	public static <T, K> Map<T, K> unmarshalMaps(byte[] bs) {
		return new Gson().fromJson(new String(bs, StandardCharsets.UTF_8),
				new TypeToken<TreeMap<T, K>>() {
				}.getType());
	}

	public static byte[] marshalMapSL(Map<String, Long> maps) {
		return new Gson().toJson(maps).getBytes(StandardCharsets.UTF_8);
	}

	public static Map<String, Long> unmarshalMapSL(byte[] value) {
		@SuppressWarnings("unused")
		Map<String, Double> map = new Gson().fromJson(new String(value,
				StandardCharsets.UTF_8),
				new TypeToken<TreeMap<String, Double>>() {
				}.getType());
		
//		Map<String, Long> m = new Gson()
		return null;

	}
	// public static byte[] marshalMapsLong(Map<String, Long>)
}
