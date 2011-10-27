package org.ovirt.engine.core.common.businessentities.mapping;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import org.ovirt.engine.core.compat.Guid;

public class GuidType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.OTHER };
    }

    @Override
    public Class<Guid> returnedClass() {
        return Guid.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x != null ? x.equals(y) : y == null ? true : false;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) {
        Guid result = new Guid();

        try {
            String value = rs.getString(names[0]);

            result = value != null ? new Guid(value) : null; //  == null ? "" : value);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value != null) {
            if (value instanceof Guid) {
                st.setObject(index, ((Guid) value).toString(), Types.OTHER);
            } else {
                st.setObject(index, new Guid(value.toString()), Types.OTHER);
            }
        } else {
            st.setNull(index, Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value != null ? new Guid(value.toString()) : null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return value.toString();
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return new Guid(cached.toString());
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
