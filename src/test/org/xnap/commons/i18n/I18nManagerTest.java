package org.xnap.commons.i18n;

import java.util.Locale;
import junit.framework.TestCase;
import org.xnap.commons.i18n.testpackage.MockResourceBundle;

public class I18nManagerTest extends TestCase 
{
	private Locale savedDefault;
	
	protected void setUp() throws Exception 
	{
		savedDefault = Locale.getDefault();
		Locale.setDefault(Locale.GERMAN);

		Locale.setDefault(Locale.GERMAN);
	}
	
	protected void tearDown() throws Exception 
	{
		I18nFactory.clearCache();
		
		Locale.setDefault(savedDefault);
	}
	
	public void testSetLocale()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
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
