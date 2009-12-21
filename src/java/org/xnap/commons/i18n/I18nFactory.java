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
package org.xnap.commons.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * Factory class that creates and caches I18n instances.
 * <p>
 * Given a {@link Class} object the factory looks up the resource bundle
 * responsible for handling message translations. The bundle is returned with an
 * {@link I18n} object wrapped around it, which provides the translation
 * methods. The lookup is described at {@link #getI18n(Class,String)}.
 * <p>
 * Use the factory for creating <code>I18n</code> objects to make sure no
 * extraneous objects are created.
 * 
 * @author Felix Berger
 * @author Tammo van Lessen
 * @author Steffen Pingel
 * @since 0.9
 */
public class I18nFactory {

	private static final String BASENAME_KEY = "basename";
	
	/**
	 * Use the default configuration.
	 * 
	 * @since 0.9.1
	 */
	public static final int DEFAULT = 0;
	/**
	 * Fall back to a default resource bundle that returns the passed text if no
	 * resource bundle can be located.
	 * 
	 * @since 0.9.1
	 */
	public static final int FALLBACK = 1 << 0;
	/**
	 * Look for files named {@link #PROPS_FILENAME} to determine the basename.
	 * 
	 * @since 0.9.1
	 */
	public static final int READ_PROPERTIES = 2 << 0;
	/**
	 * Do not cache {@link I18n} instance.
	 * 
	 * @since 0.9.1
	 */
	public static final int NO_CACHE = 4 << 0;
	
	/**
	 * Default name for Message bundles, is "i18n.Messages".
	 * 
	 * @since 0.9.1
	 */
	public static final String DEFAULT_BASE_NAME = "i18n.Messages";
	
	/**
	 * Filename of the properties file that contains the i18n properties, is
	 * "i18n.properties".
	 * 
	 * @since 0.9
	 */
	public static final String PROPS_FILENAME = "i18n.properties";
	
	private static final I18nCache i18nCache = new I18nCache();

	private I18nFactory()
	{
	}

	/**
	 * Clears the cache of i18n objects. Used by the test classes.
	 */
	static void clearCache()
	{
		i18nCache.visit(new I18nCache.Visitor() {

			public void visit(I18n i18n)
			{
				I18nManager.getInstance().remove(i18n);
			}
		});
		i18nCache.clear();
	}

	/**
	 * Calls {@link #getI18n(Class, Locale) getI18n(clazz, Locale.getDefault())}.
	 */
	public static I18n getI18n(final Class clazz)
	{
		return getI18n(clazz, Locale.getDefault());
	}

	/**
	 * Calls {@link #getI18n(Class, Locale, int) getI18n(clazz, locale,
	 * READ_PROPERTIES)}.
	 * 
	 * @since 0.9.1
	 */
	public static I18n getI18n(final Class clazz, final Locale locale)
	{
		return getI18n(clazz, locale, READ_PROPERTIES);
	}

	/**
	 * Returns the I18n instance responsible for translating messages in the
	 * package specified by <code>clazz</code>.
	 * <p>
	 * Lookup works by iterating upwards in the package hierarchy: First the
	 * internal cache is asked for an I18n object for a package, otherwise the
	 * algorithm looks for an <code>i18n.properties</code> file in the
	 * package. The properties file is queried for a key named
	 * <code>basename</code> whose value should be the fully qualified
	 * resource/class name of the resource bundle, e.g
	 * <code>org.xnap.commons.i18n.Messages</code>.
	 * <p>
	 * If after the first iteration no I18n instance has been found, a second
	 * search begins by looking for resource bundles having the name
	 * <code>baseName</code>.
	 * 
	 * @param clazz
	 *            the package hierarchy of the clazz and its class loader are
	 *            used for resolving and loading the resource bundle
	 * @param locale
	 *            the locale of the underlying resource bundle
	 * @param flags
	 *            a combination of these configuration flags: {@link #FALLBACK}
	 * @return created or cached <code>I18n</code> instance
	 * @throws MissingResourceException
	 *             if no resource bundle was found
	 * @since 0.9.1
	 */
	public static I18n getI18n(final Class clazz, final Locale locale, final int flags)
	{
		ClassLoader classLoader = getClassLoader(clazz.getClassLoader());
		
		String bundleName = null;
		if (isReadPropertiesSet(flags)) {
			String path = clazz.getName();
			int index;
			do {
				index = path.lastIndexOf('.');
				path = (index != -1) ? path.substring(0, index) : "";
				bundleName = readFromPropertiesFile(path, locale, classLoader);
			}
			while (bundleName == null && index != -1);
		}
		
		if (bundleName == null) {
			bundleName = DEFAULT_BASE_NAME;
		}
		
		return getI18n("", bundleName, classLoader, locale, flags);
	}

	/**
	 * Calls
	 * {@link #getI18n(Class, String, Locale) getI18n(clazz, bundleName, Locale.getDefault())}.
	 * 
	 * @since 0.9
	 */
	public static I18n getI18n(final Class clazz, final String bundleName)
	{
		return getI18n(clazz, bundleName, Locale.getDefault());
	}

	/**
	 * Calls
	 * {@link #getI18n(Class, String, Locale, int) getI18n(clazz, bundleName, locale, DEFAULT)}.
	 * 
	 * @since 0.9.1
	 */
	public static I18n getI18n(final Class clazz, final String bundleName, final Locale locale)
	{
		return getI18n(clazz, bundleName, locale, DEFAULT);
	}

	/**
	 * Calls
	 * {@link #getI18n(Class, String, Locale) getI18n(getPackageName(clazz), bundleName, clazz.getClassLoader(), locale, DEFAULT)}.
	 * 
	 * @since 0.9.1
	 */
	public static I18n getI18n(final Class clazz, final String bundleName, final Locale locale, int flags)
	{
		return getI18n(clazz.getName(), bundleName, clazz.getClassLoader(), locale, flags);
	}

	/**
	 * @since 0.9.1
	 */
	public static I18n getI18n(final String path, final String bundleName, final ClassLoader classLoader, final Locale locale,
			final int flags)
	{
		int index;
		String prefix = path;
		do {
			// chop of last segment of path
			index = prefix.lastIndexOf('.');
			prefix = (index != -1) ? prefix.substring(0, index) : "";
			String name = prefix.length() == 0 ? bundleName : prefix + "." + bundleName;
			
			// check cache
			I18n i18n = i18nCache.get(name, locale);
			if (i18n != null) {
				return i18n;
			}
			
			// look for resource bundle in class path
			i18n = findByBaseName(name, locale, getClassLoader(classLoader), flags);
			if (i18n != null) {
				if ((flags & NO_CACHE) == 0) {
					i18nCache.put(name, i18n);
				}
				return i18n;
			}
		}
		while (index != -1);
		
		// fallback to default bundle
		if (isFallbackSet(flags)) {
			I18n i18n = i18nCache.get("", locale);
			if (i18n == null) {
				i18n = new I18n(new EmptyResourceBundle(locale));
				i18nCache.put("", i18n);
			}
			return i18n;
		}
		
		throw new MissingResourceException("Resource bundle not found", path, bundleName);
	}

	static ClassLoader getClassLoader(ClassLoader classLoader) {
		return (classLoader != null) ? classLoader : ClassLoader.getSystemClassLoader();
	}
	
	/**
	 * Tries to create an I18n instance from a properties file.
	 * 
	 * @param path
	 * @param loader
	 * @return null if no properties file was found
	 * @throws MissingResourceException
	 *             if properties file was found but specified resource not
	 */
	static String readFromPropertiesFile(final String path, final Locale locale, final ClassLoader loader)
	{
		Properties props = new Properties();
		String filename = path.length() == 0 ? PROPS_FILENAME : path.replace('.', '/') + "/" + PROPS_FILENAME;
		InputStream in = loader.getResourceAsStream(filename);
		if (in != null) {
			try {
				props.load(in);
			}
			catch (IOException e) {
				// XXX now what?
			}
			finally {
				try {
					in.close();
				}
				catch (IOException e) {
					// this exception is lost
				}
			}
			return props.getProperty(BASENAME_KEY);
		}
		return null;
	}

	/**
	 * Uses the class loader to look for a messages properties file.
	 * 
	 * @param baseName
	 *            the base name of the resource bundle
	 * @param path
	 *            the path that prefixes baseName
	 * @param loader
	 *            the class loader used to look up the bundle
	 * @param flags
	 * @return the created instance
	 */
	static I18n findByBaseName(final String baseName, final Locale locale, final ClassLoader loader, int flags)
	{
		try {
			return createI18n(baseName, locale, loader, flags);
		}
		catch (MissingResourceException e) {
			return null;
		}
	}

	/**
	 * Creates a new i18n instance and registers it with {@link I18nManager}.
	 * 
	 * @param baseName
	 *            the base name of the resource bundle
	 * @param locale
	 *            the locale
	 * @param loader
	 *            the class loader used to look up the bundle
	 * @return the created instance
	 */
	private static I18n createI18n(final String baseName, final Locale locale, final ClassLoader loader, final int flags)
	{
		I18n i18n = new I18n(baseName, locale, loader);
		if (!isNoCacheSet(flags)) {
			I18nManager.getInstance().add(i18n);
		}
		return i18n;
	}
	
	private static boolean isFallbackSet(final int flags)
	{
		return (flags & FALLBACK) != 0;
	}

	private static boolean isReadPropertiesSet(final int flags)
	{
		return (flags & READ_PROPERTIES) != 0;
	}

	private static boolean isNoCacheSet(final int flags)
	{
		return (flags & NO_CACHE) != 0;
	}

}
