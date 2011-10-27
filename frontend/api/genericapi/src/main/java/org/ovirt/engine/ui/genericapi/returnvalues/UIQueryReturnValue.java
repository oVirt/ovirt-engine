package org.ovirt.engine.ui.genericapi.returnvalues;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.ValueObject;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UIQueryReturnValue")
public class UIQueryReturnValue {

    private Object returnValue;
    private boolean succeeded;

    @XmlElement(name = "ReturnValueWrapper")
    public ValueObject getSerializaedReturnValue() {
        return ValueObject.createValueObject(returnValue);
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @XmlElement(name = "Succeeded")
    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean value) {
        succeeded = value;
    }
}
