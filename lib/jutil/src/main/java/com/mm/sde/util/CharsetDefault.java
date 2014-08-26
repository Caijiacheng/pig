package com.mm.sde.util;

import java.nio.charset.Charset;

final public class CharsetDefault
{
	static final String CHARSET_NAME = "utf-8";
	public static String name()
	{
		return CHARSET_NAME;
	}
	
	public static Charset ins() 
	{
		return Charset.forName(CHARSET_NAME);
	}
	
	
}
