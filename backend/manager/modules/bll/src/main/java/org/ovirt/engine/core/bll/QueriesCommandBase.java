package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.common.queries.LicenseReturnValue;
import org.ovirt.engine.core.common.queries.SearchReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dal.VdcCommandBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.WARN)
public abstract class QueriesCommandBase<P extends VdcQueryParametersBase> extends VdcCommandBase {

    private final static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String QuerySuffix = "Query";

    /**
     * Factory to determine the type of the ReturnValue field
     *
     * @return
     */
    protected VdcQueryReturnValue CreateReturnValue() {
        if (getClass().getName().endsWith("GetAllVmSnapshotsByDriveQuery")) {
            return new GetAllVmSnapshotsByDriveQueryReturnValue();
        }
        else if (getClass().getName().endsWith("SearchQuery")) {
            return new SearchReturnValue();
        }
        else if (getClass().getName().endsWith("IsLicenseValidQuery")) {
            return new LicenseReturnValue();
        } else {
            return new VdcQueryReturnValue();
        }
    }

    // get correct return value type
    private final VdcQueryReturnValue returnValue;
    private final VdcQueryType type;
    private final P parameters;

    public QueriesCommandBase(P parameters) {
        this.parameters = parameters;
        returnValue = CreateReturnValue();
        type = initQueryType();
    }

    private final VdcQueryType initQueryType() {
        try {
            String name = getClass().getSimpleName();
            name = name.substring(0, name.length() - QuerySuffix.length());
            return VdcQueryType.valueOf(name);
        } catch (Exception e) {
            return VdcQueryType.Unknown;
        }
    }

    @Override
    protected void ExecuteCommand() {
        if (validatePermissions() && validateInputs()) {
            try {
                returnValue.setSucceeded(true);
                executeQueryCommand();
            } catch (RuntimeException ex) {
                returnValue.setSucceeded(false);
                Throwable th = ex instanceof VdcBLLException ? ex : ex.getCause();
                if (th != null && th instanceof VdcBLLException) {
                    VdcBLLException vdcExc = (VdcBLLException) th;
                    if (vdcExc.getErrorCode() != null) {
                        returnValue.setExceptionString(vdcExc.getErrorCode().toString());
                    } else {
                        returnValue.setExceptionString(vdcExc.getMessage());
                    }
                    log.errorFormat("Query {0} failed. Exception message is {1}",
                            getClass().getSimpleName(),
                            vdcExc.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debugFormat("Detailed stacktrace:", vdcExc);
                    }
                } else {
                    returnValue.setExceptionString(ex.getMessage());
                    log.errorFormat("Query {0} failed. Exception message is {1}",
                            getClass().getSimpleName(),
                            ex.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debugFormat("Detailed stacktrace:", ex);
                    }
                }
            }
        } else {
            log.error("Query execution failed due to invalid inputs. " + returnValue.getExceptionString());
        }
    }

    /**
    * Validates if this query is permitted to run.
    *
    * @return <code>true</code> if the query is OK (i.e., the issuing user has enough permissions to execute it), or
    *         <code>false</code> otherwise.
    */
    private final boolean validatePermissions() {
        // Stub implementation as a placeholder to allow a small commit - currently, all the queries are admin queries
        return type.isAdmin();
    }

    /**
     * @return true if all parameters class and its inner members passed
     *         validation
     */
    private boolean validateInputs() {
        Set<ConstraintViolation<P>> violations = validator.validate(getParameters());
        if (!violations.isEmpty()) {
            returnValue.setExceptionString(violations.toString());
            return false;
        }
        return true;
    }

    protected void ProceedOnFail() {
        // Empty default implementation, method is here to allow inheriting classes to override.
    }

    public VdcQueryReturnValue getQueryReturnValue() {
        return returnValue;
    }

    public P getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", super.toString(),
                (getParameters() != null ? getParameters().toString() : "null"));
    }

    protected abstract void executeQueryCommand();

    @Override
    public void setReturnValue(Object value) {
        returnValue.setReturnValue(value);
    }
}
