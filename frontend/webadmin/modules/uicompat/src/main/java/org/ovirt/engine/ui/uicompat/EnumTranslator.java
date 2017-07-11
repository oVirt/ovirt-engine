package org.ovirt.engine.ui.uicompat;

import java.util.MissingResourceException;
import java.util.logging.Logger;

public class EnumTranslator implements Translator<Enum<?>> {
    private static final Logger logger = Logger.getLogger(EnumTranslator.class.getName());
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final EnumTranslator INSTANCE = new EnumTranslator();

    private static final LocalizedEnums enums = ConstantsManager.getInstance().getEnums();

    private EnumTranslator() {
    }

    public static EnumTranslator getInstance() {
        return INSTANCE;
    }

    @Override
    public String translate(Enum<?> key) {
        if (key == null) {
            final String result = constants.notAvailableLabel();
            logger.warning("trying to localize null, probable error. " +
                    "Exception is not thrown, returning '" + result + "'");
            return result;
        }

        try {
            final String translatedString = enums.getString(keyToTranslate(key));
            return translatedString != null ? translatedString : notLocalizedKey(key);
        } catch (MissingResourceException e) {
            // Silently ignore missing resource
            return notLocalizedKey(key, e);
        }
    }

    private String notLocalizedKey(Enum<?> key) {
        return notLocalizedKey(key, null);
    }

    private String notLocalizedKey(Enum<?> key, MissingResourceException e) {
        String logString = "Missing Enum resource '" + key + "'."; //$NON-NLS-1$
        if (e != null) {
            logString += " " + e.getLocalizedMessage();
        }

        logger.warning(logString);
        return key.name();
    }

    private String keyToTranslate(Enum<?> key) {
        String className = key.getDeclaringClass().toString();
        String classNameWithoutPackage = className.substring(className.lastIndexOf(".") + 1, className.length()); //$NON-NLS-1$

        return classNameWithoutPackage + "___" + key.name();
    }

    @Override
    public boolean containsKey(Enum<?> key) {
        final String translatedString = enums.getString(keyToTranslate(key));

        return translatedString != null;
    }
}
