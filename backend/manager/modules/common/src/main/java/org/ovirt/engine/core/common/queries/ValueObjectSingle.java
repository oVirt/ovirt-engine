package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public class ValueObjectSingle extends ValueObject implements Serializable {
    private static final long serialVersionUID = -7912651567888297194L;

    private Object value;

    public ValueObjectSingle() {
    }

    public ValueObjectSingle(Object value) {
        this.value = value;
    }

    @Override
    public Object asValue() {
        return value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
