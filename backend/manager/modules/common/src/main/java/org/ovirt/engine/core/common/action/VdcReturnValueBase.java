package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.compat.Guid;

public class VdcReturnValueBase implements Serializable {
    private static final long serialVersionUID = 6063371142273092365L;

    private boolean canDoAction;
    private ArrayList<String> canDoActionMessages;
    private boolean succeeded;
    private boolean isSyncronious;
    private Object returnValue;
    private String description;
    /**
     * Holds the ids of the async task place holders in the database. On server restart this list used to clean up and
     * fail all the tasks that have place holders but don't have a vdsm task id.
     */
    private ArrayList<Guid> taskPlaceHolderIdList;
    /**
     * The list of vdsm task ids associated with a command
     */
    private ArrayList<Guid> vdsmTaskIdList;
    /**
     * The list of vdsm task ids associated directly with a command
     */
    private ArrayList<Guid> internalVdsmTaskIdList;
    private boolean endActionTryAgain;
    private ArrayList<String> executeFailedMessages;
    private VdcFault fault;
    private String correlationId;
    private Guid jobId;

    public VdcReturnValueBase() {
        canDoActionMessages = new ArrayList<String>();
        description = "";
        taskPlaceHolderIdList = new ArrayList<Guid>();
        vdsmTaskIdList = new ArrayList<Guid>();
        internalVdsmTaskIdList = new ArrayList<Guid>();
        endActionTryAgain = true;
        executeFailedMessages = new ArrayList<String>();
    }

    public VdcFault getFault() {
        if (fault == null) {
            fault = new VdcFault();
            fault.setError(VdcBllErrors.ENGINE);
            fault.setMessage(VdcBllErrors.ENGINE.name());
        }
        return fault;
    }

    public void setFault(VdcFault value) {
        fault = value;
    }

    public boolean getCanDoAction() {
        return canDoAction;
    }

    public void setCanDoAction(boolean value) {
        canDoAction = value;
    }

    public ArrayList<String> getCanDoActionMessages() {
        return canDoActionMessages;
    }

    public void setCanDoActionMessages(ArrayList<String> value) {
        canDoActionMessages = value;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean value) {
        succeeded = value;
    }

    public ArrayList<String> getExecuteFailedMessages() {
        return executeFailedMessages;
    }

    public void setExecuteFailedMessages(ArrayList<String> value) {
        executeFailedMessages = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getActionReturnValue() {
        return (T)returnValue;
    }

    public void setActionReturnValue(Object value) {
        returnValue = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    public boolean getIsSyncronious() {
        return isSyncronious;
    }

    public void setIsSyncronious(boolean value) {
        isSyncronious = value;
    }

    public ArrayList<Guid> getVdsmTaskIdList() {
        return vdsmTaskIdList;
    }

    public void setVdsmTaskIdList(ArrayList<Guid> value) {
        vdsmTaskIdList = value;
    }

    public ArrayList<Guid> getTaskPlaceHolderIdList() {
        return taskPlaceHolderIdList;
    }

    public void setTaskPlaceHolderIdList(ArrayList<Guid> value) {
        taskPlaceHolderIdList = value;
    }

    public ArrayList<Guid> getInternalVdsmTaskIdList() {
        return internalVdsmTaskIdList;
    }

    public boolean getHasAsyncTasks() {
        return (getVdsmTaskIdList() != null && getVdsmTaskIdList().size() > 0);
    }

    public boolean getEndActionTryAgain() {
        return endActionTryAgain;
    }

    public void setEndActionTryAgain(boolean value) {
        endActionTryAgain = value;
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
