package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SpmStatusResult")
public class SpmStatusResult implements Serializable {
    private static final long serialVersionUID = -2043744550859733845L;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "SpmStatus")
    private SpmStatus privateSpmStatus = SpmStatus.forValue(0);

    public SpmStatus getSpmStatus() {
        return privateSpmStatus;
    }

    public void setSpmStatus(SpmStatus value) {
        privateSpmStatus = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "SpmLVER")
    private String privateSpmLVER;

    public String getSpmLVER() {
        return privateSpmLVER;
    }

    public void setSpmLVER(String value) {
        privateSpmLVER = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
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
