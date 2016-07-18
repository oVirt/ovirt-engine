package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcCommandBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ActionGroupDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.WARN)
public abstract class QueriesCommandBase<P extends VdcQueryParametersBase> extends VdcCommandBase {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String QuerySuffix = "Query";

    // get correct return value type
    private final VdcQueryReturnValue returnValue;
    private final VdcQueryType queryType;
    private DbUser user;
    private final P parameters;
    private boolean isInternalExecution = false;
    private final EngineContext engineContext;

    @Inject
    protected AuditLogDirector auditLogDirector;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    private VDSBrokerFrontend vdsBroker;

    public QueriesCommandBase(P parameters) {
        this(parameters, null);
    }

    public QueriesCommandBase(P parameters, EngineContext engineContext) {
        this.parameters = parameters;
        returnValue = new VdcQueryReturnValue();
        queryType = initQueryType();
        this.engineContext = engineContext == null ? new EngineContext().withSessionId(parameters.getSessionId()) : engineContext;
    }

    /**
     * @see PostConstruct
     */
    @PostConstruct
    protected final void postConstruct() {
        user = initUser();
    }

    private VdcQueryType initQueryType() {
        try {
            String name = getClass().getSimpleName();
            name = name.substring(0, name.length() - QuerySuffix.length());
            return VdcQueryType.valueOf(name);
        } catch (Exception e) {
            return VdcQueryType.Unknown;
        }
    }

    protected DbUser initUser() {
        return getSessionDataContainer().getUser(engineContext.getSessionId(),
                parameters.getRefresh());
    }

    protected SessionDataContainer getSessionDataContainer() {
        return sessionDataContainer;
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getRefresh()) {
            getSessionDataContainer().updateSessionLastActiveTime(getParameters().getSessionId());
        }
        if (validatePermissions()) {
            if (validateInputs()) {
                try {
                    returnValue.setSucceeded(true);
                    executeQueryCommand();
                } catch (RuntimeException ex) {
                    returnValue.setSucceeded(false);
                    Throwable th = ex instanceof EngineException ? ex : ex.getCause();
                    if (th instanceof EngineException) {
                        EngineException vdcExc = (EngineException) th;
                        if (vdcExc.getErrorCode() != null) {
                            returnValue.setExceptionString(vdcExc.getErrorCode().toString());
                        } else {
                            returnValue.setExceptionString(vdcExc.getMessage());
                        }
                        log.error("Query '{}' failed: {}",
                                getClass().getSimpleName(),
                                vdcExc.getMessage());
                        log.error("Exception", vdcExc);
                    } else {
                        returnValue.setExceptionString(ex.getMessage());
                        log.error("Query '{}' failed: {}",
                                getClass().getSimpleName(),
                                ex.getMessage());
                        log.error("Exception", ex);
                    }
                }
            } else {
                log.error("Query execution failed due to invalid inputs: {}", returnValue.getExceptionString());
            }
        } else {
            String errMessage = "Query execution failed due to insufficient permissions.";
            log.error(errMessage);
            returnValue.setExceptionString(errMessage);
        }
    }

    /**
    * Validates if this query is permitted to run.
    *
    * @return <code>true</code> if the query is OK (i.e., the issuing user has enough permissions to execute it), or
    *         <code>false</code> otherwise.
    */
    private boolean validatePermissions() {
        // If the user requests filtered execution, his permissions are inconsequential.
        // If the query supports filtering it should be allowed, and if not - not.
        if (parameters.isFiltered()) {
            return !queryType.isAdmin();
        }

        // If the query was executed internally, it should be allowed in any event.
        if (isInternalExecution) {
            return true;
        }

        // In any other event, we have admin execution, which should only be allowed according to the user's
        // permissions.
        // Note that is cached per session
        return getUser().isAdmin();
    }

    /**
     * @return true if all parameters class and its inner members passed
     *         validation
     */
    protected boolean validateInputs() {
        Set<ConstraintViolation<P>> violations = validator.validate(getParameters());
        if (!violations.isEmpty()) {
            returnValue.setExceptionString(violations.toString());
            return false;
        }
        return true;
    }

    public VdcQueryReturnValue getQueryReturnValue() {
        return returnValue;
    }

    public EngineContext getEngineContext() {
        return engineContext;
    }

    public P getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", super.toString(),
                getParameters() != null ? getParameters().toString() : "null");
    }

    protected abstract void executeQueryCommand();

    @Override
    public void setReturnValue(Object value) {
        returnValue.setReturnValue(value);
    }

    public boolean isInternalExecution() {
        return isInternalExecution;
    }

    public void setInternalExecution(boolean isInternalExecution) {
        this.isInternalExecution = isInternalExecution;
    }

    protected DbUser getUser() {
        return user;
    }

    protected Guid getUserID() {
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    protected long getEngineSessionSeqId() {
        if (engineContext.getSessionId() == null) {
            throw new RuntimeException("No sessionId found for query " + getClass().getName());
        }
        return getSessionDataContainer().getEngineSessionSeqId(engineContext.getSessionId());
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected ActionGroupDao getActionGroupDao() {
        return getDbFacade().getActionGroupDao();
    }

    public VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected VdcQueryReturnValue runInternalQuery(VdcQueryType actionType, VdcQueryParametersBase parameters) {
        //All internal queries should have refresh set to false, since the decision to refresh the session should
        //be up to the client. All internal queries will not refresh the session.
        parameters.setRefresh(false);
        return getBackend().runInternalQuery(actionType, parameters, getEngineContext());
    }

    public VDSBrokerFrontend getVdsBroker() {
        return vdsBroker;
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws EngineException {
        return getVdsBroker().runVdsCommand(commandType, parameters);
    }
}
