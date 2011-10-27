package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.MigrationSupport;

/**
 * Custom converter for migration support
 *
 */
public class MigrationSupportConverter implements TypeConverter {

    @Override
    public String convert(Object value) {

        return value.toString();
    }

    @Override
    public Object convert(String value, Class<?> clazz) {

        return MigrationSupport.valueOf(value);
    }

}
