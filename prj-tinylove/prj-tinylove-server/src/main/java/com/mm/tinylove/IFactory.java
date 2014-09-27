package com.mm.tinylove;

import java.util.Iterator;

public interface IFactory<T> {
	T create();
	Iterator<T> iter();
}
