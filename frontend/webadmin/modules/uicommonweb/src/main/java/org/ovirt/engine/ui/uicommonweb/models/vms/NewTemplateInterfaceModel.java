package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewTemplateInterfaceModel extends NewVmInterfaceModel {

    public static NewTemplateInterfaceModel createInstance(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel) {
        NewTemplateInterfaceModel instance =
                new NewTemplateInterfaceModel(vm, clusterCompatibilityVersion, vmNicList, sourceModel);
        instance.init();
        return instance;
    }

    protected NewTemplateInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel) {
        super(vm, clusterCompatibilityVersion, vmNicList, sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHashName("new_network_interface_tmps"); //$NON-NLS-1$
    }

    @Override
    protected void init() {
        super.init();
        getPlugged().setIsChangable(false);
    }

    @Override
    protected void initMAC() {
        getMAC().setIsAvailable(false);
    }

    @Override
    protected void onSaveMAC(VmNetworkInterface nicToSave) {
        nicToSave.setMacAddress(null);
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.AddVmTemplateInterface;
    }

    @Override
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmTemplateInterfaceParameters(getVm().getId(), nicToSave);
    }

}
