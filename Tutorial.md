# Requirements #

  * Java 1.3 (or higher)
  * GNU gettext

# Internationalization #

The Gettext Commons provide the class I18n that has methods that need to be invoked each time a user visible string is used:

```
I18n i18n = I18nFactory.getI18n(getClass());
System.out.println(i18n.tr("This text will be translated"));
```

I18n also supports proper handling of plurals:

```
System.out.println(i18n.trn("Copied file.", "Copied files.", 1));
// will print "Copied file."

System.out.println(i18n.trn("Copied file.", "Copied files.", 4));
// will print "Copied files."
```

In addition there are convenience methods that use the Java API [MessageFormat.format()](http://java.sun.com/javase/6/docs/api/java/text/MessageFormat.html#patterns) for substitution:

```
System.out.println(i18n.tr("Folder: {0}", new File("/home/xnap-commons"));
System.out.println(i18n.tr("It''s {0} o''clock.", 6));
// Will print "It's 6 o'clock". Note the double apostrophes.
// See Java documentation about java.text.MessageFormat.
System.out.println(i18n.trn("Night {0} of 1001", "More than 1001 nights! {0} already!", 1002, new Integer(1024)));
// Will print "More than 1001 nights! 1024 already!"
```

And sometimes it is necessary to provide different translations of the same word as some words may have multiple meanings in the native language the program is written but not in other languages:

```
System.out.println(i18n.trc("chat (verb)", "chat"));
System.out.println(i18n.trc("chat (noun)", "chat"));
```

The preferable way to create an I18n object is through the I18nFactory. The factory caches the I18n object internally using the the package name of the provided class object and registers it with I18nManager. Thus all classes of the same package will use the same I18n instance.

```
public class SampleClass
{
        private static I18n i18n = I18nFactory.getI18n(SampleClass.class);

        String localizedString;

        public SampleClass()
        {
                localizedString = i18n.tr("Hello, World");
        }
}
```

I18nManager lets you register independently created I18n objects and provides the facility to change the locale of all registered I18n objects thereby notifying possible LocaleChangeListeners:

```
public class LocaleChangeAwareClass implements LocaleChangeListener
{
        private static I18n i18n = I18nFactory.getI18n(LocaleChangeAwareClass.class);

        String localizedString;

        public LocaleChangeAwareClass()
        {
                localizedString = i18n.tr("Hello, World");
                I18nManager.getInstance().addWeakLocaleChangeListener(this);
        }

        public void localeChanged(LocaleChangeEvent event)
        {
                // update strings
                localizedString = i18n.tr("Hello, World");
                ...
        }
}
```

# Creating Resource Bundles #

Once the source code has been internationalized, i.e. all user visible strings are wrapped by a call to i18n.tr(), xgettext can be used to extract these strings for localization.
This 3 step process is illustrated in [this figure](http://xnap-commons.sourceforge.net/gettext-commons/gettext-structure.png).


  1. xgettext scans the source code for calls to tr(), trc() and trn() and creates a pot file that contains all strings in the native language.
  1. msgmerge merges the strings into a po file that contains translations for a single locale. This file can be edited with convenient tools like poedit, KBabel or Emacs.
  1. msgfmt is used to generate Java class files that extend the Java ResourceBundle class.

Here is a simple example of running the gettext commands:

```
# extract keys
xgettext -ktrc:1c,2 -ktrnc:1c,2,3 -ktr -kmarktr -ktrn:1,2 -o po/keys.pot src/*.java 

# merge keys into localized po file
msgmerge -U po/de.po po/keys.pot

# translate file
poedit po/de.po

# create German ResourceBundle class file in app.i18n package
msgfmt --java2 -d src/conf -r app.i18n.Messages -l de po/de.po
```

This commands have to  be run in the root directory of the project as the class path (in this exdample app/i18n/Messages\_de) will be written into the ResourceBundle class file. You can check the path by opening the class file in a hex editor like ht (command name is hte); it is located at the beginning of the file directly after the [magic number](http://en.wikipedia.org/wiki/Magic_number_(programming)) `as0xcafebabe`.

If the .java files are not all in the same directory, you can search for them:

```
xgettext -ktrc -ktr -kmarktr -ktrn:1,2 -o po/keys.pot $(find . -name "*.java")
```

The `.po` files have to be created if they don't exist already.
```
touch po/de.po
```

`msgfmt` will create the file `app.i18n.Messages_de.class`. It may have to be loaded like that in a class called `SampleClass`:
```
I18n i18n = I18nFactory.getI18n(SampleClass.class, "app.i18n.Messages", java.util.Locale.GERMAN);
```

# Loading Resource Bundles #

In order to specify the name of the resource bundles you want to use, you create an i18n.properties file and place it in your application's toplevel package. All subpackages will use this configuration file unless they find a different one on the way up in the package hierarchy.

**i18n.properties** expects the base name of the resources as the value of the key basename:

```
basename=app.i18n.Messages
```

For more information on how the resource lookup works, please [read here](http://xnap-commons.sourceforge.net/gettext-commons/apidocs/org/xnap/commons/i18n/I18nFactory.html#getI18n(java.lang.Class,java.lang.String)).

# Tools Integration #

If Maven is used for building, the invocation of gettext can be easily integrated into the build process:

  * [Maven 2.x Gettext Plugin](http://gettext-commons.googlecode.com/svn/maven2-plugins-site/plugin-info.html)