package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SpmStatusResult")
public class SpmStatusResult implements Serializable {
    private static final long serialVersionUID = -2043744550859733845L;
    @XmlElement(name = "SpmStatus")
    private SpmStatus privateSpmStatus = SpmStatus.forValue(0);

    public SpmStatus getSpmStatus() {
        return privateSpmStatus;
    }

    public void setSpmStatus(SpmStatus value) {
        privateSpmStatus = value;
    }

    @XmlElement(name = "SpmLVER")
    private String privateSpmLVER;

    public String getSpmLVER() {
        return privateSpmLVER;
    }

    public void setSpmLVER(String value) {
        privateSpmLVER = value;
    }

    @XmlElement(name = "SpmId")
    private int privateSpmId;

    public int getSpmId() {
        return privateSpmId;
    }

    public void setSpmId(int value) {
        privateSpmId = value;
    }

    public SpmStatusResult() {
    }
}
