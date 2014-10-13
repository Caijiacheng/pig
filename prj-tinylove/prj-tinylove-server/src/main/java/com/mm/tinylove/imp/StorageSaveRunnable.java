package com.mm.tinylove.imp;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class StorageSaveRunnable implements Runnable {

	static Logger LOG = LoggerFactory.getLogger(StorageSaveRunnable.class);

	static ThreadLocal<Boolean> TL_IN_RUNNABLE = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return Boolean.FALSE;
		};
	};
	static ThreadLocal<Set<IStorage>> TL_INS_TO_SAVE = new ThreadLocal<Set<IStorage>>() {
		protected Set<IStorage> initialValue() {
			return Sets.newHashSet();
		}
	};

	static public void add2Save(IStorage ins) {
		// Preconditions.checkArgument(TL_IN_RUNNABLE.get(),
		// "Must use in StorageSaveRunnable");
		if (TL_IN_RUNNABLE.get()) {
			TL_INS_TO_SAVE.get().add(ins);
		}
	}

	@Override
	public void run() {
		TL_IN_RUNNABLE.set(Boolean.TRUE);
		try {
			Object obj = onSaveTransactionRun();
			Set<IStorage> ins = TL_INS_TO_SAVE.get();
			Ins.getStorageService().saveCollection(ins);
			ret = obj;
			onSuccess();
		} catch (Throwable e) {
			onException(e);
		} finally {
			TL_INS_TO_SAVE.remove();
			TL_IN_RUNNABLE.set(Boolean.FALSE);
		}
	}

	
	Object ret;
	
	abstract Object onSaveTransactionRun();

	void onSuccess() {

	}

	void onException(Throwable e) {
		LOG.error("onSaveRunnable Exception: {}", e);
		throw new RuntimeException(e);
	}
	
	public Object getResult()
	{
		return ret;
	}

}
