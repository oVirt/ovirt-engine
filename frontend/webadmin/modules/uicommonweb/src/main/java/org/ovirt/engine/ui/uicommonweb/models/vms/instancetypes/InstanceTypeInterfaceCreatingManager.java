package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
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
    protected VdcActionParametersBase createAddInterfaceParameter(Guid id, VmNetworkInterface editedVnic) {
        return new AddVmTemplateInterfaceParameters(id, editedVnic);
    }

    @Override
    protected VdcActionParametersBase createRemoveInterfaceParameter(Guid id, Guid nicId) {
        return new RemoveVmTemplateInterfaceParameters(id, nicId);
    }

    @Override
    protected void getNics(final AsyncQuery getNicsQuery, final Guid vmId, final UnitVmModel unitVmModel) {
        AsyncDataProvider.getInstance().getTemplateNicList(getNicsQuery, vmId);
    }

    @Override
    protected void doNicManipulation(
            final ArrayList<VdcActionParametersBase> createVnicParameters,
            final ArrayList<VdcActionParametersBase> updateVnicParameters,
            final ArrayList<VdcActionParametersBase> removeVnicParameters,
            final boolean isAddingNewVm,
            final Guid id,
            final UnitVmModel unitVmModel) {
        Frontend.getInstance().runMultipleActions(VdcActionType.AddVmTemplateInterface,
                createVnicParameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        Frontend.getInstance().runMultipleActions(VdcActionType.UpdateVmTemplateInterface,
                                updateVnicParameters,
                                new IFrontendActionAsyncCallback() {

                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {
                                        Frontend.getInstance().runMultipleActions(VdcActionType.RemoveVmTemplateInterface,
                                                removeVnicParameters,
                                                new IFrontendActionAsyncCallback() {

                                                    @Override
                                                    public void executed(FrontendActionAsyncResult result) {
                                                        // no need to reorder - it will be done for the VMs when creating from instance type
                                                        getCallback().vnicCreated(id, unitVmModel);
                                                    }
                                                }, this);
                                    }
                                }, this);
                    }
                }, this);
    }

}
