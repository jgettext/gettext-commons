/*
 *  XNap Commons
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
 * responsible for handling message translations. The bundle is returned with
 * an {@link I18n} object wrapped around it, which provides the translation
 * methods. The lookup is described at {@link #getI18n(Class,String)}.
 * <p>
 * Use the factory for creating <code>I18n</code> objects to make sure no
 * extraneous objects are created.
 *   
 * @author Felix Berger
 * @author Tammo van Lessen
 * @author Steffen Pingel
 */
public class I18nFactory {

	/** 
	 * Use the default configuration.
	 */
	public static final int DEFAULT = 0;
	
    /**
     * Fall back to a default resource bundle that returns the passed text if no
     * resource bundle can be located.
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
	 * Filename of the properties file that contains the i18n properties,
	 * is "i18n.properties".
	 */
	public static final String PROPS_FILENAME = "i18n.properties";

	private static final I18nCache i18nCache = new I18nCache();
	
	private I18nFactory() {
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
	
	static void registerI18n(final I18n i18n, final String base)
	{	
		synchronized (i18nCache) {
			i18nCache.put(base, i18n);
		}
	}
	
	static boolean isInDefaultPackage(Class clazz)
	{
		return clazz.getName().indexOf('.') == -1;
	}
	
	/**
	 * Calls {@link #getI18n(Class, String, Locale) getI18n(clazz,
	 * DEFAULT_BASE_NAME, locale)}.
	 */
	public static I18n getI18n(Class clazz, Locale locale) {
		return getI18n(clazz, DEFAULT_BASE_NAME, locale);
	}
	
	/**
	 * Calls {@link #getI18n(Class, Locale) getI18n(clazz, Locale.getDefault())}.
	 */
	public static I18n getI18n(Class clazz)
	{
		return getI18n(clazz, Locale.getDefault());
	}
	
	/**
	 * Calls {@link #getI18n(Class, String, Locale) getI18n(clazz, baseName, Locale.getDefault())}. 
	 */
	public static I18n getI18n(Class clazz, String baseName) {
		return getI18n(clazz, baseName, Locale.getDefault());
	}
	
    /**
     * Calls {@link #getI18n(Class, String, Locale) getI18n(clazz, baseName, Locale.getDefault(), false)}. 
     */
	public static I18n getI18n(Class clazz, String baseName, Locale locale)
    {
	    return getI18n(clazz, baseName, Locale.getDefault(), READ_PROPERTIES);
    }
	
	/**
	 * Returns the I18n instance responsible for translating messages in
	 * the package specified by <code>clazz</code>.
	 * <p>
	 * Lookup works by iterating upwards in the package hierarchy: First the
	 * internal cache is asked for an I18n object for a package, otherwise the
	 * algorithm looks for an <code>i18n.properties</code> file in the
	 * package.  The properties file is queried for a key named
	 * <code>basename</code> whose value should be the fully qualified
	 * resource/class name of the resource bundle, e.g
	 * <code>org.xnap.commons.i18n.Messages</code>.
	 * <p>
	 * If after the first iteration no I18n instance has been found, a second
	 * search begins by looking for resource bundles having the name
	 * <code>baseName</code>.
	 * 
	 * @param clazz the package hierarchy of the clazz and its class loader
	 * are used for resolving and loading the resource bundle
	 * @param baseName the name of the underlying resource bundle
	 * @param locale the locale of the underlying resource bundle
	 * @param flags a combination of these configuration flags: {@link #FALLBACK}
	 * @return created or cached <code>I18n</code> instance 
	 * @throws MissingResourceException if no resource bundle was found
	 * @since 0.9.1
	 */
	public static I18n getI18n(Class clazz, String baseName, Locale locale, int flags)
	{
		String path;
		I18n i18n = null;
	
		// look for cached versions and property files
		path = getPackageName(clazz);
		for (int index = path.lastIndexOf('.'); i18n == null && index != -1; 
		index = path.lastIndexOf('.')) {
			path = path.substring(0, index);

			i18n = i18nCache.get(path, locale);

			if (i18n == null && ((flags & READ_PROPERTIES) != 0)) {
				i18n = readFromPropertiesFile(path, locale, clazz.getClassLoader(), flags);
			}
		}

		// look for bundle with baseName
		path = getPackageName(clazz); 
		for (int index = path.lastIndexOf('.'); i18n == null && index != -1; 
		index = path.lastIndexOf('.')) {
			path = path.substring(0, index);
			i18n = findByBaseName(baseName, path, locale, clazz.getClassLoader(), flags);
		}

		
		if (i18n == null && (flags & FALLBACK) != 0) {
			path = "";
		    i18n = new I18n(new EmptyResourceBundle(locale));
		}
		 
		if (i18n != null) {
			if ((flags & NO_CACHE) == 0) {
				registerI18n(i18n, path);
			}
			return i18n;
		}
		
		throw new MissingResourceException("resource bundle not found",
		            clazz.getClass().getName(), baseName);
	}
	
	private static String getPackageName(Class clazz)
	{
		return isInDefaultPackage(clazz) ? "." : clazz.getName();
	}

	/**
	 * Tries to create an I18n instance from a properties file.
	 * @param path
	 * @param loader
	 * @return null if no properties file was found
	 * @throws MissingResourceException if properties file was found but 
	 * specified resource not
	 */
	static I18n readFromPropertiesFile(String path, Locale locale, ClassLoader loader, int flags)
	{
		Properties props = new Properties();
		path = path.length() == 0 ? PROPS_FILENAME : path.replace('.', '/')
				+ "/" + PROPS_FILENAME;
		InputStream in = loader.getResourceAsStream(path);
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
			String baseName = props.getProperty("basename");
			if (baseName != null) {
				return createI18n(baseName, locale, loader, flags);
			}
		}
		return null;
	}
	
	/**
	 * Uses the class loader to look for a messages properties file.
	 *
	 * @param baseName the base name of the resource bundle
	 * @param path the path that prefixes baseName 
	 * @param loader the class loader used to look up the bundle
	 * @param flags 
	 * @return the created instance
	 */
	static I18n findByBaseName(String baseName, String path, Locale locale, ClassLoader loader, int flags)
	{
		path = path.length() == 0 ? baseName : path + "." + baseName;
		try {
			return createI18n(path, locale, loader, flags);
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
	/**
	 * Creates a new i18n instance and registers it with {@link I18nManager}.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param locale the locale
	 * @param loader the class loader used to look up the bundle
	 * @return the created instance
	 */
	private static I18n createI18n(String baseName, Locale locale,
								   ClassLoader loader, int flags)
	{
		I18n i18n = new I18n(baseName, locale, loader);
		if ((flags & NO_CACHE) == 0) {
			I18nManager.getInstance().add(i18n);
		}
		return i18n;
	}
	
}
