package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.compat.Guid;

public class VdcReturnValueBase implements Serializable {
    private static final long serialVersionUID = 6063371142273092365L;

    private boolean _canDoAction;
    private ArrayList<String> _canDoActionMessages = new ArrayList<String>();
    private boolean _succeeded;
    private boolean _isSyncronious;
    private Object _returnValue;
    private String _description = "";
    private ArrayList<Guid> _taskIdList = new ArrayList<Guid>();
    private ArrayList<Guid> _internalIdList = new ArrayList<Guid>();
    private boolean _endActionTryAgain = true;
    private ArrayList<String> _executeFailedMessages = new ArrayList<String>();
    private VdcFault _fault;
    private String correlationId;
    private Guid jobId;

    public VdcReturnValueBase() {
    }

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

    public boolean getCanDoAction() {
        return _canDoAction;
    }

    public void setCanDoAction(boolean value) {
        _canDoAction = value;
    }

    public ArrayList<String> getCanDoActionMessages() {
        return _canDoActionMessages;
    }

    public void setCanDoActionMessages(ArrayList<String> value) {
        _canDoActionMessages = value;
    }

    public boolean getSucceeded() {
        return _succeeded;
    }

    public void setSucceeded(boolean value) {
        _succeeded = value;
    }

    public ArrayList<String> getExecuteFailedMessages() {
        return _executeFailedMessages;
    }

    public Object getActionReturnValue() {
        return _returnValue;
    }

    public void setActionReturnValue(Object value) {
        _returnValue = value;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String value) {
        _description = value;
    }

    public boolean getIsSyncronious() {
        return _isSyncronious;
    }

    public void setIsSyncronious(boolean value) {
        _isSyncronious = value;
    }

    public ArrayList<Guid> getTaskIdList() {
        return _taskIdList;
    }

    public void setTaskIdList(ArrayList<Guid> value) {
        _taskIdList = value;
    }

    public ArrayList<Guid> getInternalTaskIdList() {
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

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }
}
