package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

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

    protected void getNics(final AsyncQuery<List<VmNetworkInterface>> getNicsQuery, final Guid vmId, final UnitVmModel unitVmModel) {
        AsyncDataProvider.getInstance().getNicTypeList(unitVmModel.getOSType().getSelectedItem(),
                       unitVmModel.getDataCenterWithClustersList().getSelectedItem().getCluster().getCompatibilityVersion(),
                new AsyncQuery<>(returnValue -> {
                    defaultType = AsyncDataProvider.getInstance().getDefaultNicType(returnValue);
                    supportedInterfaceTypes = returnValue;
                    AsyncDataProvider.getInstance().getVmNicList(getNicsQuery, vmId);
                }));
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
                addInterfaceResult ->
                        Frontend.getInstance().runMultipleActions(VdcActionType.UpdateVmInterface,
                        updateVnicParameters,
                        updateInterfaceResult-> Frontend.getInstance().runMultipleActions(VdcActionType.RemoveVmInterface,
                                removeVnicParameters,
                                removeInterfaceResult -> {
                                    if (isAddingNewVm) {
                                        VmOperationParameterBase reorderParams = new VmOperationParameterBase(id);
                                        Frontend.getInstance().runAction(VdcActionType.ReorderVmNics, reorderParams,
                                                reorderResult -> getCallback().vnicCreated(id, unitVmModel));
                                    } else {
                                        getCallback().vnicCreated(id, unitVmModel);
                                    }
                                },
                                this),
                        this),
                this);
    }

}
