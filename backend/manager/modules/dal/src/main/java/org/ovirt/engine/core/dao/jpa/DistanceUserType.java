package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class DistanceUserType implements UserType {
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object deepCopy(Object val) throws HibernateException {
        if (val == null)
            return null;

        return val;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable disassemble(Object val) throws HibernateException {
        if (val == null) {
            return null;
        }

        return (HashMap<Integer, Integer>) val;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null && y == null) {
            return true;
        }
        if (x == null) {
            return false;
        }
        // TODO: Compare map
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
        if (value == null)
            return null;
        return getDistanceMap(value.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(PreparedStatement stmt, Object value, int index, SessionImplementor si)
            throws HibernateException, SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setString(index, getDistanceString((Map<Integer, Integer>) value));
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

    // format: (<index_id>, <distance>);*, for example: "0, 10; 2, 16"
    private Map<Integer, Integer> getDistanceMap(String distance) {
        Map<Integer, Integer> nodeDistance = new HashMap<>();
        if (StringUtils.isBlank(distance)) {
            return nodeDistance;
        }
        String[] distanceArray = distance.split(";");
        for (int i = 0; i < distanceArray.length; i++) {
            String[] nodeDistanceArray = distanceArray[i].split(",");
            nodeDistance.put(Integer.valueOf(nodeDistanceArray[0]), Integer.valueOf(nodeDistanceArray[1]));
        }
        return nodeDistance;
    }

    private String getDistanceString(Map<Integer, Integer> distance) {
        if (distance == null || distance.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<Integer, Integer> entry : distance.entrySet()) {
            sb.append(entry.getKey());
            sb.append(",");
            sb.append(entry.getValue());
            sb.append(";");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

}
