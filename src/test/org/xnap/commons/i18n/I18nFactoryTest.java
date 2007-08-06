package org.xnap.commons.i18n;

import java.util.List;
import java.util.Locale;
import junit.framework.TestCase;
import org.xnap.commons.i18n.testpackage.MockResourceBundle;
import org.xnap.commons.i18n.testpackage.noresources.HasNoOwnResources;
import org.xnap.commons.i18n.testpackage.resources.HasItsOwnResources;

public class I18nFactoryTest extends TestCase 
{

	private static Locale[] LOCALES = { Locale.GERMAN, Locale.FRENCH };
	private static String[] VALUES = { "Wert", "valeur" };
	private Locale savedDefault;
	private String packageName;
	
	protected void setUp() throws Exception 
	{
		packageName = MockResourceBundle.class.getName();
		packageName = packageName.substring(0, packageName.lastIndexOf('.'));
		
		savedDefault = Locale.getDefault();
		Locale.setDefault(Locale.GERMAN);
		
		I18nFactory.clearCache();
	}
	
	protected void tearDown() throws Exception 
	{
		Locale.setDefault(savedDefault); 
	}
	
	public void testFindByBaseName()
	{
		for (int i = 0; i < LOCALES.length; i++) {
			Locale.setDefault(LOCALES[i]);
			I18n i18n = I18nFactory.findByBaseName("TestMessages", packageName, 
					Locale.getDefault(), MockResourceBundle.class.getClassLoader());
			assertEquals(LOCALES[i], i18n.getResources().getLocale());
			assertEquals(VALUES[i], i18n.getResources().getString("value"));
			assertEquals(VALUES[i], i18n.tr("value"));
		}
		
		// same for mock up
		I18n i18n = I18nFactory.findByBaseName("MockResourceBundle", 
				packageName, Locale.getDefault(), getClass().getClassLoader());
		assertEquals(MockResourceBundle.class, i18n.getResources().getClass());
		assertEquals("value", i18n.getResources().getString("value"));
		assertEquals("value", i18n.tr("value"));
	}
	
	public void testReadFromPropertiesFile()
	{
		Locale.setDefault(Locale.GERMAN);
		I18n i18n = I18nFactory.readFromPropertiesFile(packageName, 
				Locale.getDefault(), getClass().getClassLoader());
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

	public void testGetI18nFallback()
	{
	    I18n i18n = I18nFactory.getI18n(HasNoOwnResources.class, "NonExistant", Locale.getDefault(), I18nFactory.FALLBACK);
	    assertTrue(i18n.getResources() instanceof EmptyResourceBundle);
	}

}
