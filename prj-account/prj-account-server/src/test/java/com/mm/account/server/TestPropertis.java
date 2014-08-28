package com.mm.account.server;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Unit test for simple App.
 */
public class TestPropertis 
{

    /**
     * Rigourous Test :-)
     */
	@Test
    public void testApp()
    {
        assertTrue( true );
    }
	
	
	Logger LOG = LoggerFactory.getLogger(TestPropertis.class);
	
	@Test
	public void testPropertis() throws FileNotFoundException, IOException
	{
		Properties prop = new Properties();
		prop.load(this.getClass().getResourceAsStream("test.propertis"));
		
		for (Entry<Object, Object> obj : prop.entrySet())
		{
			LOG.error("key:{}, value:{}", obj.getKey(), obj.getValue());
		}
		
	}
}
