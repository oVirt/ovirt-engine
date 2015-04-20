package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.ovirt.engine.core.compat.Guid;

public class GuidUserType implements UserType {

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
        return (Serializable)val;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x != null && x instanceof byte[] && y != null && y instanceof byte[]) {
            return Arrays.equals((byte[])x, (byte[])y);
        }
        if (x == null && y == null) {
            return true;
        }
        if (x == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Object value) throws HibernateException {
        return value.hashCode();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor si, Object owner)
            throws HibernateException, SQLException {

        Object value = rs.getObject(names[0]);
        if (value == null) {
            return null;
        }

        return new Guid((UUID) value);
    }

    @Override
    public void nullSafeSet(PreparedStatement stmt, Object value, int index, SessionImplementor si)
            throws HibernateException, SQLException {
        if (value == null) {
            stmt.setNull(index, Types.OTHER);
        } else {
            stmt.setObject(index, ((Guid) value).getUuid());
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return Guid.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.OTHER };
    }

}
