package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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

    private VmNic oldIface;
    private VmDevice oldVmDevice;
    private VnicProfile vnicProfile;
    private boolean macShouldBeChanged;
    private RequiredAction requiredAction = null;

    public UpdateVmInterfaceCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    private RequiredAction getRequiredAction() {
        if (requiredAction == null) {
            if (!oldVmDevice.getIsPlugged() && getInterface().isPlugged()) {
                requiredAction = RequiredAction.PLUG;
            } else if (oldVmDevice.getIsPlugged() && !getInterface().isPlugged()) {
                requiredAction = RequiredAction.UNPLUG;
            } else if (liveActionRequired() && propertiesRequiringVmUpdateDeviceWereUpdated()) {
                requiredAction = RequiredAction.UPDATE_VM_DEVICE;
            }
        }

        return requiredAction;
    }

    private boolean liveActionRequired() {
        return oldVmDevice.getIsPlugged() && getInterface().isPlugged() && getVm().getStatus() == VMStatus.Up;
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
                    getVmNicDao().update(getInterface());
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
            setVdsId(getVm().getRunOnVds());
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
                                getVmNicDao().get(getInterface().getId()),
                                oldVmDevice));
                break;
            }
            }
        }
        return true;
    }

    private boolean propertiesRequiringVmUpdateDeviceWereUpdated() {
        return !ObjectUtils.equals(oldIface.getVnicProfileId(), getInterface().getVnicProfileId())
                || oldIface.isLinked() != getInterface().isLinked();
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }

        if (!updateVnicForBackwardCompatibility()) {
            return false;
        }

        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!updateVmNicAllowed(getVm().getStatus())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN_UP);
            return false;
        }

        oldVmDevice = getVmDeviceDao().get(new VmDeviceId(getInterface().getId(), getVmId()));
        List<VmNic> interfaces = getVmNicDao().getAllForVm(getVmId());
        oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNic>() {
            @Override
            public boolean eval(VmNic i) {
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
        List<VmNic> allInterfaces = new ArrayList<>(interfaces);
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
                new UpdateVmNicValidator(getInterface(), getVm().getVdsGroupCompatibilityVersion(), getVm().getOs());
        if (!validate(nicValidator.unplugPlugNotRequired())
                || !validate(nicValidator.linkedCorrectly())
                || !validate(nicValidator.isCompatibleWithOs())
                || !validate(nicValidator.emptyNetworkValid())
                || !validate(nicValidator.hotUpdatePossible())
                || !validate(nicValidator.profileValid(vm.getVdsGroupId()))) {
            return false;
        }

        Network network = null;
        if (getRequiredAction() == RequiredAction.UPDATE_VM_DEVICE) {
            Network oldNetwork = NetworkHelper.getNetworkByVnicProfileId(oldIface.getVnicProfileId());
            if (!validate(nicValidator.hotUpdateDoneWithInternalNetwork(oldNetwork, network))) {
                return false;
            }
        }

        macShouldBeChanged = !StringUtils.equals(oldIface.getMacAddress(), getMacAddress());
        if (macShouldBeChanged && (!validate(macAddressValid()) || !validate(macAvailable()))) {
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

        if (getInterface() != null && getInterface().getVnicProfileId() != null && getVm() != null) {
            VmNic oldNic = getVmNicDao().get(getInterface().getId());
            if (oldNic == null || isVnicProfileChanged(oldNic, getInterface())) {
                permissionList.add(new PermissionSubject(getInterface().getVnicProfileId(),
                        VdcObjectType.VnicProfile,
                        getActionType().getActionGroup()));
            }
        }

        return permissionList;
    }

    private VnicProfile getVnicProfile() {
        if (getInterface().getVnicProfileId() == null) {
            return null;
        }

        if (vnicProfile == null) {
            vnicProfile = getVnicProfileDao().get(getInterface().getVnicProfileId());
        }
        return vnicProfile;
    }

    private boolean isVnicProfileChanged(VmNic oldNic, VmNic newNic) {
        return !ObjectUtils.equals(oldNic.getVnicProfileId(), newNic.getVnicProfileId());
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

        public UpdateVmNicValidator(VmNic nic, Version version, int osId) {
            super(nic, version, osId);
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
                } else if (getVnicProfile().isPortMirroring()) {
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
                    || (!oldIface.getMacAddress().equals(getMacAddress()));
        }

        /**
         * @param oldNetwork
         *            The old network (can be <code>null</code>).
         * @param newNetwork
         *            The new network (can be <code>null</code>).
         * @return An error if either the old or new network is an external network, otherwise hot update is allowed.
         */
        public ValidationResult hotUpdateDoneWithInternalNetwork(Network oldNetwork, Network newNetwork) {
            return (oldNetwork == null || !oldNetwork.isExternal())
                    && (newNetwork == null || !newNetwork.isExternal())
                    ? ValidationResult.VALID
                    : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REWIRED);
        }
    }
}
