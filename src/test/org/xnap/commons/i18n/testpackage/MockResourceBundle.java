package org.xnap.commons.i18n.testpackage;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class MockResourceBundle extends ResourceBundle 
{
	
	private Locale locale;
	
	public MockResourceBundle()
	{
	}
	
	public Locale getLocale()
	{
		return locale;
	}
	
	public void setLocale(Locale locale)
	{
		this.locale = locale;
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
