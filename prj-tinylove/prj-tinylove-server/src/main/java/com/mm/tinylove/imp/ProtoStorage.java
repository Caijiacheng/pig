package com.mm.tinylove.imp;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.mm.tinylove.IObject;
import com.mm.tinylove.error.UnmarshalException;


/**
 * 为了提高数据库的读写效率。有设计的时候，先给定KVStorage存储的消息内容是不变的（其它的链接关系可以变，这个不影响）
 * 基于这个条件，在load一个数据出来后，扔到缓存中去，就不需要频繁地去Redis中实时查询数据是否是实时有效的
 * 关联关系设计成实时从数据库中去读。这个是可以接受的，毕竟数据量不大。
 * 由于是不变的，在save的时候，也只有第一次会刷数据，以后都不会刷。所以。这里只需要先强制加了Immutable的属性
 * 
 * 当然，可能会有些数据类型会打破这个规则，比如说用户信息IUser
 * 	1）由于这个更新的内容不是敏感的，所以，完全可以扔到缓存中，等缓存中的数据失效了重新加载
 * 
 * 
 * QA:
 * FIXME:
 *  1. Cache support CopyOnWrite()?
 * @author caijiacheng
 *
 * @param <T>
 */

public class ProtoStorage<T extends Message> extends KVStorage {

	static Logger LOG = LoggerFactory.getLogger(ProtoStorage.class);

	protected T value;
	@SuppressWarnings("serial")
	TypeToken<T> genericType = new TypeToken<T>(getClass()) {};

	boolean immutable = true;
	
	public ProtoStorage(long id) {
		super(id);
		if (id == INVAID_KEY)
		{
			immutable = false;
			StorageSaveRunnable.add2Save(this);
		}
	}

	public T getProto() {
		return Preconditions.checkNotNull(value, "The Message is not load");
	}
	
	@SuppressWarnings("unchecked")
	public void rebuildValueAndBrokenImmutable(Message.Builder b)
	{
		immutable = false;
		value = (T) b.build();
		StorageSaveRunnable.add2Save(this);
	}

	
	public Message.Builder getBuilder()
	{
		return newBuilder();
	}
	
	@SuppressWarnings("unchecked")
	public <K extends Message.Builder> K getKBuilder()
	{
		return (K)getBuilder();
	}
	
	Message.Builder newBuilder()
	{
		try {
			Method m = genericType.getRawType().getDeclaredMethod(
					"getDefaultInstance");
			@SuppressWarnings("unchecked")
			T v = (T) m.invoke(null, new Object[]{});
			return v.newBuilderForType();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	// 这里先toByteString(),再ToStringUtf8().主要是为了让数据进行压缩序列化
	@Override
	public byte[] marshalValue() {
		if (immutable)
		{
			return null;
		}
		immutable = true;
		return Preconditions.checkNotNull(value).toByteArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unmarshalValue(byte[] data) {
		try {
			value = (T) newBuilder().mergeFrom(
					data).build();
			immutable = true;
		} catch (InvalidProtocolBufferException e) {
			throw new UnmarshalException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IObject) {
			IObject o = (IObject) obj;
			return o.id() == id();
		}
		return super.equals(obj);
	}
}
