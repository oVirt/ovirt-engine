package org.ovirt.engine.core.common.errors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VDSError")
public class VDSError {

    public VDSError(VdcBllErrors code, String message) {
        this.privateCode = code;
        this.privateMessage = message;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "Message")
    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "Code")
    private VdcBllErrors privateCode = VdcBllErrors.forValue(0);

    public VdcBllErrors getCode() {
        return privateCode;
    }

    public void setCode(VdcBllErrors value) {
        privateCode = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "Args")
    private java.util.ArrayList<Object> privateArgs;

    public java.util.ArrayList<Object> getArgs() {
        return privateArgs;
    }

    public void setArgs(java.util.ArrayList<Object> value) {
        privateArgs = value;
    }

    public VDSError() {
    }
}
