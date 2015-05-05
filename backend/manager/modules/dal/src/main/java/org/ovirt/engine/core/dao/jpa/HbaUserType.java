package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;

public class HbaUserType implements UserType {

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object deepCopy(Object val) throws HibernateException {
        return val;
    }

    @Override
    public Serializable disassemble(Object val) throws HibernateException {
        return (Serializable) val;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object value) throws HibernateException {
        return Objects.hashCode(value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor si, Object owner)
            throws HibernateException, SQLException {

        Object value = rs.getObject(names[0]);
        if (value == null)
            return null;

        return new JsonObjectDeserializer().deserialize(value, HashMap.class);
    }

    @Override
    public void nullSafeSet(PreparedStatement stmt, Object value, int index, SessionImplementor si)
            throws HibernateException, SQLException {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, new JsonObjectSerializer().serialize(value));
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return Map.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }
}
