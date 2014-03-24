package org.ovirt.engine.ui.uicompat;

import java.util.MissingResourceException;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;

public class EnumTranslator extends Translator<Enum<?>> {
    private static final Logger logger = Logger.getLogger(EnumTranslator.class.getName());
    private static final UIConstants constants = GWT.create(UIConstants.class);
    private static final EnumTranslator INSTANCE = new EnumTranslator();

    private Enums enums = GWT.create(Enums.class);

    private EnumTranslator() {
    }

    public static EnumTranslator getInstance() {
        return INSTANCE;
    }

    @Override
    public String get(Enum<?> key) {
        if(key == null) {
           return constants.notAvailableLabel();
        }

        try {
            //FIXME: hack: due to java restriction for method names with chars that are not letters, digits, and underscores, replace . with 0
            String enumName = key.getDeclaringClass().toString();
            enumName = enumName.substring(enumName.lastIndexOf(".")+1,enumName.length()); //$NON-NLS-1$
            String translatedEnum = enums.getString(enumName + "___" + key.toString()); //$NON-NLS-1$

            return translatedEnum;
        } catch (MissingResourceException e) {
            // Silently ignore missing resource
            logger.info("Missing Enum resource: " + e.getLocalizedMessage()); //$NON-NLS-1$
            return key.name();
        }
    }
}
