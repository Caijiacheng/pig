package org.restexpress.postprocessor;

import java.util.Date;

import io.netty.handler.codec.http.HttpHeaders;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.pipeline.Postprocessor;

import com.strategicgains.util.date.DateAdapter;
import com.strategicgains.util.date.HttpHeaderTimestampAdapter;
import com.strategicgains.util.date.TimestampAdapter;

/**
 * Assigns the Last-Modified HTTP header on the response for GET responses, if applicable.
 * 
 * @author toddf
 * @since May 15, 2012
 */
public class LastModifiedHeaderPostprocessor
implements Postprocessor
{
	DateAdapter fmt = new HttpHeaderTimestampAdapter();

	@Override
	public void process(Request request, Response response)
	{
		if (!request.isMethodGet()) return;
		if (!response.hasBody()) return;

		Object body = response.getBody();

		if (!response.hasHeader(HttpHeaders.Names.LAST_MODIFIED.toString()) && body.getClass().isAssignableFrom(Date.class))
		{
			response.addHeader(HttpHeaders.Names.LAST_MODIFIED.toString(), fmt.format((Date)body));
		}
	}
}
