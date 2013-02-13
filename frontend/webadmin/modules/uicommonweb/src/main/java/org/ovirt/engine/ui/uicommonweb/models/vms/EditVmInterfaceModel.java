package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class EditVmInterfaceModel extends BaseEditVmInterfaceModel {

    private final VM vm;

    public static EditVmInterfaceModel createInstance(VmBase vmStatic, VM vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        EditVmInterfaceModel instance =
                new EditVmInterfaceModel(vmStatic, vm, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        instance.init();
        return instance;
    }

    protected EditVmInterfaceModel(VmBase vmStatic, VM vm,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            VmNetworkInterface nic,
            EntityModel sourceModel) {
        super(vmStatic, clusterCompatibilityVersion, vmNicList, nic, sourceModel);
        this.vm = vm;
    }

    protected void onPlugChange() {
        if (!isVmUp()) {
            return;
        }

        Boolean plug = isPluggedBeforeAndAfterEdit();

        if (plug) {
            getNicType().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotTypeUpdateNotPossible());
            getEnableMac().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotMacUpdateNotPossible());
            getPortMirroring().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants()
                    .hotPortMirroringUpdateNotSupported());

            initSelectedType();
            getEnableMac().setEntity(false);
            initMAC();
            initPortMirroring();

        }

        getNicType().setIsChangable(!plug);
        getEnableMac().setIsChangable(!plug);
        getMAC().setIsChangable((Boolean) getEnableMac().getEntity() && !plug);
        getPortMirroring().setIsChangable(isPortMirroringSupported() && !plug);

        updateNetworkChangability();
        updateLinkChangability();
    }

    @Override
    protected void updateLinkChangability() {
        super.updateLinkChangability();
        if (!getLinked().getIsChangable()) {
            return;
        }

        boolean isPlugged = isPluggedBeforeAndAfterEdit();
        boolean isPortMirroring = (Boolean) getPortMirroring().getEntity();

        if (isVmUp() && hotUpdateSupported) {
            if (isPlugged && isPortMirroring) {
                getLinked().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .hotLinkStateUpdateNotSupportedWithPortMirroring());
                getLinked().setIsChangable(false);
                initLinked();
                return;
            }
        }
    }

    @Override
    protected void updateNetworkChangability() {
        super.updateNetworkChangability();
        if (!getNetwork().getIsChangable()) {
            return;
        }

        boolean isPlugged = isPluggedBeforeAndAfterEdit();
        boolean isPortMirroring = (Boolean) getPortMirroring().getEntity();

        if (isVmUp() && hotUpdateSupported) {
            if (isPlugged && isPortMirroring) {
                getNetwork().setChangeProhibitionReason(ConstantsManager.getInstance()
                                .getConstants()
                                .hotNetworkUpdateNotSupportedWithPortMirroring());
                getNetwork().setIsChangable(false);
                initSelectedNetwork();
                return;
            }
        } else if (isVmUp() && isPlugged) {
            getNetwork().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getMessages()
                    .hotNetworkUpdateNotSupported(getClusterCompatibilityVersion().toString()));
            getNetwork().setIsChangable(false);
            initSelectedNetwork();
            return;
        }
    }

    boolean isVmUp() {
        return VMStatus.Up.equals(vm.getStatus());
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (sender == getPlugged())
        {
            PropertyChangedEventArgs propArgs = (PropertyChangedEventArgs) args;
            if (propArgs.PropertyName.equals("Entity")) { //$NON-NLS-1$
                onPlugChange();
            }
        }
    }

    private boolean isPluggedBeforeAndAfterEdit() {
        return getNic().isPlugged() && (Boolean) getPlugged().getEntity();
    }
}
