package contractmanager.utility;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class serves as a assistant when handling resource files. It stores global variable for easy access to
 * property file. It also provides simple way to get message with placeholders.
 *
 * @author Vaclav Mares
 */
public class ResourceHandler {

    /** Log4j logger for this class */
    private static final Logger logger = Logger.getLogger(ResourceHandler.class);

    /** ResourceBundle with properties */
    private static final ResourceBundle properties = ResourceBundle.getBundle("contractmanager");

    /** ResourceBundle with localization Strings */
    public static final ResourceBundle localization = ResourceBundle.getBundle("contractmanager",
            new Locale("en", "EN"));


    /** Constructor not meant to be used */
    private ResourceHandler(){}


    /**
     * Using this method is possible to get String resource from properties resource bundle. It also enables to get
     * the message with arguments using placeholders such as {0}, {1}, etc.
     *
     * @param resource      Name of resource to recover
     * @param arguments     Optional arguments used as a variables in message
     * @return              String with message with replaced variables if there were any
     */
    public static String getLocaleString(String resource, Object ... arguments){

        String message = "";

        try {
            String pattern = localization.getString(resource);
            message = MessageFormat.format(pattern, arguments);
        }
        catch (MissingResourceException e){
            logger.error(e.getMessage());
        }

        return message;
    }


    // Getters
    public static ResourceBundle getProperties() {
        return properties;
    }

    public static ResourceBundle getLocalization() {
        return localization;
    }
}
