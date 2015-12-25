package org.ovirt.engine.core.dao;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.ext.postgresql.GenericEnumType;
import org.dbunit.ext.postgresql.InetType;
import org.dbunit.ext.postgresql.IntervalType;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.ext.postgresql.UuidType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresDataTypeFactory extends DefaultDataTypeFactory {
    private static final Logger log = LoggerFactory.getLogger(PostgresqlDataTypeFactory.class);
    private static final Collection<String> DATABASE_PRODUCTS = Arrays.asList(new String[] { "PostgreSQL" });

    @Override
    public Collection<String> getValidDbProducts() {
        return DATABASE_PRODUCTS;
    }

    @Override
    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        log.debug("createDataType(sqlType='{}', sqlTypeName='{}')",
                sqlType, sqlTypeName);

        if (sqlType == Types.OTHER) {
            // Treat Postgresql UUID types as VARCHARS

            if ("uuid".equals(sqlTypeName)) {
                return new UuidType();
            } else if ("interval".equals(sqlTypeName)) {
                // Intervals are custom types
                return new IntervalType();
            } else if ("inet".equals(sqlTypeName)) {
                return new InetType();
            } else {
                // Finally check whether the user defined a custom datatype
                if (isEnumType(sqlTypeName)) {
                    log.debug("Custom enum type used for sqlTypeName '{}' (sqlType '{}')",
                            sqlTypeName, Integer.valueOf(sqlType));
                    return new GenericEnumType(sqlTypeName);
                }
            }
        }

        return super.createDataType(sqlType, sqlTypeName);
    }

    /**
     * Returns a data type for the given sql type name if the user wishes one. <b>Designed to be overridden by custom
     * implementations extending this class.</b> Override this method if you have a custom enum type in the database and
     * want to map it via dbunit.
     *
     * @param sqlTypeName
     *            The sql type name for which users can specify a custom data type.
     * @return <code>null</code> if the given type name is not a custom type which is the default implementation.
     * @since 2.4.6
     */
    public boolean isEnumType(String sqlTypeName) {
        return false;
    }

}
