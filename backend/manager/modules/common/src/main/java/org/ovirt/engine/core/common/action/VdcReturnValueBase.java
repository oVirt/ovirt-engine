package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.ovirt.engine.core.common.errors.*;

@XmlSeeAlso({ VMStatus.class, LoginResult.class, VdcLoginReturnValueBase.class })
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcReturnValueBase")
public class VdcReturnValueBase implements Serializable {
    private static final long serialVersionUID = 6063371142273092365L;

    private boolean _canDoAction;

    @XmlElement(name = "CanDoActionMessages")
    private java.util.ArrayList<String> _canDoActionMessages = new java.util.ArrayList<String>();
    private boolean _succeeded;
    private boolean _isSyncronious;
    private Object _returnValue;
    private String _description = "";
    private java.util.ArrayList<Guid> _taskIdList = new java.util.ArrayList<Guid>();
    private final java.util.ArrayList<Guid> _internalIdList = new java.util.ArrayList<Guid>();
    private boolean _endActionTryAgain = true;

    @XmlElement(name = "ExecuteFailedMessages")
    private final java.util.ArrayList<String> _executeFailedMessages = new java.util.ArrayList<String>();

    public VdcFault getFault() {
        if (_fault == null) {
            _fault = new VdcFault();
            _fault.setError(VdcBllErrors.ENGINE);
            _fault.setMessage(VdcBllErrors.ENGINE.name());
        }
        return _fault;
    }

    public void setFault(VdcFault value) {
        _fault = value;
    }

    @XmlElement(name = "_fault")
    private VdcFault _fault;

    @XmlElement(name = "CanDoAction")
    public boolean getCanDoAction() {
        return _canDoAction;
    }

    public void setCanDoAction(boolean value) {
        _canDoAction = value;
    }

    public java.util.ArrayList<String> getCanDoActionMessages() {
        return _canDoActionMessages;
    }

    public void setCanDoActionMessages(java.util.ArrayList<String> value) {
        _canDoActionMessages = value;
    }

    @XmlElement(name = "Succeeded")
    public boolean getSucceeded() {
        return _succeeded;
    }

    public void setSucceeded(boolean value) {
        _succeeded = value;
    }

    public java.util.ArrayList<String> getExecuteFailedMessages() {
        return _executeFailedMessages;
    }

    @XmlElement(name = "ActionReturnValue")
    public Object getActionReturnValue() {
        return _returnValue;
    }

    public void setActionReturnValue(Object value) {
        _returnValue = value;
    }

    @XmlElement(name = "Description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String value) {
        _description = value;
    }

    @XmlElement(name = "IsSynchronous")
    public boolean getIsSyncronious() {
        return _isSyncronious;
    }

    public void setIsSyncronious(boolean value) {
        _isSyncronious = value;
    }

    @XmlElement(name = "TaskIdListGuidArray")
    public java.util.ArrayList<Guid> getTaskIdList() {
        return _taskIdList;
    }

    public void setTaskIdList(java.util.ArrayList<Guid> value) {
        _taskIdList = value;
    }

    public java.util.ArrayList<Guid> getInternalTaskIdList() {
        return _internalIdList;
    }

    public boolean getHasAsyncTasks() {
        return (getTaskIdList() != null && getTaskIdList().size() > 0);
    }

    public boolean getEndActionTryAgain() {
        return _endActionTryAgain;
    }

    public void setEndActionTryAgain(boolean value) {
        _endActionTryAgain = value;
    }

    public VdcReturnValueBase() {
    }
}
