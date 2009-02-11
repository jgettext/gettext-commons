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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides methods for internationalization.
 * <p>
 * To learn how message strings wrapped in one of the <code>tr*()</code>
 * methods can be extracted and localized, see <a
 * href="http://code.google.com/p/gettext-commons/wiki/Tutorial">this tutorial</a>.
 * 
 * @author Steffen Pingel
 * @author Felix Berger
 * @author Tammo van Lessen
 * @since 0.9
 */
public class I18n {

	private static final String CONTEXT_GLUE = "\u0004";
	
	/**
	 * Reference to the current localization bundles.
	 */
	private volatile ResourceBundle bundle;

	/**
	 * The locale of the strings used in the source code.
	 * 
	 * @see #trc(String, String)
	 */
	private volatile Locale sourceCodeLocale = Locale.ENGLISH;

	private String baseName;

	private ClassLoader loader;

	private volatile Locale locale;

	/**
	 * Constructs an I18n object for a resource bundle.
	 * 
	 * @param bundle
	 *            must not be <code>null</code>
	 * @throws NullPointerException
	 *             if <code>bundle</code> is null
	 * @since 0.9
	 */
	public I18n(ResourceBundle bundle)
	{
		setResources(bundle);
	}

	/**
	 * Constructs an I18n object by calling {@link #setResources(String, Locale,
	 * ClassLoader)}.
	 * 
	 * @throws MissingResourceException
	 *             if the resource bundle could not be loaded
	 * @throws NullPointerException
	 *             if one of the arguments is <code>null</code>
	 * @since 0.9
	 */
	public I18n(String baseName, Locale locale, ClassLoader loader)
	{
		setResources(baseName, locale, loader);
	}

	/**
	 * Returns the current resource bundle.
	 * 
	 * @since 0.9
	 */
	public ResourceBundle getResources()
	{
		return bundle;
	}

	/**
	 * Returns the locale this instance was created with. This can be different
	 * from the locale of the resource bundle returned by
	 * {@link #getResources()}.
	 * 
	 * @return the locale or null, if this instance was directly created from a
	 *         resource bundle
	 * @since 0.9
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Sets a resource bundle to be used for message translations.
	 * <p>
	 * If this is called, the possibly previously specified class loader and
	 * baseName are invalidated, since the bundle might be from a different
	 * context. Subsequent calls to {@link #setLocale(Locale)} won't have any
	 * effect.
	 * 
	 * @since 0.9
	 */
	public synchronized void setResources(ResourceBundle bundle)
	{
		if (bundle == null) {
			throw new NullPointerException();
		}
		this.bundle = bundle;
		this.baseName = null;
		this.locale = bundle.getLocale();
		this.loader = null;
	}

	/**
	 * Tries to load a resource bundle using {@link
	 * ResourceBundle#getBundle(java.lang.String, java.util.Locale,
	 * java.lang.ClassLoader)}.
	 * 
	 * @throws MissingResourceException
	 *             if the bundle could not be loaded
	 * @throws NullPointerException
	 *             if one of the arguments is <code>null</code>
	 * @since 0.9
	 */
	public synchronized void setResources(String baseName, Locale locale, ClassLoader loader)
	{
		this.bundle = ResourceBundle.getBundle(baseName, locale, loader);
		this.baseName = baseName;
		this.locale = locale;
		this.loader = loader;
	}

	/**
	 * Marks <code>text</code> to be translated, but doesn't return the
	 * translation but <code>text</code> itself.
	 * 
	 * @since 0.9
	 */
	public static final String marktr(String text)
	{
		return text;
	}

	/**
	 * Tries to load a resource bundle for the locale.
	 * <p>
	 * The resource bundle is then used for message translations. Note, you have
	 * to retrieve all messages anew after a locale change in order for them to
	 * be translated to the language specified by the new locale.
	 * <p>
	 * 
	 * @return false if there is not enough information for loading a new
	 *         resource bundle, see {@link #setResources(ResourceBundle)}.
	 * @throws MissingResourceException
	 *             if the resource bundle for <code>locale</code> could not be
	 *             found
	 * @throws NullPointerException
	 *             if <code>locale</code> is null
	 * @since 0.9
	 */
	public synchronized boolean setLocale(Locale locale)
	{
		if (baseName != null && loader != null) {
			setResources(baseName, locale, loader);
			return true;
		}
		else {
			this.locale = locale;
		}
		return false;
	}

	/**
	 * Sets the locale of the text in the source code.
	 * <p>
	 * Only languages that have one singular and one plural form can be used as
	 * source code locales, since {@link #trn(String, String, long)} takes
	 * exactly these two forms as parameters.
	 * 
	 * @param locale
	 *            the locale
	 * @throws NullPointerException
	 *             if <code>locale</code> is <code>null</code>
	 * @see #trc(String, String)
	 * @since 0.9
	 */
	public void setSourceCodeLocale(Locale locale)
	{
		if (locale == null) {
			throw new NullPointerException("locale must not be null");
		}
		sourceCodeLocale = locale;
	}

	/**
	 * Returns <code>text</code> translated into the currently selected
	 * language. Every user-visible string in the program must be wrapped into
	 * this function.
	 * 
	 * @param text
	 *            text to translate
	 * @return the translation
	 * @since 0.9
	 */
	public final String tr(String text)
	{
		try {
			return bundle.getString(text);
		}
		catch (MissingResourceException e) {
			return text;
		}
	}

	/**
	 * Returns <code>text</code> translated into the currently selected
	 * language.
	 * <p>
	 * Occurrences of {number} placeholders in text are replaced by
	 * <code>objects</code>.
	 * <p>
	 * Invokes
	 * {@link MessageFormat#format(java.lang.String, java.lang.Object[])}.
	 * 
	 * @param text
	 *            text to translate
	 * @param objects
	 *            arguments to <code>MessageFormat.format()</code>
	 * @return the translated text
	 * @since 0.9
	 */
	public final String tr(String text, Object[] objects)
	{
		return MessageFormat.format(tr(text), objects);
	}

	/**
	 * Convenience method that invokes {@link #tr(String, Object[])}.
	 * 
	 * @since 0.9
	 */
	public final String tr(String text, Object o1)
	{
		return tr(text, new Object[]{ o1 });
	}

	/**
	 * Convenience method that invokes {@link #tr(String, Object[])}.
	 * 
	 * @since 0.9
	 */
	public final String tr(String text, Object o1, Object o2)
	{
		return tr(text, new Object[]{ o1, o2 });
	}

	/**
	 * Convenience method that invokes {@link #tr(String, Object[])}.
	 * 
	 * @since 0.9
	 */
	public final String tr(String text, Object o1, Object o2, Object o3)
	{
		return tr(text, new Object[]{ o1, o2, o3 });
	}

	/**
	 * Convenience method that invokes {@link #tr(String, Object[])}.
	 * 
	 * @since 0.9
	 */
	public final String tr(String text, Object o1, Object o2, Object o3, Object o4)
	{
		return tr(text, new Object[]{ o1, o2, o3, o4 });
	}

	/**
	 * Returns the plural form for <code>n</code> of the translation of
	 * <code>text</code>.
	 * 
	 * @param text
	 *            the key string to be translated.
	 * @param pluralText
	 *            the plural form of <code>text</code>.
	 * @param n
	 *            value that determines the plural form
	 * @return the translated text
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n)
	{
		try {
			return trnInternal(bundle, text, pluralText, n);
		}
		catch (MissingResourceException e) {
			return (n == 1) ? text : pluralText;
		}
	}

	/**
	 * Returns the plural form for <code>n</code> of the translation of
	 * <code>text</code>.
	 * 
	 * @param text
	 *            the key string to be translated.
	 * @param pluralText
	 *            the plural form of <code>text</code>.
	 * @param n
	 *            value that determines the plural form
	 * @param objects
	 *            object args to be formatted and substituted.
	 * @return the translated text
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n, Object[] objects)
	{
		return MessageFormat.format(trn(text, pluralText, n), objects);
	}

	/**
	 * Overloaded method that invokes
	 * {@link #trn(String, String, long, Object[])} passing <code>Object</code>
	 * arguments as an array.
	 * 
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n, Object o1)
	{
		return trn(text, pluralText, n, new Object[]{ o1 });
	}

	/**
	 * Overloaded method that invokes
	 * {@link #trn(String, String, long, Object[])} passing <code>Object</code>
	 * arguments as an array.
	 * 
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n, Object o1, Object o2)
	{
		return trn(text, pluralText, n, new Object[]{ o1, o2 });
	}

	/**
	 * Overloaded method that invokes
	 * {@link #trn(String, String, long, Object[])} passing <code>Object</code>
	 * arguments as an array.
	 * 
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n, Object o1, Object o2, Object o3)
	{
		return trn(text, pluralText, n, new Object[]{ o1, o2, o3 });
	}

	/**
	 * Overloaded method that invokes
	 * {@link #trn(String, String, long, Object[])} passing <code>Object</code>
	 * arguments as an array.
	 * 
	 * @since 0.9
	 */
	public final String trn(String text, String pluralText, long n, Object o1, Object o2, Object o3, Object o4)
	{
		return trn(text, pluralText, n, new Object[]{ o1, o2, o3, o4 });
	}

	/**
	 * Returns the plural form for <code>n<code> of the translation of ???
	 *      
	 * Based on GettextResource.java that is part of GNU gettext for Java
	 * Copyright (C) 2001 Free Software Foundation, Inc.
	 * 
	 * @param bundle a ResourceBundle
	 * @param text the key string to be translated, an ASCII string
	 * @param pluralText its English plural form
	 * @return the translation of <code>text</code> depending on <code>n</code>,
	 *         or <code>text</code> or <code>pluralText</code> if none is found
	 */
	private static String trnInternal(ResourceBundle orgBundle, String text, String pluralText, long n)
	{
		ResourceBundle bundle = orgBundle;
		do {
			boolean isGetTextBundle = false;
			boolean hasPluralHandling = false;
			Method handleGetObjectMethod = null;
			Method getParentMethod = null;
			Method lookupMethod = null;
			Method pluralEvalMethod = null;
			try {
				handleGetObjectMethod = bundle.getClass().getMethod("handleGetObject", new Class[]{ String.class });
				getParentMethod = bundle.getClass().getMethod("getParent", new Class[0]);
				isGetTextBundle = Modifier.isPublic(handleGetObjectMethod.getModifiers());
				lookupMethod = bundle.getClass().getMethod("lookup", new Class[]{ String.class });
				pluralEvalMethod = bundle.getClass().getMethod("pluralEval", new Class[]{ Long.TYPE });
				hasPluralHandling = true;
			}
			catch (Exception e) {}
			if (isGetTextBundle) {
				// GNU gettext generated bundle
				if (hasPluralHandling) {
					// GNU gettext generated bundle w/ plural handling
					try {
						Object localValue = lookupMethod.invoke(bundle, new Object[]{ text });
						if (localValue.getClass().isArray()) {
							String[] pluralforms = (String[])localValue;
							long index = 0;
							try {
								index = ((Long)pluralEvalMethod.invoke(bundle, new Object[]{ new Long(n) }))
										.longValue();
								if (!(index >= 0 && index < pluralforms.length)) {
									index = 0;
								}
							}
							catch (IllegalAccessException e) {}
							return pluralforms[(int)index];
						}
						else {
							// Found the value. It doesn't depend on n in this
							// case.
							return (String)localValue;
						}
					}
					catch (Exception e) {}
				}
				else {
					// GNU gettext generated bundle w/o plural handling
					try {
						Object localValue = handleGetObjectMethod.invoke(bundle, new Object[]{ text });
						if (localValue != null) {
							return (String)localValue;
						}
					}
					catch (Exception e) {}
				}
				bundle = null;
				try {
					bundle = (ResourceBundle)getParentMethod.invoke(bundle, new Object[0]);
				}
				catch (Exception e) {}
			}
			else {
				return bundle.getString(text);
			}
		}
		while (bundle != null);
		throw new MissingResourceException("Can not find resource for key " + text + " in bundle "
				+ orgBundle.getClass().getName(), orgBundle.getClass().getName(), text);
	}

	/**
	 * Disambiguates translation keys.
	 * 
	 * @param context
	 *            the context of the text to be translated
	 * @param text
	 *            the ambiguous key message in the source locale
	 * @return <code>text</code> if the locale of the underlying resource
	 *         bundle equals the source code locale, the translation of
	 *         <code>comment</code> otherwise.
	 * @see #setSourceCodeLocale(Locale)
	 * @since 0.9
	 */
	public final String trc(String context, String text)
	{
		if (sourceCodeLocale.equals(getResources().getLocale())) { 
			return text;
		} else {
			String key = context + CONTEXT_GLUE + text; 
			String translated = tr(key);
			// if no translation was found return text in source locale
			return translated == key ? text : translated;
		}
	}
	
	/** 
	 * Returns the plural form for <code>n</code> of the translation of
	 * <code>text</code>.
	 * 
	 * @param context
	 * 			  the context of the message to disambiguate it when translating
	 * @param text
	 *            the key string to be translated.
	 * @param pluralText
	 *            the plural form of <code>text</code>.
	 * @param n
	 *            value that determines the plural form
	 * @return the translated text
	 * @since 0.9.5
	 */
	public final String trnc(String context, String singularText, String pluralText, long n) {
		try {
			return trnInternal(bundle, context + CONTEXT_GLUE + singularText, pluralText, n);
		}
		catch (MissingResourceException e) {
			return (n == 1) ? singularText : pluralText;
		}
	}

	/**
	 * Returns the plural form for <code>n</code> of the translation of
	 * <code>text</code>.
	 * 
	 * @param context
	 * 			  the context of the message to disambiguate it when translating
	 * @param text
	 *            the key string to be translated.
	 * @param pluralText
	 *            the plural form of <code>text</code>.
	 * @param n
	 *            value that determines the plural form
	 * @param objects
	 *            object args to be formatted and substituted.
	 * @return the translated text
	 * @since 0.9
	 */
	public final String trnc(String context, String singularText, String pluralText, long n, Object[] objects) {
		return MessageFormat.format(trnc(context, singularText, pluralText, n), objects);
	}

	/**
	 * Overloaded method that invokes
	 * {@link #trnc(String, String, String, long, Object[]) passing <code>obj</code>
	 * arguments as an array.
	 * 
	 * @since 0.9.5
	 */
	public final String trnc(String comment, String singularText, String pluralText, long n, Object obj) {
		return MessageFormat.format(trnc(comment, singularText, pluralText, n), new Object[] { obj });
	}
	
	/**
	 * Overloaded method that invokes
	 * {@link #trnc(String, String, String, long, Object[]) passing <code>obj1</code> and <code>obj2</code> 
	 * arguments as an array.
	 * 
	 * @since 0.9.5
	 */
	public final String trnc(String comment, String singularText, String pluralText, long n, Object obj1, Object obj2) {
		return MessageFormat.format(trnc(comment, singularText, pluralText, n), new Object[] { obj1, obj2 });
	}
	
	/**
	 * Overloaded method that invokes
	 * {@link #trnc(String, String, String, long, Object[]) passing <code>obj1</code>, <code>obj2</code> and <code>obj3</code>
	 * arguments as an array.
	 * 
	 * @since 0.9.5
	 */
	public final String trnc(String comment, String singularText, String pluralText, long n, Object obj1, Object obj2, Object obj3) {
		return MessageFormat.format(trnc(comment, singularText, pluralText, n), new Object[] { obj1, obj2, obj3 });
	}
	
	/**
	 * Overloaded method that invokes
	 * {@link #trnc(String, String, String, long, Object[]) passing <code>obj1</code>, <code>obj2</code>, <code>obj3</code> and <code>obj4</code>
	 * arguments as an array.
	 * 
	 * @since 0.9.5
	 */
	public final String trnc(String comment, String singularText, String pluralText, long n, Object obj1, Object obj2, Object obj3, Object obj4) {
		return MessageFormat.format(trnc(comment, singularText, pluralText, n), new Object[] { obj1, obj2, obj3, obj4 });
	}
	
}
