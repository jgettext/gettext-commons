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
 * @author Steffen Pingel
 */
public class I18nCacheTest extends TestCase {

	public void testGet()
	{
		I18nCache cache = new I18nCache();
		I18n i18n = new I18n(new MockResourceBundle());
		cache.put("foo", i18n);
		assertNull(cache.get("foo", Locale.getDefault()));
		try {
			cache.get("foo", null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException expected) {}
		i18n.setLocale(Locale.ENGLISH);
		assertSame(i18n, cache.get("foo", Locale.ENGLISH));
	}

	public void testClear()
	{
		I18nCache cache = new I18nCache();
		I18n i18n = new I18n(new MockResourceBundle());
		i18n.setLocale(Locale.ENGLISH);
		cache.put("foo", i18n);
		assertNotNull(cache.get("foo", Locale.ENGLISH));
		cache.clear();
		assertNull(cache.get("foo", Locale.ENGLISH));
	}

	public void testVisit()
	{
		I18nCache cache = new I18nCache();
		final I18n i18n = new I18n(new MockResourceBundle());
		cache.put("foo", i18n);
		final int[] count = new int[1];
		cache.visit(new I18nCache.Visitor() {

			public void visit(I18n visited)
			{
				assertSame(i18n, visited);
				count[0]++;
			}
		});
		assertEquals(1, count[0]);
	}

}
