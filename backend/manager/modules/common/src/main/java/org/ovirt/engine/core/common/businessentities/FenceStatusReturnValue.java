package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.StringHelper;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FenceStatusReturnValue")
public class FenceStatusReturnValue implements Serializable {
    private static final long serialVersionUID = 8070963676213797507L;
    // indicates that operation was skipped because Host is already in requested state.
    public static final String SKIPPED = "skipped";
    public FenceStatusReturnValue(String status, String message) {
        _status = status;
        _message = message;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Status")
    private String _status;

    public String getStatus() {
        return _status;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Message")
    private String _message;

    public String getMessage() {
        return (StringHelper.EqOp(_message.toLowerCase(), "done")) ? "" : _message;
    }

    public boolean getIsSucceeded() {
        return (StringHelper.isNullOrEmpty(getMessage()));
    }

    public boolean getIsSkipped() {
        return _status.equalsIgnoreCase(SKIPPED);
    }

    @Override
    public String toString() {
        final String MSG = "Host Status is: ";
        final String TEST_SUCCEEDED = "Test Succeeded, ";
        final String TEST_FAILED = "Test Failed, ";
        final String SEP = ". ";
        final String FAILED_MESSAGE_HEADER = "The fence-agent script reported the following error: ";
        return (getIsSucceeded()) ? TEST_SUCCEEDED + MSG + getStatus() : TEST_FAILED + MSG + getStatus() + SEP
               + FAILED_MESSAGE_HEADER + getMessage();

    }

    public FenceStatusReturnValue() {
    }
}
