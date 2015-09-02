package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class VdsIdVDSCommandBase<P extends VdsIdVDSCommandParametersBase> extends VDSCommandBase<P> {
    protected VdsManager _vdsManager;

    public VdsIdVDSCommandBase(P parameters, boolean newHost) {
        super(parameters);
        if (!newHost) {
            _vdsManager = ResourceManager.getInstance().getVdsManager(parameters.getVdsId());
        }
    }

    public VdsIdVDSCommandBase(P parameters) {
        this(parameters, false);
    }

    protected Guid getVdsId() {
        return getParameters().getVdsId();
    }

    private VDS _vds;

    protected VDS getVds() {
        if (_vds == null) {
            _vds = DbFacade.getInstance().getVdsDao().get(getVdsId());
        }
        return _vds;
    }

    @Override
    protected String getAdditionalInformation() {
        if (getVds() != null) {
            return String.format("HostName = %1$s", getVds().getName());
        } else {
            return super.getAdditionalInformation();
        }
    }

    @Override
    protected void executeVDSCommand() {
        if (_vdsManager != null) {
            synchronized (_vdsManager.getLockObj()) {
                executeVdsIdCommand();
            }
        } else {
            executeVdsIdCommand();
        }
    }

    protected abstract void executeVdsIdCommand();
}
