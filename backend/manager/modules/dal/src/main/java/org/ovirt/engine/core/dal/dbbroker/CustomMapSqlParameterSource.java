package org.ovirt.engine.core.dal.dbbroker;

import java.lang.reflect.Method;

import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Automatically translates incoming complex values as needed
 */
public class CustomMapSqlParameterSource extends MapSqlParameterSource {

    private DbEngineDialect dialect;

    public CustomMapSqlParameterSource(DbEngineDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public MapSqlParameterSource addValue(String paramName, Object value) {
        // just to be safe
        if (value != null) {

            // lets check if we need to translate value
            if (value.getClass().isEnum())
                value = extractEnumValue(value);
            else if (value instanceof NGuid)
                value = value.toString();
            else if (value instanceof Version)
                value = value.toString();
        }

        return super.addValue(dialect.getParamNamePrefix() + paramName, value);
    }

    private Object extractEnumValue(Object value) {
        Method getValueMethod = findMethodByName(value, "getValue");
        if (getValueMethod != null) {
            return invokeMethod(value, getValueMethod);
        } else {
            Method ordinalMethod = findMethodByName(value, "ordinal");
            if (ordinalMethod != null) {
                return invokeMethod(value, ordinalMethod);
            } else {
                return Integer.valueOf(0);
            }
        }
    }

    private Object invokeMethod(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke method", e);
        }
    }

    private Method findMethodByName(Object value, String methodName) {
        try {
            return value.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
