import java.util.Locale;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * This example class illustrates how the methods provided by {@link i18n}
 * can be used to localize text messages.
 * 
 * Nota bene: Contrary to this example changing the resources during
 * run-time is usually not a good idea, since most strings are saved in the
 * swing components and not reread from resource bundles.
 * 
 * @author Felix Berger
 */
public class I18nExample
{
	public static void main(String[] args)
	{
		I18n i18n = I18nFactory.getI18n(I18nExample.class, "Messages");
		/*
		 * We do two runs, the first with the orginal locale the second one
		 * with the German locale, to see which messages are translated and
		 * how.
		 */
		for (int i = 0; i < 2; i++) {
			
			if (i == 0) {
				print("First run");
				
			}
			else {
				print("Second run");
				i18n.setLocale(Locale.GERMAN);
			}
			
			/*
			 * This is the method you will be using most of the time.
			 */
			print(i18n.tr("This text is marked for translation and is translated"));
		
			/*
			 * This method marks the text for translation, but doesn't
			 * translate. This can be used for keys which should be stored
			 * untranslated but should be translated in the user interface.
			 */
			String mark = i18n.marktr("This text is marked for translation but not translated");
		
			/*
			 * See in the second run, it's never translated.
			 */
			print(mark);
		
			/*
			 * Now you can use the text in a variable and it is correctly
			 * translated.
			 */
			print(i18n.tr(mark));
		
			/*
			 * A convenience wrapper for MessageFormat.format(String, Object[]).
			 */
			print(i18n.tr("Four: {0}", new Integer(4)));
		
			/*
			 * This method disambiguates a word which has to be translated
			 * differently depending on how it is used. In our example the
			 * word "chat" is translated differently to German when it is
			 * used as a noun and as a verb.
			 */
			print(i18n.trc("chat (verb)", "chat"));
			
			print(i18n.trc("chat (noun)", "chat"));
		
			/*
			 * I18n.trn handles plurals. The third parameter contains the number
			 * of objects to decide which plural form to use.
			 */
			print(i18n.trn("{0} file is open", "{0} files are open", 1, new Integer(1)));
			
			print(i18n.trn("{0} file is open", "{0} files are open", 2, new Integer(2)));
		}
	}
	
	private static void print(String text)
	{
		System.out.println(text);
	}
}
