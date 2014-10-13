package com.mm.tinylove;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

import com.mm.tinylove.imp.TestDefaultUser;
import com.mm.tinylove.imp.TestLongRandSet;
import com.mm.tinylove.imp.TestLongRangeList;
import com.mm.tinylove.imp.TestMessageView;
import com.mm.tinylove.imp.TestStorageService;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestDefaultUser.class, TestLongRandSet.class,
		TestLongRangeList.class, TestMessageView.class,
		TestStorageService.class })
public class TinyTestSuite {

}
