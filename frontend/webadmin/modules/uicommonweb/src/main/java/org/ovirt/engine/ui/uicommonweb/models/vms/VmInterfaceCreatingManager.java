package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class VmInterfaceCreatingManager extends BaseInterfaceCreatingManager {

    public VmInterfaceCreatingManager(PostVnicCreatedCallback callback) {
        super(callback);
    }

    @Override
    protected VdcActionParametersBase createAddInterfaceParameter(Guid id, VmNetworkInterface editedVnic) {
        editedVnic.setVmTemplateId(null);
        return new AddVmInterfaceParameters(id, editedVnic);
    }

    @Override
    protected VdcActionParametersBase createRemoveInterfaceParameter(Guid id, Guid nicId) {
        return new RemoveVmInterfaceParameters(id, nicId);
    }

    protected void getNics(final AsyncQuery getNicsQuery, final Guid vmId, final UnitVmModel unitVmModel) {
        AsyncQuery osInfoQuery = new AsyncQuery(new INewAsyncCallback() {
               @Override
               public void onSuccess(Object model, Object returnValue) {
                   defaultType = AsyncDataProvider.getInstance().getDefaultNicType((Collection<VmInterfaceType>) returnValue);
                       supportedInterfaceTypes = (Collection<VmInterfaceType>) returnValue;
                       AsyncDataProvider.getInstance().getVmNicList(getNicsQuery, vmId);
                   }
           });
       AsyncDataProvider.getInstance().getNicTypeList(unitVmModel.getOSType().getSelectedItem(),
                       unitVmModel.getDataCenterWithClustersList().getSelectedItem().getCluster().getCompatibilityVersion(),
                       osInfoQuery);
    }

    @Override
    protected void doNicManipulation(
            final ArrayList<VdcActionParametersBase> createVnicParameters,
            final ArrayList<VdcActionParametersBase> updateVnicParameters,
            final ArrayList<VdcActionParametersBase> removeVnicParameters,
            final boolean isAddingNewVm,
            final Guid id,
            final UnitVmModel unitVmModel) {
        Frontend.getInstance().runMultipleActions(VdcActionType.AddVmInterface,
                createVnicParameters,
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        Frontend.getInstance().runMultipleActions(VdcActionType.UpdateVmInterface,
                                updateVnicParameters,
                                new IFrontendActionAsyncCallback() {

                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {
                                        Frontend.getInstance().runMultipleActions(VdcActionType.RemoveVmInterface,
                                                removeVnicParameters,
                                                new IFrontendActionAsyncCallback() {

                                                    @Override
                                                    public void executed(FrontendActionAsyncResult result) {
                                                        if (isAddingNewVm) {
                                                            VmOperationParameterBase reorderParams = new VmOperationParameterBase(id);
                                                            Frontend.getInstance().runAction(VdcActionType.ReorderVmNics, reorderParams, new IFrontendActionAsyncCallback() {
                                                                public void executed(FrontendActionAsyncResult result) {
                                                                    getCallback().vnicCreated(id, unitVmModel);
                                                                }
                                                            });
                                                        } else {
                                                            getCallback().vnicCreated(id, unitVmModel);
                                                        }
                                                    }
                                                }, this);
                                    }
                                }, this);
                    }
                }, this);
    }

}
