The Gettext Commons project provides Java classes for internationalization (i18n) through GNU gettext.

The lightweight library combines the power of the unix-style gettext tools with the widely used Java ResourceBundles. This makes it possible to use the original text instead of arbitrary property keys, which is less cumbersome and makes programs easier to read. And there are a lot more advantages of using gettext:

  * Easy extraction of user visible strings
  * Strings are marked as fuzzy when the original text changes so translators can check if the translations still match
  * Powerful plural handling
  * Build process integration through Maven or Ant

Here is an example that demonstrates how easy it is to use the Gettext Commons:

```
I18n i18n = I18nFactory.getI18n(getClass());
System.out.println(i18n.tr("This text will be translated"));
```

Have a look at the [tutorial](Tutorial.md) to see how you can enhance your Java application with the Gettext Commons.
