package org.xnap.commons.i18n;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * A <code>ResourceBundle</code> that returns the key as a value.
 *
 * FIXME needs to implement proper plural handling
 * FIXME the bundle needs to have a valid locale for proper sourceCodeLocale handling
 */
class EmptyResourceBundle extends ResourceBundle
{
	/**
	 * Returns the key as value.
	 */
	protected Object handleGetObject(String key) 
	{
		return key;
	}

	public Enumeration getKeys() 
	{
		return new EmptyStringEnumeration();
	}
	
	private static class EmptyStringEnumeration implements Enumeration
	{

		public boolean hasMoreElements() 
		{
			return false;
		}

		public Object nextElement() 
		{
			throw new IllegalStateException("nextElement must not be " +
					"called on empty enumeration");
		}
		
	}
	
}