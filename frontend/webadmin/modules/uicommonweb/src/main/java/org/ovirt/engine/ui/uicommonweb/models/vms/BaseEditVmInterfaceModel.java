package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import java.util.List;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class BaseEditVmInterfaceModel extends VmInterfaceModel {

    VmNetworkInterface nic;

    protected BaseEditVmInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        super(vm, clusterCompatibilityVersion, vmNicList, sourceModel, new EditNetworkBehavior());
        this.nic = nic;
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        setHashName("edit_network_interface_vms"); //$NON-NLS-1$
    }

    protected VmNetworkInterface getNic() {
        return nic;
    }

    @Override
    protected void init() {
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                getNicType().setItems((List<VmInterfaceType>) returnValue);
                postNicInit();
            }
        };

        AsyncDataProvider.getNicTypeList(getVm().getOsId(), getClusterCompatibilityVersion(), asyncQuery);
    }

    private void postNicInit() {
        initSelectedType();

        getName().setEntity(getNic().getName());
        initMAC();

        initLinked();

        initPortMirroring();
        initNetworks();

        initCustomPropertySheet();

        // Plug should be the last one updated, cause it controls the changeability of the other editor
        getPlugged().setEntity(getNic().isPlugged());
        if (!hotPlugSupported) {
            getPlugged().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getMessages()
                    .hotPlugNotSupported(getClusterCompatibilityVersion().toString()));
        }
        getPlugged().setIsChangable(hotPlugSupported);

        initCommands();
    }

    @Override
    protected VmNetworkInterface createBaseNic() {
        return (VmNetworkInterface) Cloner.clone(getNic());
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.UpdateVmInterface;
    }

    @Override
    protected String getDefaultMacAddress() {
        return (getNic()).getMacAddress();
    }

    @Override
    protected void initSelectedType() {
        Integer selectedNicType = getNic().getType();
        ArrayList<VmInterfaceType> nicTypes = (ArrayList<VmInterfaceType>) getNicType().getItems();
        nicTypes = nicTypes == null ? new ArrayList<VmInterfaceType>() : nicTypes;

        if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
        {
            selectedNicType = AsyncDataProvider.getDefaultNicType().getValue();
        }

        getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));
    }

    @Override
    protected void initMAC() {
        getMAC().setIsChangable(false);
        getMAC().setEntity(getNic().getMacAddress());
    }

    @Override
    protected void initPortMirroring() {
        getPortMirroring().setIsChangable(isPortMirroringSupported());
        getPortMirroring().setEntity(getNic().isPortMirroring());
    }

    @Override
    protected void initLinked() {
        if (hotUpdateSupported) {
            getLinked().setEntity(getNic().isLinked());
        } else {
            getLinked().setEntity(true);
            getLinked().setIsAvailable(false);
        }
    }

    @Override
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmInterfaceParameters(getVm().getId(), nicToSave);
    }

    @Override
    protected void setCustomPropertyFromVm() {
        getCustomPropertySheet().setEntity(KeyValueModel
                .convertProperties(getNic().getCustomProperties()));
    }
}
