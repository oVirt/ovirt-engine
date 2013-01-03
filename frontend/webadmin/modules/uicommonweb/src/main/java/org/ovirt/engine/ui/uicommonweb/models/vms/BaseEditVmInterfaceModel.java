package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class BaseEditVmInterfaceModel extends VmInterfaceModel {

    VmNetworkInterface nic;

    protected BaseEditVmInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        super(vm, clusterCompatibilityVersion, vmNicList, sourceModel);
        this.nic = nic;
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        setHashName("edit_network_interface_vms"); //$NON-NLS-1$
    }

    protected VmNetworkInterface getNic() {
        return nic;
    }

    @Override
    protected void init() {
        Integer selectedNicType = getNic().getType();
        ArrayList<VmInterfaceType> nicTypes =
                AsyncDataProvider.GetNicTypeList(getVm().getos(),
                        VmInterfaceType.forValue(selectedNicType) == VmInterfaceType.rtl8139_pv);
        getNicType().setItems(nicTypes);

        if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
        {
            selectedNicType = AsyncDataProvider.GetDefaultNicType(getVm().getos()).getValue();
        }

        getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));

        getName().setEntity(getNic().getName());
        initMAC();

        if (hotUpdateSupported) {
            getLinked().setEntity(getNic().isLinked());
        } else {
            getLinked().setEntity(true);
            getLinked().setIsAvailable(false);
        }

        initPortMirroring();
        initNetworks();

        // Plug should be the last one updated, cause it controls the changeability of the other editor
        getPlugged().setEntity(getNic().isPlugged());
        if (!hotPlugSupported) {
            getPlugged().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
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
    protected void initSelectedNetwork(ArrayList<Network> networks) {
        for (Network a : networks)
        {
            String networkName = a == null ? null : a.getName();
            if (StringHelper.stringsEqual(networkName, getNic().getNetworkName()))
            {
                getNetwork().setSelectedItem(a);
                return;
            }
        }

        // In some cases, like importVm the network can be deleted from the nic.
        // In these cases, the network can be null even if NetworkLinking is not supported.
        // If the user doesn't set the network, when he'll try to run the VM or update/hotPlug the nic he will get a
        // canDo.
        if (getNic().getNetworkName() == null) {
            getNetwork().setSelectedItem(null);
        }
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
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmInterfaceParameters(getVm().getId(), nicToSave);
    }
}
