package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * 如果出现嵌套的Runnable,最终这个StorageSaveRunnable会在最后的StorageSaveRunnable中一起存储处理
 * 
 * 
 * 
 * @author caijiacheng
 * 
 */

public abstract class StorageSaveRunnable implements Runnable {

	static Logger LOG = LoggerFactory.getLogger(StorageSaveRunnable.class);

	static ThreadLocal<Integer> TL_IN_RUNNABLE = new ThreadLocal<Integer>() {
		protected Integer initialValue() {
			return 0;
		};
	};
	static ThreadLocal<Map<String, IStorage>> TL_INS_TO_SAVE = new ThreadLocal<Map<String, IStorage>>() {
		protected Map<String, IStorage> initialValue() {
			return Maps.newHashMap();
		}
	};

	static ThreadLocal<Stack<StorageSaveRunnable>> TL_RUNNABLE_TO_SAVE = new ThreadLocal<Stack<StorageSaveRunnable>>() {
		protected Stack<StorageSaveRunnable> initialValue() {
			return new Stack<>();
		};
	};

	static ThreadLocal<Throwable> TL_THROWABLE = new ThreadLocal<>();

	static public void add2Save(IStorage ins) {
		Preconditions.checkArgument(TL_IN_RUNNABLE.get() > 0,
				"Must use in StorageSaveRunnable");

		String key = new String(ins.marshalKey(), StandardCharsets.UTF_8);
		// if (TL_IN_RUNNABLE.get() > 0)
		if (!TL_INS_TO_SAVE.get().containsKey(key)) {
			//LOG.debug("add2Save():{}, ins:{}", key, ins);
			TL_INS_TO_SAVE.get().put(key, ins);
		}
	}

	@SuppressWarnings("unchecked")
	static public <T extends IStorage> Optional<T> loadFromTransiction(
			byte[] key) {

		if (TL_IN_RUNNABLE.get() > 0) {
			return Optional.fromNullable((T) TL_INS_TO_SAVE.get().get(
					new String(key, StandardCharsets.UTF_8)));
		} else {
			return Optional.absent();
		}
	}

	@Override
	public void run() {
		TL_IN_RUNNABLE.set(TL_IN_RUNNABLE.get() + 1);
		TL_RUNNABLE_TO_SAVE.get().add(this);
		try {
			ret = onSaveTransactionRun();

		} catch (Throwable e) {
			// onException(e);
			if (TL_THROWABLE.get() == null)
				TL_THROWABLE.set(e);

		} finally {
			TL_IN_RUNNABLE.set(TL_IN_RUNNABLE.get() - 1);
			if (TL_IN_RUNNABLE.get() == 0) {

				if (TL_THROWABLE.get() == null) {
					try {
						Ins.getStorageService().saveCollection(
								TL_INS_TO_SAVE.get().values());
						while (TL_RUNNABLE_TO_SAVE.get().size() != 0) {
							try {
								TL_RUNNABLE_TO_SAVE.get().pop().onSuccess();
							} catch (Throwable e) {
								LOG.error("onSuccess Exception:{}", e);
							}
						}
					} catch (Throwable e) {
						TL_THROWABLE.set(e);
					}
				}

				if (TL_THROWABLE.get() != null) {
					while (TL_RUNNABLE_TO_SAVE.get().size() != 0) {
						try {
							TL_RUNNABLE_TO_SAVE.get().pop()
									.onException(TL_THROWABLE.get());
						} catch (Throwable e) {
						}
					}
				}

				TL_THROWABLE.remove();
				TL_INS_TO_SAVE.remove();
			} else {
				if (TL_THROWABLE.get() != null) {
					throw new RuntimeException("Break Nest Runnable");
				}
			}
		}
	}

	Object ret;

	abstract protected Object onSaveTransactionRun();

	protected void onSuccess() {

	}

	void onException(Throwable e) {
		LOG.error("onSaveRunnable Exception: {}", e);
	}

	public Object getResult() {
		return ret;
	}

}
