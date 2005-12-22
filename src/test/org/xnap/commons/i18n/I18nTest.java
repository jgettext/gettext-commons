/*
 *  XNap Commons
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.xnap.commons.i18n;
import java.util.Locale;

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
		// WARNING: i18n instances are cached in the factory, therefore test 
		// cases may not alter their state
		i18nDE = new I18n(BASENAME, Locale.GERMAN, getClass().getClassLoader());
		i18nEN = new I18n(BASENAME, Locale.ENGLISH, getClass().getClassLoader());
	}
	
	protected void tearDown() throws Exception 
	{
	}
	
	public void testTr()
	{
		assertEquals("Foo", i18nDE.tr("Foo"));
		assertEquals("Bar", i18nDE.tr("Bar"));
		assertEquals("Automatisch", i18nDE.tr("Automatic"));
		assertEquals("Erg\u00e4nzung", i18nDE.tr("Completion"));
	}
	
	public void testTr1()
	{
		assertEquals("Foo foo ", i18nEN.tr("Foo {0} ", "foo"));
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
		assertEquals("Foo", i18nEN.trc("Foo (context)", "Foo"));
		assertEquals("KontextFoo", i18nDE.trc("Foo (context)", "Foo"));
	}
	
	public void testSetLocale()
	{
		I18n i18n = new I18n(new MockResourceBundle());
		assertFalse(i18n.setLocale(Locale.FRENCH));
		i18n.setResources(MockResourceBundle.class.getName(), 
				Locale.GERMAN, MockResourceBundle.class.getClassLoader());
		assertTrue(i18n.setLocale(Locale.FRENCH));
	}
	
	public void testSetResources()
	{
		try {
			new I18n(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {
		}
		try {
			I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
			i18n.setResources(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {
		}
		I18n i18n = I18nFactory.getI18n(MockResourceBundle.class);
		String baseName = MockResourceBundle.class.getName();
		i18n.setResources(baseName, Locale.GERMAN, MockResourceBundle.class.getClassLoader());
		assertEquals(MockResourceBundle.class, i18n.getResources().getClass());
		try {
			i18n.setResources(null, Locale.GERMAN, MockResourceBundle.class.getClassLoader());
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {
		}
		try {
			i18n.setResources(baseName, null, MockResourceBundle.class.getClassLoader());
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {
		}
		try {
			i18n.setResources(baseName, Locale.GERMAN, null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException npe) {
		}
	}
	
	public void testSetSourceCodeLocale()
	{
		i18nDE.setSourceCodeLocale(Locale.GERMAN);
		assertEquals("bar", i18nDE.trc("foo (verb)", "bar"));
		i18nDE.setSourceCodeLocale(Locale.ENGLISH);
		assertEquals("foo (verb)", i18nDE.trc("foo (verb)", "bar"));
		
		try {
			i18nDE.setSourceCodeLocale(null);
			fail("null pointer exception expected");
		}
		catch (NullPointerException npe) {
		}
	}
	
	public void testTrnEN()
	{
		assertEquals("Foo", i18nEN.trn("Foo", "Bar", 1)); 
		assertEquals("Bar", i18nEN.trn("Foo", "Bar", 2));
		assertEquals("2 Bars", i18nEN.trn("Foo", "{0} Bars", 2, new Integer(2)));
	}
	
	public void testTrnDE()
	{
		assertEquals("Datei", i18nDE.trn("File", "{0} Files", 1, new Integer(1)));		
		assertEquals("2 Dateien", i18nDE.trn("File", "{0} Files", 2, new Integer(2)));		
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

}
