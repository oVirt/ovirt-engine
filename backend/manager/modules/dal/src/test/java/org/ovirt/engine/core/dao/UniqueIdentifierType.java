package org.ovirt.engine.core.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.ovirt.engine.core.compat.Guid;

public class UniqueIdentifierType extends AbstractDataType {

    public UniqueIdentifierType() {
        super("uniqueidentifier", Types.CHAR, Guid.class, false);
    }

    @Override
    public Object typeCast(Object value) throws TypeCastException {
        return value.toString();
    }

    @Override
    public Object getSqlValue(int column, ResultSet resultSet) throws SQLException, TypeCastException {
        String uuid = resultSet.getString(column);

        return uuid.toString();
    }

    @Override
    public void setSqlValue(Object uuid, int column,
                            PreparedStatement statement) throws SQLException, TypeCastException {
        statement.setObject(column, uuid.toString());
    }

}
