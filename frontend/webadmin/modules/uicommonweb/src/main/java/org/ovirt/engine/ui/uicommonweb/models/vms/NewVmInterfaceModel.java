package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmInterfaceModel extends VmInterfaceModel {

    public static NewVmInterfaceModel createInstance(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel){
        NewVmInterfaceModel instance = new NewVmInterfaceModel(vm, clusterCompatibilityVersion, vmNicList, sourceModel);
        instance.init();
        return instance;
    }

    protected NewVmInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel) {
        super(vm, clusterCompatibilityVersion, vmNicList, sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHashName("new_network_interface_vms"); //$NON-NLS-1$
    }

    @Override
    protected void init() {
        String newNicName = AsyncDataProvider.GetNewNicName(getVmNicList());
        getNicType().setItems(AsyncDataProvider.GetNicTypeList(getVm().getOs(), false));
        getNicType().setSelectedItem(AsyncDataProvider.GetDefaultNicType(getVm().getOs()));
        getName().setEntity(newNicName);
        initMAC();

        if (!hotPlugSupported) {
            getPlugged().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .hotPlugNotSupported(getClusterCompatibilityVersion().toString()));
        }
        getPlugged().setIsChangable(hotPlugSupported);
        getPlugged().setEntity(true);

        if (hotUpdateSupported) {
            getLinked().setEntity(true);
        } else {
            getLinked().setEntity(true);
            getLinked().setIsAvailable(false);
        }

        initPortMirroring();
        initNetworks();
        initCommands();
    }

    @Override
    protected VmNetworkInterface createBaseNic() {
        return new VmNetworkInterface();
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.AddVmInterface;
    }

    @Override
    protected String getDefaultMacAddress() {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected void initSelectedNetwork(ArrayList<Network> networks) {
        getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
    }

    @Override
    protected void initMAC() {
        getMAC().setIsChangable(false);

    }

    @Override
    protected void initPortMirroring() {
        getPortMirroring().setIsChangable(isPortMirroringSupported());
        getPortMirroring().setEntity(false);
    }

    @Override
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmInterfaceParameters(getVm().getId(), nicToSave);
    }
}
