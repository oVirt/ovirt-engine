package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public abstract class NewQosModel<T extends QosBase, P extends QosParametersModel<T>> extends QosModel<T, P> {
    public NewQosModel(Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
    }

    @Override
    protected void executeSave() {
        final QosParametersBase<T> parameters = getParameters();
        parameters.setQos(getQos());
        Frontend.getInstance().runAction(getVdcAction(), parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result1) {
                VdcReturnValueBase retVal = result1.getReturnValue();
                boolean succeeded = false;
                if (retVal != null && retVal.getSucceeded()) {
                    succeeded = true;
                    getQos().setId((Guid) retVal.getActionReturnValue());
                }
                postSaveAction(succeeded);
            }
        });
    }

    protected abstract VdcActionType getVdcAction();

    protected abstract QosParametersBase<T> getParameters();
}
