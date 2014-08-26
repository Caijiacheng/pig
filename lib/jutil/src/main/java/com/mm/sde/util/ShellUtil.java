package com.mm.sde.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShellUtil
{

	public static Process shellProcess(String cmd)
	{
		String[] cmds = cmd.split(" ");

		ProcessBuilder builder = new ProcessBuilder(cmds);

		builder.redirectErrorStream(true);
		try
		{
			return builder.start();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}




	public static String[] getProcessOutput(Process p)
	{
		List<String> lines = new ArrayList<String>();
		InputStream in = p.getInputStream();
		if (in == null)
		{
			return null;
		}

		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(in, CharsetDefault.ins()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				lines.add(line);
			}

		}
		catch (IOException e)
		{
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
			}

			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			p.destroy();
		}

		return lines.toArray(new String[lines.size()]);
	}

	public static String[] shellCmd(String cmd)
	{

		Process p = shellProcess(cmd);

		if (p == null)
		{
			return null;
		}

		return getProcessOutput(p);
	}

}
