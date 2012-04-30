package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.List;

public class ValueObjectList extends ValueObject implements Serializable {
    private static final long serialVersionUID = -5301189842358791641L;

    private List list;

    public ValueObjectList() {
    }

    public ValueObjectList(List list) {
        this.list = list;
    }

    @Override
    public List asList() {
        return list;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
