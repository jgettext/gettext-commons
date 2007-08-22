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
import org.xnap.commons.i18n.testpackage.noresources.HasNoOwnResources;
import org.xnap.commons.i18n.testpackage.resources.HasItsOwnResources;

/**
 * @author Felix Berger
 */
public class I18nFactoryTest extends TestCase {

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
			I18n i18n = I18nFactory.findByBaseName(packageName + ".TestMessages", Locale.getDefault(),
					MockResourceBundle.class.getClassLoader(), I18nFactory.DEFAULT);
			assertEquals(LOCALES[i], i18n.getResources().getLocale());
			assertEquals(VALUES[i], i18n.getResources().getString("value"));
			assertEquals(VALUES[i], i18n.tr("value"));
		}

		// same for mock up
		I18n i18n = I18nFactory.findByBaseName(packageName + ".MockResourceBundle", Locale.getDefault(), getClass()
				.getClassLoader(), I18nFactory.DEFAULT);
		assertEquals(MockResourceBundle.class, i18n.getResources().getClass());
		assertEquals("value", i18n.getResources().getString("value"));
		assertEquals("value", i18n.tr("value"));
	}

	public void testReadFromPropertiesFile()
	{
		String baseName = I18nFactory.readFromPropertiesFile(packageName, Locale.getDefault(), getClass()
				.getClassLoader());
		assertEquals("org.xnap.commons.i18n.testpackage.TestMessages", baseName);
	}

	public void testGetI18n()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		assertNotSame(MockResourceBundle.class, i18n.getResources().getClass());
		i18n = I18nFactory.getI18n(MockResourceBundle.class, "MockResourceBundle");
		assertSame(MockResourceBundle.class, i18n.getResources().getClass());
	}

	public void testGetI18nFromDefaultPackage()
	{
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class, "DefaultMessages", Locale.ENGLISH);
		assertEquals("DefaultBundle", i18n.tr("source"));
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

	public void testGetI18nFallback()
	{
		I18n i18n = I18nFactory.getI18n(HasNoOwnResources.class, "NonExistant", Locale.getDefault(),
				I18nFactory.FALLBACK);
		assertTrue(i18n.getResources() instanceof EmptyResourceBundle);
	}

	public void testSetResources()
	{
		try {
			I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
			i18n.setResources(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {}
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		String baseName = MockResourceBundle.class.getName();
		i18n.setResources(baseName, Locale.GERMAN, MockResourceBundle.class.getClassLoader());
		assertEquals(MockResourceBundle.class, i18n.getResources().getClass());
	}

}
