package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;

/**
 * Action (command) return value class.
 *
 * <p>
 * Final on purpose, to avoid the need of implementing GWT custom field serializer
 * for each subclass. Instead of subclassing, consider using the {@link #returnValue}
 * object itself to carry any additional data.
 * </p>
 */
public final class ActionReturnValue implements Serializable {
    private static final long serialVersionUID = 9194321065754359119L;

    private boolean valid;
    private ArrayList<String> validationMessages;
    private boolean succeeded;
    private boolean isSynchronous;
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
    private EngineFault fault;
    private String correlationId;
    private Guid jobId;

    public ActionReturnValue() {
        validationMessages = new ArrayList<>();
        description = "";
        taskPlaceHolderIdList = new ArrayList<>();
        vdsmTaskIdList = new ArrayList<>();
        internalVdsmTaskIdList = new ArrayList<>();
        endActionTryAgain = true;
        executeFailedMessages = new ArrayList<>();
    }

    public EngineFault getFault() {
        if (fault == null) {
            fault = new EngineFault();
            fault.setError(EngineError.ENGINE);
            fault.setMessage(EngineError.ENGINE.name());
        }
        return fault;
    }

    public void setFault(EngineFault value) {
        fault = value;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean value) {
        valid = value;
    }

    public ArrayList<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(ArrayList<String> value) {
        validationMessages = value;
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
        return (T) returnValue;
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

    public boolean getIsSynchronous() {
        return isSynchronous;
    }

    public void setIsSynchronous(boolean value) {
        isSynchronous = value;
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
        return getVdsmTaskIdList() != null && getVdsmTaskIdList().size() > 0;
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
