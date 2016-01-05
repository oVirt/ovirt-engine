package org.ovirt.engine.core.utils;

import java.util.Properties;

@SuppressWarnings("serial")
public class EnumTranslationProperties<E extends Enum<E>> extends Properties {
    private final Class<E> enumClass;

    public EnumTranslationProperties(Class<E> enumClass) {
        this.enumClass = enumClass;
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

        try {
            // Will throw an IllegalArgumentException if the key isn't an EnumConstant
            Enum.valueOf(enumClass, stringKey);
        } catch (IllegalArgumentException e) {
            throw new MissingEnumTranslationException("No translation for key [" + stringKey + "] in " + enumClass);
        }

        return super.put(key, value);
    }
}
