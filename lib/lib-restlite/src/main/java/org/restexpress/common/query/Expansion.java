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
package org.restexpress.common.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author toddf
 * @since Jun 16, 2014
 */
public class Expansion
{
	private List<String> expansions;

	public Expansion()
	{
		super();
	}

	public Expansion(List<String> expansions)
	{
		this();
		this.expansions = new ArrayList<String>(expansions);
	}

	public Expansion addExpansion(String expansion)
	{
		expansions.add(expansion);
		return this;
	}

	public boolean isEmpty()
	{
		return (expansions == null || expansions.isEmpty());
	}

	public boolean contains(String value)
	{
		return expansions.contains(value);
	}

	public void iterate(ExpansionCallback callback)
	{
		if (callback == null || isEmpty()) return;

		for (String expansion : expansions)
		{
			callback.expand(expansion);
		}
	}
}
