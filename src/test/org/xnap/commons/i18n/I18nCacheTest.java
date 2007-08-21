package org.xnap.commons.i18n;

import java.util.Locale;

import org.xnap.commons.i18n.testpackage.MockResourceBundle;

import junit.framework.TestCase;

/**
 * @author Steffen Pingel
 */
public class I18nCacheTest extends TestCase {
	
	public void testGet() {
		I18nCache cache = new I18nCache();
		I18n i18n = new I18n(new MockResourceBundle());
		cache.put("foo", i18n);
		assertNull(cache.get("foo", Locale.getDefault()));
		try {
			cache.get("foo", null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
		i18n.setLocale(Locale.ENGLISH);
		assertSame(i18n, cache.get("foo", Locale.ENGLISH));
	}

	public void testClear() {
		I18nCache cache = new I18nCache();
		I18n i18n = new I18n(new MockResourceBundle());
		i18n.setLocale(Locale.ENGLISH);
		cache.put("foo", i18n);
		assertNotNull(cache.get("foo", Locale.ENGLISH));
		cache.clear();
		assertNull(cache.get("foo", Locale.ENGLISH));
	}

	public void testVisit() {
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
