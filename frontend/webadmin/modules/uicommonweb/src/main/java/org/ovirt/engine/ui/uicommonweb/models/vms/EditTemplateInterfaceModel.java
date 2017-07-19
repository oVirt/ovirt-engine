package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditTemplateInterfaceModel extends BaseEditVmInterfaceModel {

    public static EditTemplateInterfaceModel createInstance(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            IModel sourceModel) {
        EditTemplateInterfaceModel instance =
                new EditTemplateInterfaceModel(vm, dcId, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        instance.init();
        return instance;
    }

    protected EditTemplateInterfaceModel(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            IModel sourceModel) {
        super(vm,
                VMStatus.Down,
                dcId,
                clusterCompatibilityVersion,
                vmNicList,
                nic,
                sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        setHelpTag(HelpTag.edit_network_interface_tmps);
        setHashName("edit_network_interface_tmps"); //$NON-NLS-1$
    }

    @Override
    protected void init() {
        super.init();
        getPlugged().setIsChangeable(false);
        getNetworkFilterParameterListModel().setIsAvailable(false);
    }

    @Override
    protected void initMAC() {
        getMAC().setIsAvailable(false);
    }

    @Override
    protected void onSaveMAC(VmNetworkInterface nicToSave) {
        nicToSave.setMacAddress(getNic().getMacAddress());
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.UpdateVmTemplateInterface;
    }

    @Override
    protected ActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmTemplateInterfaceParameters(getVm().getId(), nicToSave);
    }
}
