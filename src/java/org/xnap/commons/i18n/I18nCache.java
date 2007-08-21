package org.xnap.commons.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Steffen Pingel
 */
class I18nCache {
	
	private final Map i18nByPackage = new HashMap();

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
			throw new IllegalArgumentException();
		}
		
		List list = (List)i18nByPackage.get(packageName);
		if (list != null) {
			for (Iterator it = list.iterator(); it.hasNext();) {
				I18n i18n = (I18n)it.next();
				if (locale.equals(i18n.getLocale())) {
					return i18n;
				}
			}
		}
		return null;
	}

	public void put(String packageName, I18n i18n)
	{
		List list = (List)i18nByPackage.get(packageName);
		if (list == null) {
			list = new ArrayList();
			i18nByPackage.put(packageName, list);
		}
		list.add(i18n);
	}

	public void visit(final Visitor visitor)
	{
		for (Iterator it = i18nByPackage.values().iterator(); it.hasNext();) {
			List list = (List)it.next();
			for (Iterator it2 = list.iterator(); it2.hasNext();) {
				I18n i18n = (I18n)it2.next();
				visitor.visit(i18n);
			}
		}
	}

	public static interface Visitor {
		
		void visit(I18n i18n);
		
	}

}
