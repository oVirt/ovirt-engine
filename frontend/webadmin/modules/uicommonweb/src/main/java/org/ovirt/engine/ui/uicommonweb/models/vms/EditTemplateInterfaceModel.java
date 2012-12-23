package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditTemplateInterfaceModel extends BaseEditVmInterfaceModel {

    public static EditTemplateInterfaceModel createInstance(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        EditTemplateInterfaceModel instance =
                new EditTemplateInterfaceModel(vm, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        instance.init();
        return instance;
    }

    protected EditTemplateInterfaceModel(VmBase vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        super(vm, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        setHashName("edit_network_interface_tmps"); //$NON-NLS-1$
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
    protected void initPortMirroring() {
        getPortMirroring().setIsAvailable(false);
    }

    @Override
    protected void onSaveMAC(VmNetworkInterface nicToSave) {
        nicToSave.setMacAddress(getNic().getMacAddress());
    }

    @Override
    protected void onSavePortMirroring(VmNetworkInterface nicToSave) {
        nicToSave.setPortMirroring(getNic().isPortMirroring());
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.UpdateVmTemplateInterface;
    }

    @Override
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmTemplateInterfaceParameters(getVm().getId(), nicToSave);
    }
}
