package org.ovirt.engine.core.utils;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class EnumTranslationProperties extends Properties {
    private static final Logger log = LoggerFactory.getLogger(EnumTranslationProperties.class);

    private final Class<? extends Enum>[] classes;

    public EnumTranslationProperties(Class<? extends Enum>... classes) {
        this.classes = classes;
    }

    /**
     * This method overrides the put method of Map in order to add the validation
     * of an existing key. Otherwise the Hashtable implementation inherited by
     * Properties class it would just override existing value without indicating
     * the caller that it already exists.
     */
    @Override
    public Object put(Object key, Object value) {
        String stringKey = (String) key;
        boolean found = false;

        // Skip testing validation messages
        for (Class<? extends Enum> clazz : classes) {
            try {
                // Will throw an IllegalArgumentException if the key isn't an EnumConstant
                Enum.valueOf(clazz, stringKey);
                found = true;
                break;
            } catch (IllegalArgumentException ignore) {
                log.debug(stringKey + " is not a key in " + clazz.getName());
            }
        }

        if (!found) {
            StringBuilder sb = new StringBuilder("No translation for key [")
                    .append(stringKey)
                    .append("] in enums: [")
                    .append(classes[0].getName());

            // Start from the second class
            for (int i = 1; i < classes.length; ++i) {
                sb.append(", ").append(classes[i]);
            }

            sb.append(']');

            throw new MissingEnumTranslationException(sb.toString());
        }

        return super.put(key, value);
    }
}
