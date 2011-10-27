package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
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

    @XmlElement
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
