package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmPoolToAdElementParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("VmPoolName") })
public abstract class VmPoolToAdGroupBaseCommand<T extends VmPoolToAdElementParameters> extends
        AdGroupsHandlingCommandBase<T> {
    private VmPool mVmPool;
    private String mVmPoolName;

    public VmPoolToAdGroupBaseCommand(T parameters) {
        super(parameters);
    }

    public String getVmPoolName() {
        if (mVmPoolName == null && mVmPool == null) {
            mVmPool = DbFacade.getInstance().getVmPoolDao().get(getVmPoolId());
            if (mVmPool != null) {
                mVmPoolName = mVmPool.getvm_pool_name();
            }
        }
        return mVmPoolName;
    }

    protected Guid getVmPoolId() {
        return getParameters().getVmPoolId();
    }
}
