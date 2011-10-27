package org.ovirt.engine.core.dao;

import java.sql.Types;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;

public class SQLServerDataTypeFactory extends DefaultDataTypeFactory {
    @Override
    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        if (sqlType == Types.CHAR)
            if ("uniqueidentifier".equals(sqlTypeName))
                return new UniqueIdentifierType();

        return super.createDataType(sqlType, sqlTypeName);
    }

}
