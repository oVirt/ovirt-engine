package org.ovirt.engine.core.dal.dbbroker;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Automatically translates incoming complex values as needed
 */
@Named
@Dependent
public class CustomMapSqlParameterSource extends MapSqlParameterSource {

    private final DbEngineDialect dialect;

    @Inject
    public CustomMapSqlParameterSource(DbEngineDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public MapSqlParameterSource addValue(String paramName, Object value) {
        Object tmpValue = value;
        // just to be safe
        if (tmpValue != null) {

            // lets check if we need to translate value
            if (tmpValue instanceof Enum) {
                tmpValue = extractEnumValue(tmpValue);
            } else if (tmpValue instanceof Guid) {
                tmpValue = ((Guid) tmpValue).getUuid();
            } else if (tmpValue instanceof Version) {
                tmpValue = value.toString();
            }
        }

        return super.addValue(dialect.getParamNamePrefix() + paramName, tmpValue);
    }

    @Override
    public MapSqlParameterSource addValues(Map<String, ?> values) {
        if (values != null) {
            for (Entry<String, ?> entry : values.entrySet()) {
                addValue(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }

    private static Object extractEnumValue(Object value) {
        Method getValueMethod = findMethodByName(value, "getValue");
        if (getValueMethod != null) {
            return invokeMethod(value, getValueMethod);
        }

        Method ordinalMethod = findMethodByName(value, "ordinal");
        if (ordinalMethod != null) {
            return invokeMethod(value, ordinalMethod);
        }

        return Integer.valueOf(0);
    }

    private static Object invokeMethod(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke method", e);
        }
    }

    private static Method findMethodByName(Object value, String methodName) {
        try {
            return value.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
