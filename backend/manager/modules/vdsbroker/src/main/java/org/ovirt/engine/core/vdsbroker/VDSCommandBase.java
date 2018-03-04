package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.VdcCommandBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public abstract class VDSCommandBase<P extends VDSParametersBase> extends VdcCommandBase {
    private P _parameters;
    private boolean async;

    @Inject
    protected ResourceManager resourceManager;

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
                !addInfo.isEmpty() ? addInfo + "," : StringUtils.EMPTY,
                getParameters() != null ? getParameters().toString() : "null");
    }

    @Override
    protected void executeCommand() {
        try {
            if (isAsync()) {
                executeCommandAsynchronously();
                return;
            }
            // creating ReturnValue object since execute can be called more than once (failover)
            // and we want returnValue clean from last run.
            _returnValue = new VDSReturnValue();
            getVDSReturnValue().setSucceeded(true);
            executeVDSCommand();
        } catch (RuntimeException ex) {
            setVdsRuntimeErrorAndReport(ex);
        }
    }

    protected void setVdsRuntimeErrorAndReport(RuntimeException ex) {
        setVdsRuntimeError(ex);
        logException(ex);
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
    }

    private void logException(RuntimeException ex) {
        if (ex.getMessage() == null || !ex.getMessage().contains("Policy reset")) {
            log.error("Command '{}' execution failed: {}", this, ex.getMessage());
            log.debug("Exception", ex);
        }
    }

    protected String getAdditionalInformation() {
        return StringUtils.EMPTY;
    }

    protected abstract void executeVDSCommand();

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return this.async;
    }

    /**
     * When providing asynchronous execution of vds command this method need to
     * be overridden.
     */
    protected void executeCommandAsynchronously() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not provide setAsyncResult implementation");
    }
}
