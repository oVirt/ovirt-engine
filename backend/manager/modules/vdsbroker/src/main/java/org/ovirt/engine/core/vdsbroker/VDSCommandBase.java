package org.ovirt.engine.core.vdsbroker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.VdcCommandBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSRecoveringException;

public abstract class VDSCommandBase<P extends VDSParametersBase> extends VdcCommandBase {
    private P _parameters;

    public P getParameters() {
        return _parameters;
    }

    protected VDSReturnValue _returnValue = null;

    public VDSReturnValue getVDSReturnValue() {
        return _returnValue;
    }

    public void setVDSReturnValue(VDSReturnValue value) {
        _returnValue = value;
    }

    @Override
    public Object getReturnValue() {
        return getVDSReturnValue().getReturnValue();
    }

    @Override
    public void setReturnValue(Object value) {
        getVDSReturnValue().setReturnValue(value);
    }

    public VDSCommandBase(P parameters) {
        _parameters = parameters;
    }

    @Override
    public String toString() {
        String addInfo = getAdditionalInformation();
        return String.format("%s(%s %s)", super.toString(),
                (!addInfo.isEmpty() ? addInfo + "," : StringUtils.EMPTY),
                (getParameters() != null ? getParameters().toString() : "null"));
    }

    @Override
    protected void ExecuteCommand() {
        try {
            // creating ReturnValue object since execute can be called more than once (failover)
            // and we want returnValue clean from last run.
            _returnValue = new VDSReturnValue();
            getVDSReturnValue().setSucceeded(true);
            ExecuteVDSCommand();
        } catch (VDSNetworkException ex) {
            setVdsNetworkError(ex);
        } catch (IRSErrorException ex) {
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(ex.toString());
            getVDSReturnValue().setExceptionObject(ex);
            getVDSReturnValue().setVdsError(ex.getVdsError());
            logException(ex);
        } catch (RuntimeException ex) {
            setVdsRuntimeError(ex);
        }

    }

    protected void setVdsRuntimeError(RuntimeException ex) {
        getVDSReturnValue().setSucceeded(false);
        getVDSReturnValue().setExceptionString(ex.toString());
        getVDSReturnValue().setExceptionObject(ex);

        VDSExceptionBase vdsExp = (VDSExceptionBase) ((ex instanceof VDSExceptionBase) ? ex : null);
        // todo: consider adding unknown vds error in case of non
        // VDSExceptionBase exception
        if (vdsExp != null) {
            if (vdsExp.getVdsError() != null) {
                getVDSReturnValue().setVdsError(((VDSExceptionBase) ex).getVdsError());
            } else if (vdsExp.getCause() instanceof VDSExceptionBase) {
                getVDSReturnValue().setVdsError(((VDSExceptionBase) vdsExp.getCause()).getVdsError());
            }
        }

        if (ex instanceof VDSRecoveringException) {
            log.errorFormat("Command {0} execution failed. Error: {1}",
                    getCommandName(),
                    ExceptionUtils.getMessage(ex));
        } else {
            logException(ex);
        }
    }

    protected void setVdsNetworkError(VDSNetworkException ex) {
        getVDSReturnValue().setSucceeded(false);
        getVDSReturnValue().setExceptionString(ex.toString());
        getVDSReturnValue().setExceptionObject(ex);
        VDSError tempVar = ex.getVdsError();
        VDSError tempVar2 = new VDSError();
        tempVar2.setCode(VdcBllErrors.VDS_NETWORK_ERROR);
        tempVar2.setMessage(ex.getMessage());
        getVDSReturnValue().setVdsError((tempVar != null) ? tempVar : tempVar2);
    }

    private void logException(RuntimeException ex) {
        log.errorFormat("Command {0} execution failed. Exception: {1}", getCommandName(), ExceptionUtils.getMessage(ex));
    }

    protected String getAdditionalInformation() {
        return StringUtils.EMPTY;
    }

    protected abstract void ExecuteVDSCommand();
}
