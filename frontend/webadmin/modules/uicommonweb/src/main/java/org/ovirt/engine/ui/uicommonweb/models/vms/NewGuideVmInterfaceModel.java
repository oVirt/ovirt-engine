package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewGuideVmInterfaceModel extends NewVmInterfaceModel {

    public static NewGuideVmInterfaceModel createInstance(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel) {
        NewGuideVmInterfaceModel instance = new NewGuideVmInterfaceModel(vm, dcId, clusterCompatibilityVersion, vmNicList, sourceModel);
        instance.init();
        return instance;
    }

    protected NewGuideVmInterfaceModel(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            EntityModel sourceModel) {
        super(vm, dcId, clusterCompatibilityVersion, vmNicList, sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHashName("new_network_interface_vms_guide"); //$NON-NLS-1$
    }

    @Override
    public void postOnSave() {
        super.postOnSave();
        getSourceModel().postAction();
    }

    @Override
    public VmGuideModel getSourceModel() {
        return (VmGuideModel) super.getSourceModel();
    }

    @Override
    protected void cancel() {
        super.cancel();
        getSourceModel().resetData();
    }

}
