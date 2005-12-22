package org.xnap.commons.i18n;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.xnap.commons.i18n.testpackage.MockResourceBundle;
import org.xnap.commons.i18n.testpackage.noresources.HasNoOwnResources;
import org.xnap.commons.i18n.testpackage.resources.HasItsOwnResources;

public class I18nFactoryTest extends TestCase 
{
	private String packageName;
	
	private static Locale[] LOCALES = { Locale.GERMAN, Locale.FRENCH };
	private static String[] VALUES = { "Wert", "valeur" };
	
	private Locale savedDefault;
	
	private HashMap cachedMap;
	
	protected void setUp() throws Exception 
	{
		packageName = MockResourceBundle.class.getName();
		packageName = packageName.substring(0, packageName.lastIndexOf('.'));
		savedDefault = Locale.getDefault();
		Locale.setDefault(Locale.GERMAN);
	
		// TODO change visibility of i18nPackage field to default
		// we cache and use a new clean map for I18nFactory.i18nByPackage
		Field field = I18nFactory.class.getDeclaredField("i18nByPackage");
		field.setAccessible(true);
		cachedMap = (HashMap)field.get(null);
		field.set(null, new HashMap());
	}
	
	protected void tearDown() throws Exception 
	{
		Field field = I18nFactory.class.getDeclaredField("i18nByPackage");
		field.setAccessible(true);
		field.set(null, cachedMap);
		Locale.setDefault(savedDefault); 
	}
	
	public void testFindByBaseName()
	{
		for (int i = 0; i < LOCALES.length; i++) {
			Locale.setDefault(LOCALES[i]);
			I18n i18n = I18nFactory.findByBaseName("TestMessages", packageName, 
					MockResourceBundle.class.getClassLoader());
			assertEquals(LOCALES[i], i18n.getResources().getLocale());
			assertEquals(VALUES[i], i18n.getResources().getString("value"));
			assertEquals(VALUES[i], i18n.tr("value"));
		}
		
		// same for mock up
		I18n i18n = I18nFactory.findByBaseName("MockResourceBundle", 
				packageName, getClass().getClassLoader());
		assertEquals(MockResourceBundle.class, i18n.getResources().getClass());
		assertEquals("value", i18n.getResources().getString("value"));
		assertEquals("value", i18n.tr("value"));
	}
	
	public void testReadFromPropertiesFile()
	{
		Locale.setDefault(Locale.GERMAN);
		I18n i18n = I18nFactory.readFromPropertiesFile(packageName, 
				getClass().getClassLoader());
		assertNotNull(i18n);
		assertEquals("Wert", i18n.getResources().getString("value"));
		assertEquals("Wert", i18n.tr("value"));
	}
	
	public void testGetI18n()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		assertNotSame(MockResourceBundle.class, i18n.getResources().getClass());
		i18n = I18nFactory.getI18n(MockResourceBundle.class, "MockResourceBundle");
		// base name is ignored, since we found properties
		assertNotSame(MockResourceBundle.class, i18n.getResources().getClass());
	}
	
	public void testSetLocale()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		I18nFactory.setLocale(Locale.FRENCH);
		assertEquals(Locale.FRENCH, i18n.getResources().getLocale());
		I18nFactory.setLocale(Locale.GERMAN);
		assertEquals(Locale.GERMAN, i18n.getResources().getLocale());
	}
	
	public void testOwnResources()
	{
		I18n i18n = I18nFactory.getI18n(HasItsOwnResources.class);
		i18n.setLocale(Locale.GERMAN);
		assertEquals("yes", i18n.tr("own"));
	}
	
	public void testNoOwnResources()
	{
		I18n i18n = I18nFactory.getI18n(HasNoOwnResources.class);
		i18n.setLocale(Locale.GERMAN);
		assertEquals(Locale.GERMAN, i18n.getResources().getLocale());
		assertEquals("Wert", i18n.tr("value"));
		i18n.setLocale(Locale.FRENCH);
		assertEquals(Locale.FRENCH, i18n.getResources().getLocale());
		assertEquals("valeur", i18n.tr("value"));
	}
	
	public void testIsInDefaultPackage()
	{
		assertFalse(I18nFactory.isInDefaultPackage(I18nFactory.class));
		assertFalse(I18nFactory.isInDefaultPackage(List.class));
	}
	
	public void testListener()
	{
		MyLocaleChangeListener listener = new MyLocaleChangeListener();
		I18nFactory.addLocaleChangeListener(listener );
		assertEquals(0, listener.count);
		I18nFactory.setLocale(Locale.GERMAN);
		assertEquals(1, listener.count);
		assertEquals(Locale.GERMAN, listener.newLocale);
		I18nFactory.removeLocaleChangeListener(listener);
		I18nFactory.setLocale(Locale.FRENCH);
		assertEquals(1, listener.count);
		assertEquals(Locale.GERMAN, listener.newLocale);
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
