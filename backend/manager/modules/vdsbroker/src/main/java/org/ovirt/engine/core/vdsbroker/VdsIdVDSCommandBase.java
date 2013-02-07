package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class VdsIdVDSCommandBase<P extends VdsIdVDSCommandParametersBase> extends VDSCommandBase<P> {
    protected VdsManager _vdsManager;

    public VdsIdVDSCommandBase(P parameters) {
        super(parameters);
        _vdsManager = ResourceManager.getInstance().GetVdsManager(parameters.getVdsId());
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
            return String.format("HostName = %1$s", getVds().getVdsName());
        } else {
            return super.getAdditionalInformation();
        }
    }

    @Override
    protected void ExecuteVDSCommand() {
        if (_vdsManager != null) {
            synchronized (_vdsManager.getLockObj()) {
                ExecuteVdsIdCommand();
            }
        } else {
            ExecuteVdsIdCommand();
        }
    }

    protected abstract void ExecuteVdsIdCommand();
}
