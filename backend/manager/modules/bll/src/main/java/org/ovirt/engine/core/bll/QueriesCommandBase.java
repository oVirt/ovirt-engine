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
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcCommandBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.WARN)
public abstract class QueriesCommandBase<P extends QueryParametersBase> extends VdcCommandBase {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String QuerySuffix = "Query";

    // get correct return value type
    private final QueryReturnValue returnValue;
    private final QueryType queryType;
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

    @Inject
    protected BackendInternal backend;

    public QueriesCommandBase(P parameters, EngineContext engineContext) {
        if (parameters.getCorrelationId() == null) {
            parameters.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        } else {
            CorrelationIdTracker.setCorrelationId(parameters.getCorrelationId());
        }
        this.parameters = parameters;
        returnValue = new QueryReturnValue();
        returnValue.setCorrelationId(parameters.getCorrelationId());
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

    private QueryType initQueryType() {
        try {
            String name = getClass().getSimpleName();
            name = name.substring(0, name.length() - QuerySuffix.length());
            return QueryType.valueOf(name);
        } catch (Exception e) {
            return QueryType.Unknown;
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
        if (getParameters().getRefresh() || getSessionDataContainer().isSsoOvirtAppApiScope(getParameters().getSessionId())) {
            getSessionDataContainer().updateSessionLastActiveTime(getParameters().getSessionId());
        }
        if (validatePermissions()) {
            if (validateInputs()) {
                long start = System.currentTimeMillis();
                try {
                    returnValue.setSucceeded(true);
                    executeQueryCommand();
                } catch (RuntimeException ex) {
                    handleException(ex, true);
                } finally {
                    log.debug("Query {} took {} ms", getCommandName(), System.currentTimeMillis() - start);
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

    protected void handleException(RuntimeException ex, boolean printStack) {
        returnValue.setSucceeded(false);
        Throwable th = ex instanceof EngineException ? ex : ex.getCause();
        if (th instanceof EngineException) {
            EngineException vdcExc = (EngineException) th;
            if (vdcExc.getErrorCode() != null && !vdcExc.isUseRootCause()) {
                returnValue.setExceptionString(vdcExc.getErrorCode().toString());
            } else {
                returnValue.setExceptionString(vdcExc.getMessage());
            }
            log.error("Query '{}' failed: {}",
                    getClass().getSimpleName(),
                    vdcExc.getMessage());
            if (printStack) {
                log.error("Exception", vdcExc);
            }
        } else {
            returnValue.setExceptionString(ex.getMessage());
            log.error("Query '{}' failed: {}",
                    getClass().getSimpleName(),
                    ex.getMessage());
            if (printStack) {
                log.error("Exception", ex);
            }
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

    /**
     * Apply {@linkplain ValidationResult} to {@linkplain QueryReturnValue} maintained for this query.
     * @param result to be applied
     * @return true if provided result is valid
     */
    public boolean validate(ValidationResult result) {
        if (result.isValid()) {
            return true;
        }
        returnValue.setExceptionString(result.getMessagesAsStrings().stream().findFirst().orElse(""));
        returnValue.setSucceeded(false);
        return false;
    }

    public QueryReturnValue getQueryReturnValue() {
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

    protected QueryReturnValue runInternalQuery(QueryType actionType, QueryParametersBase parameters) {
        //All internal queries should have refresh set to false, since the decision to refresh the session should
        //be up to the client. All internal queries will not refresh the session.
        parameters.setRefresh(false);
        return backend.runInternalQuery(actionType, parameters, getEngineContext());
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws EngineException {
        return vdsBroker.runVdsCommand(commandType, parameters);
    }
}
