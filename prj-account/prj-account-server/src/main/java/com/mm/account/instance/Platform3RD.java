package com.mm.account.instance;

public enum Platform3RD {
	WEIBO, 
	QQ,
	WEIXIN;
	
	String dbKey()
	{
		switch (this) {
		case WEIBO:
				return "weibo_id";
		case QQ:
				return "qq_id";
		case WEIXIN:
				return "weixin_id";
		default:
			throw new RuntimeException();
		}
	}
	
}
