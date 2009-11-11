/*
 *  Gettext Commons
 *
 *  Copyright (C) 2005  Felix Berger
 *  Copyright (C) 2005  Steffen Pingel
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.xnap.commons.i18n;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A <code>ResourceBundle</code> that returns the key as a value.
 *
 * FIXME needs to implement proper plural handling
 * FIXME the bundle needs to have a valid locale for proper sourceCodeLocale handling
 */
class EmptyResourceBundle extends ResourceBundle
{
	private Locale locale;

	public EmptyResourceBundle(Locale locale)
	{
		this.locale = locale;
	}

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
	
	public Locale getLocale()
	{
		return locale;
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