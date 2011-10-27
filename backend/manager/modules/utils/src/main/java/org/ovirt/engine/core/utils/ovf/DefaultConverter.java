package org.ovirt.engine.core.utils.ovf;

import org.apache.commons.beanutils.ConvertUtils;

/**
 * Default converter - can be used for primitive types , their boxed versions (i.e Boolean, Integer, etc..) and Strings
 * Uses Apache beans
 *
 */
public class DefaultConverter implements TypeConverter {

    @Override
    public String convert(Object value) {
        return ConvertUtils.convert(value);
    }

    @Override
    public Object convert(String value, Class<?> clazz) {
        return ConvertUtils.convert(value, clazz);
    }

}
