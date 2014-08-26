/*
    Copyright 2014, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.restexpress.query;

import java.util.Arrays;
import java.util.List;

import org.restexpress.Request;
import org.restexpress.common.query.Expansion;
import org.restexpress.common.util.StringUtils;
import org.restexpress.exception.BadRequestException;

/**
 * @author toddf
 * @since Jun 16, 2014
 */
public abstract class Expansions
{
	private static final String EXPAND_HEADER_NAME = "expand";
	private static final String EXPAND_SEPARATOR = ",\\s*";
//	private static final String EXPAND_SEGMENT_SEPARATOR = "\\.";

	public static Expansion parseFrom(Request request)
    {
	    return parseFrom(request, (List<String>) null);
    }

	public static Expansion parseFrom(Request request, String... allowedProperties)
	{
		return parseFrom(request, Arrays.asList(allowedProperties));
	}

	public static Expansion parseFrom(Request request, List<String> allowedProperties)
    {
		String expandString = request.getHeader(EXPAND_HEADER_NAME);

		if (expandString == null || expandString.trim().isEmpty())
		{
			return new Expansion();
		}

		String[] expansions = expandString.split(EXPAND_SEPARATOR);

		for (String expand : expansions)
		{
			enforceSupportedProperties(allowedProperties, expand);
		}

		return new Expansion(Arrays.asList(expansions));
    }

	private static void enforceSupportedProperties(List<String> allowedProperties, String requested)
    {
	    if (allowedProperties != null && !allowedProperties.contains(requested))
	    {
	    	throw new BadRequestException(requested + " is not a supported expansion. Supported expansion names are: "
	    		+ StringUtils.join(", ", allowedProperties));
	    }
    }
}
