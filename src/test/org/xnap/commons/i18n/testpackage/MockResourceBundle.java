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
