package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.IEndedTaskVisitor;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 4872560145516614773L;

    private transient String sessionid;
    private boolean shouldbelogged;
    private String httpSessionId;
    private VdcUser parametersCurrentUser;
    private TransactionScopeOption transctionOption;

    /**
     * Indicates if the command should use the compensation mechanism or not.
     */
    private boolean compensationEnabled = false;

    private VdcActionType parentCommand = VdcActionType.Unknown;

    private transient VdcActionParametersBase parentParameters;

    // this flag marks if the command ran with MultipleAction for ProcessExceptionToClient
    private boolean multipleAction;

    private Object entityId;

    private ArrayList<VdcActionParametersBase> imagesParameters;

    private boolean taskGroupSuccess;

    private long taskStartTime;

    private ArrayList<Guid> taskIds;

    /**
     * A cross system identifier of the executed action
     */
    private String correlationId;

    public VdcActionParametersBase() {
        shouldbelogged = true;
        transctionOption = TransactionScopeOption.Required;
        setTaskGroupSuccess(true);
        taskStartTime = System.currentTimeMillis();
        setParentCommand(VdcActionType.Unknown);
    }

    public String getSessionId() {
        if (StringHelper.isNullOrEmpty(sessionid)) {
            if (getHttpSessionId() != null) {
                sessionid = getHttpSessionId();
            } else {
                sessionid = "";
            }
        }
        return sessionid;
    }

    public void setSessionId(String value) {
        sessionid = value;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public VdcUser getParametersCurrentUser() {
        return parametersCurrentUser;
    }


    public void setParametersCurrentUser(VdcUser value) {
        parametersCurrentUser = value;
    }

    public boolean getShouldBeLogged() {
        return shouldbelogged;
    }

    public void setShouldBeLogged(boolean value) {
        shouldbelogged = value;
    }

    public TransactionScopeOption getTransactionScopeOption() {
        return transctionOption;
    }

    public void setTransactionScopeOption(TransactionScopeOption value) {
        transctionOption = value;
    }

    public boolean isCompensationEnabled() {
        return compensationEnabled;
    }

    public void setCompensationEnabled(boolean compensationEnabled) {
        this.compensationEnabled = compensationEnabled;
    }

    public VdcActionType getParentCommand() {
        return parentCommand;
    }

    public void setParentCommand(VdcActionType value) {
        parentCommand = value;
    }

    public VdcActionParametersBase getParentParameters() {
        return parentParameters;
    }

    public void setParentParemeters (VdcActionParametersBase parameters) {
        parentParameters = parameters;
    }

    public boolean getMultipleAction() {
        return multipleAction;
    }

    public void setMultipleAction(boolean value) {
        multipleAction = value;
    }

    public ArrayList<VdcActionParametersBase> getImagesParameters() {
        if (imagesParameters == null) {
            imagesParameters = new ArrayList<VdcActionParametersBase>();
        }
        return imagesParameters;
    }

    public void setImagesParameters(ArrayList<VdcActionParametersBase> value) {
        imagesParameters = value;
    }

    public boolean getTaskGroupSuccess() {
        return taskGroupSuccess;
    }

    public void setTaskGroupSuccess(boolean value) {
        taskGroupSuccess = value;
    }

    public boolean Accept(EndedTaskInfo taskInfo, IEndedTaskVisitor visitor) {
        boolean retVal = visitor.Visit(taskInfo, this);
        if (!retVal) {
            for (VdcActionParametersBase parameters : getImagesParameters()) {
                retVal = parameters.Accept(taskInfo, visitor);
                if (retVal) {
                    break;
                }
            }
        }
        return retVal;
    }


    public Object getEntityId() {
        return entityId;
    }

    public void setEntityId(Object value) {
        entityId = value;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(long value) {
        taskStartTime = value;
    }

    public ArrayList<Guid> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(ArrayList<Guid> value) {
        taskIds = value;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imagesParameters == null) ? 0 : imagesParameters.hashCode());
        result = prime * result + (shouldbelogged ? 1231 : 1237);
        result = prime * result + (int) (taskStartTime ^ (taskStartTime >>> 32));
        result = prime * result + ((transctionOption == null) ? 0 : transctionOption.hashCode());
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result + ((httpSessionId == null) ? 0 : httpSessionId.hashCode());
        result = prime * result + (multipleAction ? 1231 : 1237);
        result =
                prime * result + ((parametersCurrentUser == null) ? 0 : parametersCurrentUser.hashCode());
        result = prime * result + ((parentCommand == null) ? 0 : parentCommand.hashCode());
        result = prime * result + (taskGroupSuccess ? 1231 : 1237);
        result = prime * result + ((taskIds == null) ? 0 : taskIds.hashCode());
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VdcActionParametersBase other = (VdcActionParametersBase) obj;
        if (imagesParameters == null) {
            if (other.imagesParameters != null)
                return false;
        } else if (!imagesParameters.equals(other.imagesParameters))
            return false;
        if (shouldbelogged != other.shouldbelogged)
            return false;
        if (taskStartTime != other.taskStartTime)
            return false;
        if (transctionOption != other.transctionOption)
            return false;
        if (entityId == null) {
            if (other.entityId != null)
                return false;
        } else if (!entityId.equals(other.entityId))
            return false;
        if (httpSessionId == null) {
            if (other.httpSessionId != null)
                return false;
        } else if (!httpSessionId.equals(other.httpSessionId))
            return false;
        if (multipleAction != other.multipleAction)
            return false;
        if (parametersCurrentUser == null) {
            if (other.parametersCurrentUser != null)
                return false;
        } else if (!parametersCurrentUser.equals(other.parametersCurrentUser))
            return false;
        if (parentCommand != other.parentCommand)
            return false;
        if (taskGroupSuccess != other.taskGroupSuccess)
            return false;
        if (taskIds == null) {
            if (other.taskIds != null)
                return false;
        } else if (!taskIds.equals(other.taskIds))
            return false;
        if (correlationId == null) {
            if (other.correlationId != null)
                return false;
        } else if (!correlationId.equals(other.correlationId))
            return false;
        return true;
    }
}
