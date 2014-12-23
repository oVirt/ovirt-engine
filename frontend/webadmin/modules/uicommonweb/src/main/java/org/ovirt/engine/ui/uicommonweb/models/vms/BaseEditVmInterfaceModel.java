package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import java.util.List;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class BaseEditVmInterfaceModel extends VmInterfaceModel {

    VmNetworkInterface nic;

    protected BaseEditVmInterfaceModel(VmBase vm,
            VMStatus vmStatus,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        super(vm,
                vmStatus,
                dcId,
                clusterCompatibilityVersion,
                vmNicList,
                sourceModel,
                new EditProfileBehavior());
        this.nic = nic;
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        setHelpTag(HelpTag.edit_network_interface_vms);
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

        AsyncDataProvider.getInstance().getNicTypeList(getVm().getOsId(), getClusterCompatibilityVersion(), asyncQuery);
    }

    private void postNicInit() {
        initSelectedType();

        getName().setEntity(getNic().getName());
        initMAC();

        initLinked();

        initProfiles();

        // Plug should be the last one updated, cause it controls the changeability of the other editor
        getPlugged().setEntity(getNic().isPlugged());
        if (!allowPlug()) {
            getPlugged().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getMessages()
                    .nicHotPlugNotSupported(getClusterCompatibilityVersion().toString()));
        }
        getPlugged().setIsChangable(allowPlug());

        initCommands();
    }

    @Override
    protected VmNetworkInterface createBaseNic() {
        return new VmNetworkInterface(getNic());
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
        VmInterfaceType selectedNicType = VmInterfaceType.forValue(getNic().getType());
        ArrayList<VmInterfaceType> nicTypes = (ArrayList<VmInterfaceType>) getNicType().getItems();
        nicTypes = nicTypes == null ? new ArrayList<VmInterfaceType>() : nicTypes;

        if (selectedNicType == null || !nicTypes.contains(selectedNicType))
        {
            selectedNicType = AsyncDataProvider.getInstance().getDefaultNicType(nicTypes);
        }

        getNicType().setSelectedItem(selectedNicType);
    }

    @Override
    protected void initMAC() {
        getMAC().setIsChangable(false);
        getMAC().setEntity(getNic().getMacAddress());
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
}
