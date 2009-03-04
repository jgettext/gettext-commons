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
import java.util.MissingResourceException;

import junit.framework.TestCase;

import org.xnap.commons.i18n.testpackage.MockResourceBundle;

/**
 * @author Steffen Pingel
 * @author Felix Berger
 */
public class I18nTest extends TestCase {

	public static final String BASENAME = "org.xnap.commons.i18n.Messages";

	private I18n i18nDE;

	private I18n i18nEN;

	protected void setUp() throws Exception
	{
		try {
			i18nDE = new I18n(BASENAME, Locale.GERMAN, getClass().getClassLoader());
		}
		catch (MissingResourceException e) {
			throw new RuntimeException(
					"Please make sure you run 'mvn org.xnap.commons:maven-gettext-plugin:dist' before executing tests");
		}
		i18nEN = new I18n(BASENAME, Locale.ENGLISH, getClass().getClassLoader());
	}

	protected void tearDown() throws Exception
	{
	}

	public void testTr()
	{
		assertEquals("Haus", i18nDE.tr("house"));
		assertEquals("Maus", i18nDE.tr("mouse"));
		assertEquals("Automatisch", i18nDE.tr("Automatic"));
		assertEquals("Erg\u00e4nzung", i18nDE.tr("Completion"));
	}

	public void testTr1()
	{
		assertEquals("House Nr. 2 ", i18nEN.tr("House Nr. {0} ", new Integer(2)));
		assertEquals("0", i18nEN.tr("{0}", "0"));
	}

	public void testTr2()
	{
		assertEquals("Foo bar foo", i18nEN.tr("Foo {1} {0}", "foo", "bar"));
		assertEquals("Foo foo bar", i18nEN.tr("Foo {0} {1}", "foo", "bar"));
	}

	public void testTr3()
	{
		assertEquals("Foo bar baz foo", i18nEN.tr("Foo {1} {2} {0}", "foo", "bar", "baz"));
		assertEquals("Foo foo bar baz", i18nEN.tr("Foo {0} {1} {2}", "foo", "bar", "baz"));
	}

	public void testTr4()
	{
		assertEquals("Foo bar baz boing foo", i18nEN.tr("Foo {1} {2} {3} {0}", "foo", "bar", "baz", "boing"));
		assertEquals("Foo foo bar baz boing", i18nEN.tr("Foo {0} {1} {2} {3}", "foo", "bar", "baz", "boing"));
	}

	public void testMarktr()
	{
		assertEquals(I18n.marktr("Foo"), "Foo");
	}

	public void testTrc()
	{
		assertEquals("chat", i18nEN.trc("noun", "chat"));
		assertEquals("chat", i18nEN.trc("verb", "chat"));
		assertEquals("Chat", i18nDE.trc("noun", "chat"));
		assertEquals("Chatten", i18nDE.trc("verb", "chat"));
	}

	public void testSetLocale()
	{
		I18n i18n = new I18n(new MockResourceBundle());
		assertFalse(i18n.setLocale(Locale.FRENCH));
		i18n.setResources(MockResourceBundle.class.getName(), Locale.GERMAN, MockResourceBundle.class.getClassLoader());
		assertTrue(i18n.setLocale(Locale.FRENCH));
	}

	public void testSetResources()
	{
		try {
			new I18n(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {}
		String baseName = MockResourceBundle.class.getName();
		try {
			i18nDE.setResources(null, Locale.GERMAN, MockResourceBundle.class.getClassLoader());
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {}
		try {
			i18nDE.setResources(baseName, null, MockResourceBundle.class.getClassLoader());
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {}
		try {
			i18nDE.setResources(baseName, Locale.GERMAN, null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {}
	}

	public void testSetSourceCodeLocale()
	{
		i18nDE.setSourceCodeLocale(Locale.GERMAN);
		assertEquals("chat", i18nDE.trc("verb", "chat"));
		i18nDE.setSourceCodeLocale(Locale.ENGLISH);
		assertEquals("Chatten", i18nDE.trc("verb", "chat"));

		try {
			i18nDE.setSourceCodeLocale(null);
			fail("null pointer exception expected");
		}
		catch (NullPointerException npe) {}
	}

	public void testTrnEN()
	{
		assertEquals("Foo", i18nEN.trn("Foo", "{0} Bars", 1));
		assertEquals("{0} Bars", i18nEN.trn("Foo", "{0} Bars", 2));
		assertEquals("2 Bars", i18nEN.trn("Foo", "{0} Bars", 2, new Integer(2)));
	}

	public void testTrnDE()
	{
		assertEquals("Datei", i18nDE.trn("File", "{0} Files", 1, new Integer(1)));
		assertEquals("2 Dateien", i18nDE.trn("File", "{0} Files", 2, new Integer(2)));
	}

	public void testTrn1()
	{
		assertEquals("Foo foo ", i18nEN.trn("Foo {0} ", "Foos {0}", 1, "foo"));
	}

	public void testTrn2()
	{
		assertEquals("Foo bar foo", i18nEN.trn("Foo {1} {0}", "Foos", 1, "foo", "bar"));
		assertEquals("Foo foo bar", i18nEN.trn("Foo {0} {1}", "Foos", 1, "foo", "bar"));
	}

	public void testTrn3()
	{
		assertEquals("Foo bar baz foo", i18nEN.trn("Foo {1} {2} {0}", "Foos", 1, "foo", "bar", "baz"));
		assertEquals("Foo foo bar baz", i18nEN.trn("Foo {0} {1} {2}", "Foos", 1, "foo", "bar", "baz"));
	}

	public void testTrn4()
	{
		assertEquals("Foo bar baz boing foo", i18nEN
				.trn("Foo {1} {2} {3} {0}", "Foos", 1, "foo", "bar", "baz", "boing"));
		assertEquals("Foo foo bar baz boing", i18nEN
				.trn("Foo {0} {1} {2} {3}", "Foos", 1, "foo", "bar", "baz", "boing"));
	}

	public void testSetEmptyResources()
	{
		// this should load the empty resource bundle
		// we have to set the default to italian too, so
		// ResourceBundle.getBundle(...) doesn't fall back on the default locale
		// which might be there
		Locale.setDefault(Locale.ITALIAN);
		assertTrue(i18nDE.setLocale(Locale.ITALIAN));
		assertEquals("value", i18nDE.tr("value"));
	}

	public void testTrcReturnsTextWhenTranslationNotFound() {
		assertEquals("baobab", i18nDE.trc("dont translate to German", "baobab"));
	}
}
