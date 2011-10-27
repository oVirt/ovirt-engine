package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import java.io.Serializable;
import java.util.List;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
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

    @XmlElement
    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
