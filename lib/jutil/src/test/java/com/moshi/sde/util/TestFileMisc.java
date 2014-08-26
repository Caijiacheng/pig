package com.moshi.sde.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mm.sde.util.FileMisc;


@RunWith(Parameterized.class)
public class TestFileMisc {

    @Parameters
    public static Collection<?> data()
    {
        return Arrays.asList(new Object[][]{ {"gb2312"}, {"gbk"}, {"utf-8"}, {"utf-16"} });
    }
	
    private String _charset;
    
    public TestFileMisc(String charset)
    {
    	_charset = charset;
    }
    
	@Test
	public void testFileWriteRead() throws IOException
	{
		
		String s[] = new String[]{"这是什么", "hello", "24e2fdsfdf"};
		
		String f = "filemisc.txt";
		Charset charset = Charset.forName(_charset);
		
		FileMisc.dumpToFile(new File(f), charset, s);
		
		Collection<String> ss = FileMisc.getFileContent(new File(f), charset);
		
		String[] oss = ss.toArray(new String[ss.size()]);
		
		for (String is : oss)
		{
			System.out.println(is);
		}
		
		for (String is : s)
		{
			System.out.println(is);
		}
		Assert.assertTrue(Arrays.equals(s, oss));
		
	}
}
