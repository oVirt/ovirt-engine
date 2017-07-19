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

public class NewTemplateInterfaceModel extends NewVmInterfaceModel {

    public static NewTemplateInterfaceModel createInstance(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        NewTemplateInterfaceModel instance =
                new NewTemplateInterfaceModel(vm, dcId, clusterCompatibilityVersion, vmNicList, sourceModel);
        instance.init();
        return instance;
    }

    protected NewTemplateInterfaceModel(VmBase vm,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        super(vm,
                VMStatus.Down,
                dcId,
                clusterCompatibilityVersion,
                vmNicList,
                sourceModel);
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHelpTag(HelpTag.new_network_interface_tmps);
        setHashName("new_network_interface_tmps"); //$NON-NLS-1$
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
        nicToSave.setMacAddress(null);
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.AddVmTemplateInterface;
    }

    @Override
    protected ActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmTemplateInterfaceParameters(getVm().getId(), nicToSave);
    }

}
