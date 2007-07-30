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
import java.util.HashMap;
import java.util.Iterator;
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

	private static HashMap i18nByPackage = new HashMap();
	
	/**
	 * Default name for Message bundles, is "i18n.Messages".
	 */
	public static final String DEFAULT_BASE_NAME = "i18n.Messages";
	/**
	 * Filename of the poperties file that contains the i18n properties,
	 * is "i18n.properties".
	 */
	public static final String PROPS_FILENAME = "i18n.properties";
	
	/**
	 * Clears the cache of i18n objects. Used by the test classes.
	 */
	static void clearCache()
	{
		for (Iterator it = i18nByPackage.values().iterator(); it.hasNext();) {
			I18n i18n = (I18n)it.next();
			I18nManager.getInstance().remove(i18n);
		}
		i18nByPackage.clear();
	}
	
	static void registerI18n(I18n i18n, String base, Class clazz)
	{
		String path = clazz.getName();
		if (!path.startsWith(base)) {
			throw new IllegalArgumentException();
		}
		
		synchronized (i18nByPackage) {
			for (int index = path.lastIndexOf('.'); 
				 index != -1 && path.length() > base.length();
				 index = path.lastIndexOf('.')) {
				path = path.substring(0, index);
				i18nByPackage.put(path, i18n);
			}
		}
	}
	
	private static void registerI18nForDefaultPackage(I18n i18n)
	{
		synchronized (i18nByPackage) {
			i18nByPackage.put("", i18n);
		}
	}
	
	static boolean isInDefaultPackage(Class clazz)
	{
		return clazz.getName().indexOf('.') == -1;
	}
	
	static I18n findI18nInDefaultPackage(String baseName, Locale locale, ClassLoader loader)
	{
		I18n i18n = (I18n)i18nByPackage.get("");
		if (i18n != null) {
			return i18n;
		}
		i18n = readFromPropertiesFile("", locale, loader);
		if (i18n != null) {
			return i18n;
		}
		i18n = findByBaseName(baseName, "", locale, loader);
		if (i18n != null) {
			return i18n;
		}
		return null;
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
	 * @return created or cached <code>I18n</code> instance 
	 * @throws MissingResourceException if no resource bundle was found
	 */
	public static I18n getI18n(Class clazz, String baseName, Locale locale)
	{
		if (isInDefaultPackage(clazz)) {
			I18n i18n = findI18nInDefaultPackage(baseName, locale,
												 clazz.getClassLoader());
			if (i18n != null) {
				registerI18nForDefaultPackage(i18n);
				return i18n;
			}
			else {
				throw new MissingResourceException("resource bundle not found",
						clazz.getClass().getName(), baseName);
			}
		}

		// look for cached versions and property files
		String path = clazz.getName();
		for (int index = path.lastIndexOf('.'); index != -1; 
				index = path.lastIndexOf('.')) {
			path = path.substring(0, index);
			I18n i18n = (I18n)i18nByPackage.get(path);
			if (i18n != null) {
				registerI18n(i18n, path, clazz);
				return i18n;
			}
			
			i18n = readFromPropertiesFile(path, locale, clazz.getClassLoader());
			if (i18n != null) {
				registerI18n(i18n, path, clazz);
				return i18n;
			}
		}
		
		// look for bundle with baseName
		path = clazz.getName();
		for (int index = path.lastIndexOf('.'); index != -1; 
				index = path.lastIndexOf('.')) {
			path = path.substring(0, index);
			I18n i18n = findByBaseName(baseName, path, locale, clazz.getClassLoader());
			if (i18n != null) {
				registerI18n(i18n, path, clazz);
				return i18n;
			}
		}
		
		throw new MissingResourceException("resource bundle not found",
				clazz.getClass().getName(), baseName);
	}
	
	/**
	 * Tries to create an I18n instance from a properties file.
	 * @param path
	 * @param loader
	 * @return null if no properties file was found
	 * @throws MissingResourceException if properties file was found but 
	 * specified resource not
	 */
	static I18n readFromPropertiesFile(String path, Locale locale, ClassLoader loader)
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
				return createI18n(baseName, locale, loader);
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
	 * @return the created instance
	 */
	static I18n findByBaseName(String baseName, String path, Locale locale, ClassLoader loader)
	{
		path = path.length() == 0 ? baseName : path + "." + baseName;
		try {
			return createI18n(path, locale, loader);
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
								   ClassLoader loader)
	{
		I18n i18n = new I18n(baseName, locale, loader);
		I18nManager.getInstance().add(i18n);
		return i18n;
	}
	
}
