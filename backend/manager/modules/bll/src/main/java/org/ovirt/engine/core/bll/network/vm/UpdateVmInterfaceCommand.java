package org.ovirt.engine.core.bll.network.vm;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmInterfaceCommand<T extends AddVmInterfaceParameters> extends AbstractVmInterfaceCommand<T> {

    private VmNetworkInterface oldIface;
    private VmDevice oldVmDevice;
    private boolean macShouldBeChanged;
    private RequiredAction requiredAction = null;

    public UpdateVmInterfaceCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    private RequiredAction getRequiredAction() {
        if (requiredAction == null) {
            if (!oldIface.isPlugged() && getInterface().isPlugged()) {
                requiredAction = RequiredAction.PLUG;
            } else if (oldIface.isPlugged() && !getInterface().isPlugged()) {
                requiredAction = RequiredAction.UNPLUG;
            } else if (liveActionRequired() && propertiesRequiringVmUpdateDeviceWereUpdated()) {
                requiredAction = RequiredAction.UPDATE_VM_DEVICE;
            }
        }

        return requiredAction;
    }

    private boolean liveActionRequired() {
        return oldIface.isPlugged() && getInterface().isPlugged() && getVm().getStatus() == VMStatus.Up;
    }

    @Override
    protected void executeVmCommand() {
        addCustomValue("InterfaceType",
                (VmInterfaceType.forValue(getInterface().getType()).getDescription()).toString());

        boolean succeeded = false;
        boolean macAddedToPool = false;
        try {
            if (macShouldBeChanged) {
                macAddedToPool = addMacToPool(getMacAddress());
            }

            getInterface().setSpeed(VmInterfaceType.forValue(getInterface().getType()).getSpeed());

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    getCompensationContext().snapshotEntity(oldIface);
                    getCompensationContext().snapshotEntity(oldVmDevice);
                    getVmNetworkInterfaceDao().update(getInterface());
                    oldVmDevice.setCustomProperties(getInterface().getCustomProperties());
                    getDbFacade().getVmDeviceDao().update(oldVmDevice);
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            succeeded = updateHost();
        } finally {
            setSucceeded(succeeded);
            if (macAddedToPool) {
                if (succeeded) {
                    MacPoolManager.getInstance().freeMac(oldIface.getMacAddress());
                } else {
                    MacPoolManager.getInstance().freeMac(getMacAddress());
                }
            }
        }
    }

    private boolean updateHost() {
        if (getVm().getStatus() == VMStatus.Up) {
            setVdsId(getVm().getRunOnVds().getValue());
        }

        if (getRequiredAction() != null){
            switch (getRequiredAction()) {
            case PLUG: {
                return activateOrDeactivateNic(getInterface(), PlugAction.PLUG);
            }
            case UNPLUG: {
                return activateOrDeactivateNic(oldIface, PlugAction.UNPLUG);
            }
            case UPDATE_VM_DEVICE: {
                runVdsCommand(VDSCommandType.UpdateVmInterface,
                        new VmNicDeviceVDSParameters(getVdsId(),
                                getVm(),
                                getVmNetworkInterfaceDao().get(getInterface().getId()),
                                oldVmDevice));
                break;
            }
            }
        }
        return true;
    }

    private boolean propertiesRequiringVmUpdateDeviceWereUpdated() {
        return (!StringUtils.equals(oldIface.getNetworkName(), getNetworkName()))
                || oldIface.isLinked() != getInterface().isLinked()
                || !ObjectUtils.objectsEqual(oldIface.getCustomProperties(), getInterface().getCustomProperties());
    }

    @Override
    protected boolean canDoAction() {

        if (!updateVmNicAllowed(getVm().getStatus())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN_UP);
            return false;
        }

        oldVmDevice =
                getVmDeviceDao().get(new VmDeviceId(getInterface().getId(), getVmId()));
        List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
        oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getInterface().getId());
            }
        });

        if (oldIface == null || oldVmDevice == null) {
            addCanDoActionMessage(VdcBllMessages.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (!StringUtils.equals(oldIface.getName(), getInterfaceName()) && !uniqueInterfaceName(interfaces)) {
                return false;
        }

        // check that not exceeded PCI and IDE limit
        java.util.ArrayList<VmNetworkInterface> allInterfaces = new java.util.ArrayList<VmNetworkInterface>(interfaces);
        allInterfaces.remove(oldIface);
        allInterfaces.add(getInterface());
        VmStatic vm = getVmStaticDAO().get(getVmId());

        if (!pciAndIdeWithinLimit(vm, allInterfaces)) {
            return false;
        }

        if (!validate(vmTemplateEmpty())) {
            return false;
        }

        UpdateVmNicValidator nicValidator =
                new UpdateVmNicValidator(getInterface(), getVm().getVdsGroupCompatibilityVersion());
        if (!validate(nicValidator.unplugPlugNotRequired())
                || !validate(nicValidator.linkedCorrectly())
                || !validate(nicValidator.networkNameValid())
                || !validate(nicValidator.hotUpdatePossible())
                || !validate(nicValidator.networkProvidedForPortMirroring())) {
            return false;
        }

        Network network = getNetworkFromDb(vm.getVdsGroupId(), getNetworkName());
        if (getNetworkName() != null) {

            // check that the network exists in current cluster
            if (network == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
                return false;
            } else if (!validate(nicValidator.portMirroringNotSetIfExternalNetwork(network))) {
                return false;
            }
        }

        if (getRequiredAction() == RequiredAction.UPDATE_VM_DEVICE) {
            Network oldNetwork = getNetworkFromDb(vm.getVdsGroupId(), oldIface.getNetworkName());
            if (!validate(nicValidator.hotUpdateDoneWithInternalNetwork(oldNetwork, network))) {
                return false;
            }
        }

        macShouldBeChanged = !StringUtils.equals(oldIface.getMacAddress(), getMacAddress());
        if (macShouldBeChanged && (!validate(macAddressValid()) || !validate(macAvailable()))) {
            return false;
        }

        if (!nicValidator.validateCustomProperties(getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateVmNic.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (oldIface.isLinked() != getInterface().isLinked()) {
                AuditLogType customValue =
                        getInterface().isLinked() ? AuditLogType.NETWORK_UPDATE_VM_INTERFACE_LINK_UP
                                : AuditLogType.NETWORK_UPDATE_VM_INTERFACE_LINK_DOWN;
                addCustomValue("LinkState", AuditLogDirector.getMessage(customValue));
            } else {
                addCustomValue("LinkState", " ");
            }
            return AuditLogType.NETWORK_UPDATE_VM_INTERFACE;
        }

        return AuditLogType.NETWORK_UPDATE_VM_INTERFACE_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getInterface() != null && StringUtils.isNotEmpty(getNetworkName()) && getVm() != null) {

            VmNetworkInterface iface = getVmNetworkInterfaceDao().get(getInterface().getId());
            if (iface != null) {
                Network network = getNetworkDAO().getByNameAndCluster(getNetworkName(), getVm().getVdsGroupId());

                if (getInterface().isPortMirroring()
                        && (isNetworkChanged(iface) || !iface.isPortMirroring())) {
                    permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                            VdcObjectType.Network,
                            ActionGroup.PORT_MIRRORING));
                } else {
                    // If the vNic's network is changed, the user should have permission for using the new network
                    if (isNetworkChanged(iface)) {
                        permissionList.add(new PermissionSubject(network == null ? null : network.getId(),
                                VdcObjectType.Network,
                                getActionType().getActionGroup()));
                    }
                }
            }
        }
        return permissionList;
    }

    private boolean isNetworkChanged(VmNetworkInterface iface) {
        return !getNetworkName().equals(iface.getNetworkName());
    }

    private boolean updateVmNicAllowed(VMStatus vmStatus) {
        return vmStatus == VMStatus.Up || vmStatus == VMStatus.Down;
    }

    private enum RequiredAction {
        PLUG,
        UNPLUG,
        UPDATE_VM_DEVICE
    }

    /**
     * Internal validator that adds checks specific to this class, but uses info from the {@link VmNicValidator}.
     */
    private class UpdateVmNicValidator extends VmNicValidator {

        public UpdateVmNicValidator(VmNetworkInterface nic, Version version) {
            super(nic, version);
        }

        /**
         * @return An error if hot updated is needed, and either network linking is not supported or the NIC has port
         *         mirroring set.
         */
        public ValidationResult hotUpdatePossible() {
            if (getRequiredAction() == RequiredAction.UPDATE_VM_DEVICE) {
                if (!FeatureSupported.networkLinking(version)) {
                    return new ValidationResult(VdcBllMessages.HOT_VM_INTERFACE_UPDATE_IS_NOT_SUPPORTED,
                            clusterVersion());
                } else if (nic.isPortMirroring()) {
                    return new ValidationResult(VdcBllMessages.CANNOT_PERFOM_HOT_UPDATE_WITH_PORT_MIRRORING);
                }
            }

            return ValidationResult.VALID;
        }

        /**
         * @return An error if live action is required and the properties requiring the NIC to be unplugged and then
         *         plugged again have changed.
         */
        public ValidationResult unplugPlugNotRequired() {
            return liveActionRequired() && propertiesRequiringUnplugPlugWereUpdated()
                    ? new ValidationResult(VdcBllMessages.CANNOT_PERFOM_HOT_UPDATE) : ValidationResult.VALID;
        }

        private boolean propertiesRequiringUnplugPlugWereUpdated() {
            return (!oldIface.getType().equals(getInterface().getType()))
                    || (!oldIface.getMacAddress().equals(getMacAddress()))
                    || (oldIface.isPortMirroring() != getInterface().isPortMirroring());
        }

        /**
         * @param oldNetwork
         *            The old network (can be <code>null</code>).
         * @param newNetwork
         *            The new network (can be <code>null</code>).
         * @return An error if either the old or new network is an external network, otherwise hot update is allowed.
         */
        public ValidationResult hotUpdateDoneWithInternalNetwork(Network oldNetwork, Network newNetwork) {
            return (oldNetwork == null || oldNetwork.getProvidedBy() == null)
                    && (newNetwork == null || newNetwork.getProvidedBy() == null)
                    ? ValidationResult.VALID
                    : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REWIRED);
        }
    }
}
