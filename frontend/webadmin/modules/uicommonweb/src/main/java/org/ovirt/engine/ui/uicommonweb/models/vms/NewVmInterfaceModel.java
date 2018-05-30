package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmInterfaceModel extends VmInterfaceModel {

    public static NewVmInterfaceModel createInstance(VmBase vm,
            VMStatus vmStatus,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        NewVmInterfaceModel instance = new NewVmInterfaceModel(vm,
                vmStatus,
                dcId,
                clusterCompatibilityVersion,
                vmNicList,
                sourceModel);
        instance.init();
        return instance;
    }

    protected NewVmInterfaceModel(VmBase vm,
            VMStatus vmStatus,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        super(vm,
                vmStatus,
                dcId,
                clusterCompatibilityVersion,
                vmNicList, sourceModel,
                new NewProfileBehavior());
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHelpTag(HelpTag.new_network_interface_vms);
        setHashName("new_network_interface_vms"); //$NON-NLS-1$
    }

    @Override
    protected void init() {
        getNetworkFilterParameterListModel().setIsAvailable(true);
        AsyncDataProvider.getInstance().getNicTypeList(getVm().getOsId(), getClusterCompatibilityVersion(), new AsyncQuery<>(
                returnValue -> {
                    setSupportedVnicTypes(returnValue);
                    postNicInit();
                }));
    }

    private void postNicInit() {
        getName().setEntity(AsyncDataProvider.getInstance().getNewNicName(getVmNicList()));
        initMAC();

        getPlugged().setIsChangeable(allowPlug());
        getPlugged().setEntity(allowPlug());
        if (!allowPlug()) {
            getPlugged().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getMessages()
                    .nicHotPlugNotSupported(getClusterCompatibilityVersion().toString()));
        }

        initLinked();

        initProfiles();
        initCommands();
    }

    @Override
    protected VmNetworkInterface createBaseNic() {
        return new VmNetworkInterface();
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.AddVmInterface;
    }

    @Override
    protected String getDefaultMacAddress() {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected void initSelectedType() {
        final VmInterfaceType defaultNicType = getDefaultNicTypeByProfile();

        if (getNicType().getItems() == null) {
            getNicType().setItems(getSupportedVnicTypes(), defaultNicType);
        } else {
            getNicType().setSelectedItem(defaultNicType);
        }
    }

    @Override
    protected void initMAC() {
        getMAC().setIsChangeable(false);

    }

    @Override
    protected void initLinked() {
        getLinked().setEntity(true);
    }

    @Override
    protected ActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        AddVmInterfaceParameters parameters = new AddVmInterfaceParameters(getVm().getId(), nicToSave);
        parameters.setFilterParameters(getNetworkFilterParameterListModel().getItems()
                .stream().map(x -> x.flush()).collect(Collectors.toList()));
        return parameters;
    }

    protected VmNetworkInterface getNic() {
        // no nic for new
        return null;
    }
}
