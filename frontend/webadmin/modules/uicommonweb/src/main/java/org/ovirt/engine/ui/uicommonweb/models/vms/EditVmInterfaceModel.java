package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class EditVmInterfaceModel extends BaseEditVmInterfaceModel {

    private static final String ON_APPROVE_COMMAND = "ON_APPROVE"; //$NON-NLS-1$
    private static final String ABORT_COMMAMD = "ABORT"; //$NON-NLS-1$
    private final VM vm;

    public static EditVmInterfaceModel createInstance(VmBase vmStatic, VM vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            IModel sourceModel) {
        EditVmInterfaceModel instance =
                new EditVmInterfaceModel(vmStatic, vm, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        instance.init();
        return instance;
    }

    protected EditVmInterfaceModel(VmBase vmStatic, VM vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            IModel sourceModel) {
        super(vmStatic,
                vm.getStatus(),
                vm.getStoragePoolId(),
                clusterCompatibilityVersion,
                vmNicList,
                nic,
                sourceModel);
        this.vm = vm;
    }

    @Override
    protected void init() {
        getNetworkFilterParameterListModel().setIsAvailable(true);
        super.init();
    }

    protected void onPlugChange() {
        if (!isVmUp()) {
            return;
        }

        Boolean plug = isPluggedBeforeAndAfterEdit();

        getNicType().setIsChangeable(!plug);
        getEnableMac().setIsChangeable(!plug);
        getMAC().setIsChangeable(getEnableMac().getEntity() && !plug);

        if (plug) {
            getNicType().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotTypeUpdateNotPossible());
            getEnableMac().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotMacUpdateNotPossible());

            initSelectedType();
            getEnableMac().setEntity(false);
            initMAC();

        }

        updateProfileChangability();
        updateLinkChangability();
    }

    @Override
    protected void updateLinkChangability() {
        super.updateLinkChangability();
        if (!getLinked().getIsChangable()) {
            return;
        }

        boolean isPlugged = isPluggedBeforeAndAfterEdit();

        if (isVmUp() && isPlugged && selectedNetworkExternal()) {
            getLinked().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotLinkStateUpdateNotSupportedExternalNetworks());
            getLinked().setIsChangeable(false);
            initLinked();
        }
    }

    @Override
    protected void updateProfileChangability() {
        super.updateProfileChangability();
        if (!getProfile().getIsChangable()) {
            return;
        }

        boolean isPlugged = isPluggedBeforeAndAfterEdit();

        if (isVmUp() && isPlugged) {
            if (selectedNetworkExternal()) {
                getProfile().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .hotNetworkUpdateNotSupportedExternalNetworks());
            } else {
                return;
            }

            getProfile().setIsChangeable(false);
            getProfileBehavior().initSelectedProfile(getProfile(), getNic());
        }
    }

    boolean isVmUp() {
        return VMStatus.Up.equals(vm.getStatus());
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (sender == getPlugged()) {
            PropertyChangedEventArgs propArgs = (PropertyChangedEventArgs) args;
            if (propArgs.propertyName.equals("Entity")) { //$NON-NLS-1$
                onPlugChange();
            }
        }
    }

    private boolean isPluggedBeforeAndAfterEdit() {
        return getNic().isPlugged() && getPlugged().getEntity();
    }

    private void confirmSave() {
        // Check if the nic was unplugged
        if (getNic().isPlugged() && !getPlugged().getEntity()) {
            ConfirmationModel model = new ConfirmationModel();
            model.setTitle(ConstantsManager.getInstance().getConstants().unplugVnicTitle());
            model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantUnplugVnicMsg());
            model.setHashName("unplug_vnic"); //$NON-NLS-1$
            getSourceModel().setConfirmWindow(model);

            UICommand approveCommand = UICommand.createDefaultOkUiCommand(ON_APPROVE_COMMAND, this);
            model.getCommands().add(approveCommand);

            UICommand cancel = UICommand.createCancelUiCommand(ABORT_COMMAMD, this); //$NON-NLS-1$
            model.getCommands().add(cancel);
        } else {
            onSave();
        }
    }

    private void abort() {
        getSourceModel().setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        if (ON_SAVE_COMMAND.equals(command.getName())) {
            confirmSave();
        } else if (ON_APPROVE_COMMAND.equals(command.getName())) {
            abort();
            onSave();
        } else if (ABORT_COMMAMD.equals(command.getName())) {
            abort();
        } else {
            super.executeCommand(command);
        }
    }
}
