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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Stores a list of {@link I18n} objects by a String key.
 * 
 * @author Steffen Pingel
 */
class I18nCache {
	
	/**
	 * Map<String, List<I18n>>, list is synchronized too.
	 */
	private final Map i18nByPackage = Collections.synchronizedMap(new HashMap());

	I18nCache()
	{
	}

	public void clear()
	{
		i18nByPackage.clear();
	}

	public I18n get(final String packageName, final Locale locale)
	{
		if (locale == null) {
			throw new NullPointerException("locale is null");
		}
		
		List list = (List)i18nByPackage.get(packageName);
		if (list != null) {
			synchronized (list) {
				for (Iterator it = list.iterator(); it.hasNext();) {
					I18n i18n = (I18n)it.next();
					if (locale.equals(i18n.getLocale())) {
						return i18n;
					}
				}
			}
		}
		return null;
	}

	public void put(String packageName, I18n i18n)
	{
		List list;
		synchronized (i18nByPackage) {
			list = (List)i18nByPackage.get(packageName);
			if (list == null) {
				list = Collections.synchronizedList(new ArrayList(2));
				i18nByPackage.put(packageName, list);
			}
		}
		list.add(i18n);
	}

	public void visit(final Visitor visitor)
	{
		List[] lists;
		synchronized (i18nByPackage) {
			 lists = (List[])i18nByPackage.values().toArray(new List[0]);
		}
		for (int i = 0; i < lists.length; i++) {
			List list = lists[i];
			synchronized (list) {
				for (Iterator it2 = list.iterator(); it2.hasNext();) {
					I18n i18n = (I18n)it2.next();
					visitor.visit(i18n);
				}
			}
		}
	}

	public static interface Visitor {
		
		void visit(I18n i18n);
		
	}

}
