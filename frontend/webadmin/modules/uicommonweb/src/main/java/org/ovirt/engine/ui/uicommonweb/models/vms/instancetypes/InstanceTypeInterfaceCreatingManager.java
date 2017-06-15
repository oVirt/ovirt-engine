package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseInterfaceCreatingManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class InstanceTypeInterfaceCreatingManager extends BaseInterfaceCreatingManager {

    public InstanceTypeInterfaceCreatingManager(PostVnicCreatedCallback callback) {
        super(callback);
    }

    @Override
    protected ActionParametersBase createAddInterfaceParameter(Guid id, VmNetworkInterface editedVnic) {
        return new AddVmTemplateInterfaceParameters(id, editedVnic);
    }

    @Override
    protected ActionParametersBase createRemoveInterfaceParameter(Guid id, Guid nicId) {
        return new RemoveVmTemplateInterfaceParameters(id, nicId);
    }

    @Override
    protected void getNics(final AsyncQuery<List<VmNetworkInterface>> getNicsQuery, final Guid vmId, final UnitVmModel unitVmModel) {
        AsyncDataProvider.getInstance().getTemplateNicList(getNicsQuery, vmId);
    }

    @Override
    protected void doNicManipulation(
            final ArrayList<ActionParametersBase> createVnicParameters,
            final ArrayList<ActionParametersBase> updateVnicParameters,
            final ArrayList<ActionParametersBase> removeVnicParameters,
            final boolean isAddingNewVm,
            final Guid id,
            final UnitVmModel unitVmModel) {
        Frontend.getInstance().runMultipleActions(ActionType.AddVmTemplateInterface,
                createVnicParameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        Frontend.getInstance().runMultipleActions(ActionType.UpdateVmTemplateInterface,
                                updateVnicParameters,
                                new IFrontendActionAsyncCallback() {

                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {
                                        Frontend.getInstance().runMultipleActions(ActionType.RemoveVmTemplateInterface,
                                                removeVnicParameters,
                                                r -> {
                                                    // no need to reorder - it will be done for the VMs when creating from instance type
                                                    getCallback().vnicCreated(id, unitVmModel);
                                                }, this);
                                    }
                                }, this);
                    }
                }, this);
    }

}
