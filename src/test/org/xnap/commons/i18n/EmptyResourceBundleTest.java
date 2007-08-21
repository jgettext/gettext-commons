package org.xnap.commons.i18n;

import junit.framework.TestCase;

public class EmptyResourceBundleTest extends TestCase {

    public void test() {
        EmptyResourceBundle bundle = new EmptyResourceBundle(null);
        assertEquals("Foo", bundle.getObject("Foo"));
        assertFalse(bundle.getKeys().hasMoreElements());
    }
    
}
