package com.mm.util.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

abstract public class AbsDBHandle<T extends AutoCloseable> implements
		IDBHandle<T> {
	static ThreadLocal<List<AutoCloseable>> s_conn_threadlocal = new ThreadLocal<List<AutoCloseable>>() {
		protected java.util.List<AutoCloseable> initialValue() {
			return new ArrayList<>();
		};
	};

	static ThreadLocal<Boolean> s_conn_share_mode = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return Boolean.FALSE;
		};
	};

	static public Runnable decorateShareConnModeRunnable(final Runnable r) {
		return new Runnable() {

			@Override
			public void run() {
				s_conn_share_mode.set(Boolean.TRUE);
				try {
					r.run();
				} finally {
					s_conn_share_mode.set(Boolean.FALSE);
					for (AutoCloseable con : s_conn_threadlocal.get()) {
						try {
							con.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					s_conn_threadlocal.remove();
					s_conn_share_mode.remove();
				}

			}
		};
	}

	static public class DecorateShareConnModeThreadFactory implements
			ThreadFactory {

		@Override
		public Thread newThread(final Runnable r) {

			return new Thread() {
				@Override
				public void run() {
					decorateShareConnModeRunnable(r).run();
				}
			};
		}
	}

	T share_conn = null;

	abstract protected T doConnect();

	@Override
	public T getConn() {
		if (s_conn_share_mode.get()) {
			return getSharedConn();
		}

		return doConnect();
	}

	@SuppressWarnings("serial")
	TypeToken<T> genericType = new TypeToken<T>(getClass()) {
	};

	static Logger LOG = LoggerFactory.getLogger("AbsDbHandle");

	@Override
	public T getSharedConn() {
		Preconditions.checkArgument(s_conn_share_mode.get(),
				"must in decorateShareConnModeRunnable()");

		if (share_conn != null) {
			return share_conn;
		}

		share_conn = getSharedConn0();
		return share_conn;
	}

	protected boolean isSharedConn(AutoCloseable handle) {
		if (genericType.isAssignableFrom(TypeToken.of(handle.getClass()))) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected T getSharedConn0() {
		List<AutoCloseable> tlocal_conn = s_conn_threadlocal.get();
		for (AutoCloseable c : tlocal_conn) {
			if (isSharedConn(c)) {
				return (T) c;
			}
		}
		share_conn = doConnect();
		tlocal_conn.add(share_conn);
		return share_conn;
	}

	// static class DynamicProxyConn extends AbstractInvocationHandler
	// {
	// Object obj;
	//
	// @SuppressWarnings("unchecked")
	// public <T extends AutoCloseable> T getConn()
	// {
	// return (T)obj;
	// }
	// public DynamicProxyConn(Object obj) {
	// this.obj = obj;
	// Preconditions.checkNotNull(obj);
	// }
	// @Override
	// protected Object handleInvocation(Object proxy, Method method,
	// Object[] args) throws Throwable {
	// System.err.println("invoke: " + method);
	// if(method.getName().equals("close") && s_conn_share_mode.get())
	// {
	// System.err.println("skip: " + method);
	// return null;
	// }
	// System.err.println("do: " + method);
	// return method.invoke(obj, args);
	// }
	// }

}
