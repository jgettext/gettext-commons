/*
 *  Gettext Commons
 *
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

import java.util.Locale;
import junit.framework.TestCase;
import org.xnap.commons.i18n.testpackage.MockResourceBundle;

/**
 * @author Felix Berger
 */
public class I18nManagerTest extends TestCase 
{
	private Locale savedDefault;
	
	protected void setUp() throws Exception 
	{
		I18nFactory.clearCache();
		
		savedDefault = Locale.getDefault();
		Locale.setDefault(Locale.GERMAN);
	}
	
	protected void tearDown() throws Exception 
	{
		Locale.setDefault(savedDefault);
	}
	
	public void testSetLocale()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		assertEquals(Locale.GERMAN, i18n.getResources().getLocale());
		I18nManager.getInstance().setDefaultLocale(Locale.FRENCH);
		assertEquals(Locale.FRENCH, i18n.getResources().getLocale());
		I18nManager.getInstance().setDefaultLocale(Locale.GERMAN);
		assertEquals(Locale.GERMAN, i18n.getResources().getLocale());
	}
	
	public void testListener()
	{
		MyLocaleChangeListener listener = new MyLocaleChangeListener();
		I18nManager.getInstance().addLocaleChangeListener(listener );
		assertEquals(0, listener.count);
		I18nManager.getInstance().setDefaultLocale(Locale.GERMAN);
		assertEquals(1, listener.count);
		assertEquals(Locale.GERMAN, listener.newLocale);
		I18nManager.getInstance().removeLocaleChangeListener(listener);
		I18nManager.getInstance().setDefaultLocale(Locale.FRENCH);
		assertEquals(1, listener.count);
		assertEquals(Locale.GERMAN, listener.newLocale);
	}

	public void testWeakListener()
	{
		int listenerCount = I18nManager.getInstance().localeChangeListeners.size();
		MyLocaleChangeListener listener = new MyLocaleChangeListener();
		I18nManager.getInstance().addWeakLocaleChangeListener(listener );
		assertEquals(0, listener.count);
		assertEquals(listenerCount + 1, I18nManager.getInstance().localeChangeListeners.size());
		I18nManager.getInstance().setDefaultLocale(Locale.GERMAN);
		assertEquals(1, listener.count);
		listener = null;
		System.gc();
		I18nManager.getInstance().setDefaultLocale(Locale.FRENCH);
		assertEquals(listenerCount, I18nManager.getInstance().localeChangeListeners.size());
	}

	private class MyLocaleChangeListener implements LocaleChangeListener {
		int count;
		Locale newLocale;
		public void localeChanged(LocaleChangeEvent event)
		{
			count++;
			newLocale = event.getNewLocale();
		}			
	};

}
