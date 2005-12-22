package org.xnap.commons.i18n.testpackage;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class MockResourceBundle extends ResourceBundle 
{
	public MockResourceBundle()
	{
	}
	
	protected Object handleGetObject(String key)
	{
		return "value";
	}

	public Enumeration getKeys() 
	{
		return new Enumeration() {
			boolean gotten = false;

			public boolean hasMoreElements() 
			{
				return !gotten;
			}

			public Object nextElement() 
			{
				gotten = true;
				return "value";
			}
		};
	}

}
