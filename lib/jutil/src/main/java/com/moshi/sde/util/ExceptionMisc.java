package com.moshi.sde.util;

public class ExceptionMisc {
	
	public static String str(Throwable e){
		StringBuffer sb = new StringBuffer(256);
		sb.append(getMessage(e));
		
		for ( StackTraceElement a : e.getStackTrace() ){
			 sb.append("	at ");
			 sb.append(a.toString());
			 sb.append("\n");
		 }
		return sb.toString();
	}
	
	public static String getMessage(Throwable e){
		StringBuffer sb = new StringBuffer(256);
		sb.append(e.getMessage());
		sb.append("\n");
		return sb.toString();
	}
}
