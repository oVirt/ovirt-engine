package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ValueObject implements Serializable {
    private static final long serialVersionUID = -405401713169411987L;

    public List asList() {
        throw new IllegalStateException("Object doesn't represent list");
    }

    public Object asValue() {
        throw new IllegalStateException("Object doesn't represent single value");
    }

    public Map asMap() {
        throw new IllegalStateException("Object doesn't represent map");
    }

    public static ValueObject createValueObject(Object value) {
        if (value instanceof List)
            return new ValueObjectList(((List) value));
        if (value instanceof Map)
            return new ValueObjectMap(((Map) value), false);
        else
            return new ValueObjectSingle(value);
    }
}
