package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ExternalNetworkManagerFactory;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.snapshots.CountMacUsageDifference;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateVmNic;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.MessageBundler;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmInterfaceCommand<T extends AddVmInterfaceParameters> extends AbstractVmInterfaceCommand<T> {

    private VmNic oldIface;
    private VmDevice oldVmDevice;
    private boolean macShouldBeChanged;
    private RequiredAction requiredAction = null;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private VnicProfileDao vnicProfileDao;

    @Inject
    private ExternalNetworkManagerFactory externalNetworkManagerFactory;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private SnapshotsManager snapshotsManager;

    private CountMacUsageDifference countMacUsageDifference;
    private List<VmNic> vmInterfaces;
    private List<VmNicFilterParameter> oldFilterParameters;

    public UpdateVmInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmId(parameters.getVmId());
    }

    public UpdateVmInterfaceCommand(Guid commandId) {
        super(commandId);
    }

    private RequiredAction getRequiredAction() {
        if (requiredAction == null) {
            if (!oldVmDevice.isPlugged() && getInterface().isPlugged()) {
                requiredAction = RequiredAction.PLUG;
            } else if (oldVmDevice.isPlugged() && !getInterface().isPlugged()) {
                requiredAction = RequiredAction.UNPLUG;
            } else if (liveActionRequired() && propertiesRequiringVmUpdateDeviceWereUpdated()) {
                requiredAction = RequiredAction.UPDATE_VM_DEVICE;
            }
        }

        return requiredAction;
    }

    private boolean liveActionRequired() {
        return oldVmDevice.isPlugged() && getInterface().isPlugged() && getVm().getStatus() == VMStatus.Up;
    }

    @Override
    protected void executeVmCommand() {
        addCustomValue("InterfaceType",
                VmInterfaceType.forValue(getInterface().getType()).getDescription().toString());

        boolean succeeded = false;
        boolean macAddedToPool = false;

        try {
            if (isVnicProfileChanged(oldIface, getInterface())) {
                Network newNetwork = networkHelper.getNetworkByVnicProfileId(getInterface().getVnicProfileId());
                Network oldNetwork = networkHelper.getNetworkByVnicProfileId(oldIface.getVnicProfileId());
                if (!Objects.equals(oldNetwork, newNetwork)) {
                    externalNetworkManagerFactory.create(oldIface).deallocateIfExternal();
                }
            }

            macAddedToPool = allocateMacFromRequest();

            if (mustChangeAddress(oldIface.getType(), getInterface().getType())) {
                vmDeviceDao.clearDeviceAddress(getInterface().getId());
            }

            getInterface().setSpeed(VmInterfaceType.forValue(getInterface().getType()).getSpeed());
            getInterface().setSynced(getRequiredAction() == RequiredAction.UNPLUG || oldIface.isSynced());

            TransactionSupport.executeInNewTransaction(() -> {
                bumpVmVersion();
                updatePassthoughDeviceIfNeeded();
                getCompensationContext().snapshotEntity(oldIface);
                vmNicDao.update(getInterface());
                saveNetworkFilterParameters();
                getCompensationContext().stateChanged();
                return null;
            });

            succeeded = updateHost();
        } finally {
            setSucceeded(succeeded);
            macPoolCleanupAfterExecution(macAddedToPool);
        }
    }

    boolean allocateMacFromRequest() {
        return macShouldBeChanged && macShouldBeAddedIntoPool() && addMacToPool(getMacAddress());
    }

    private boolean macShouldBeAddedIntoPool() {
        return countMacUsageDifference.usageDifference(getMacAddress()) > 0;
    }

    void macPoolCleanupAfterExecution(boolean macAddedToPool) {
        if (!macShouldBeChanged) {
            return;
        }

        MacPool macPool = getMacPool();
        if (getSucceeded()) {    // command succeeded, actions are not going to be reverted.

            String macToRelease = oldIface.getMacAddress();
            boolean canReleaseMac = new MacIsNotReservedInSnapshotAndCanBeReleased().macCanBeReleased(macToRelease, getVm(), countMacUsageDifference);
            if (canReleaseMac) {
                macPool.freeMac(macToRelease);
            }
        } else {
            // command did not succeed, newly acquired MAC has to be released again,
            // since this command ins't transactive
            if (macAddedToPool) {   // new mac was added to MAC pool.
                macPool.freeMac(getMacAddress());
            }
        }
    }

    private boolean updateHost() {
        if (getVm().getStatus() == VMStatus.Up) {
            setVdsId(getVm().getRunOnVds());
        }

        if (getRequiredAction() != null){
            switch (getRequiredAction()) {
            case PLUG:
                return activateOrDeactivateExistingNic(getInterface(), PlugAction.PLUG);
            case UNPLUG:
                return activateOrDeactivateExistingNic(oldIface, PlugAction.UNPLUG);
            case UPDATE_VM_DEVICE:
                runVdsCommand(VDSCommandType.UpdateVmInterface,
                        new VmNicDeviceVDSParameters(getVdsId(),
                                getVm(),
                                vmNicDao.get(getInterface().getId()),
                                oldVmDevice));
                break;
            }
        }
        return true;
    }

    private boolean activateOrDeactivateExistingNic(VmNic nic, PlugAction plugAction) {
        return activateOrDeactivateNic(nic, plugAction, false);
    }

    private boolean propertiesRequiringVmUpdateDeviceWereUpdated() {
        return !Objects.equals(oldIface.getVnicProfileId(), getInterface().getVnicProfileId())
                || oldIface.isLinked() != getInterface().isLinked()
                || !Objects.equals(oldFilterParameters, getParameters().getFilterParameters());
    }

    private void updatePassthoughDeviceIfNeeded() {
        if (mustChangeVmDevice()) {
            getCompensationContext().snapshotEntity(oldVmDevice);
            oldVmDevice.setDevice(getInterface().isPassthrough() ? VmDeviceType.HOST_DEVICE.getName()
                    : VmDeviceType.BRIDGE.getName());
            vmDeviceDao.update(oldVmDevice);
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!validate(vmStatusLegal(getVm().getStatus()))) {
            return false;
        }

        initVmData();

        if (oldIface == null || oldVmDevice == null) {
            addValidationMessage(EngineMessage.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (!updateVnicForBackwardCompatibility(oldIface)) {
            return false;
        }

        if (!StringUtils.equals(oldIface.getName(), getInterfaceName()) && !uniqueInterfaceName(vmInterfaces)) {
            return false;
        }

        // check that not exceeded PCI and IDE limit
        List<VmNic> allInterfaces = new ArrayList<>(vmInterfaces);
        allInterfaces.remove(oldIface);
        allInterfaces.add(getInterface());

        if (!pciAndIdeWithinLimit(getVm(), allInterfaces)) {
            return false;
        }

        if (!validate(parentEntityIdMatches())) {
            return false;
        }

        UpdateVmNicValidator nicValidator =
                new UpdateVmNicValidator(getInterface(), getVm().getClusterCompatibilityVersion(), getVm().getOs());
        if (!validate(nicValidator.unplugPlugNotRequired())
                || !validate(nicValidator.isNetworkSupportedByClusterSwitchType(getCluster()))
                || !validate(nicValidator.isCompatibleWithOs())
                || !validate(nicValidator.hotUpdatePossible())
                || !validate(nicValidator.profileValid(getVm().getClusterId()))
                || !validate(nicValidator.canVnicWithExternalNetworkBePlugged())
                || !validate(nicValidator.typeMatchesProfile())
                || !validate(nicValidator.passthroughIsLinked())
                || !validate(nicValidator.validateProfileNotEmptyForHostedEngineVm(getVm()))
                || !validate(nicValidator.isFailoverInSupportedClusterVersion())) {
            return false;
        }

        Network network = networkHelper.getNetworkByVnicProfileId(getInterface().getVnicProfileId());
        if (getRequiredAction() == RequiredAction.UPDATE_VM_DEVICE) {
            Network oldNetwork = networkHelper.getNetworkByVnicProfileId(oldIface.getVnicProfileId());
            if (!validate(nicValidator.hotUpdateDoneWithInternalNetwork(oldNetwork, network))
                    || !validate(nicValidator.networkExistsOnHost(network))) {
                return false;
            }
        }

        initMacPoolData();

        if (macShouldBeChanged && macShouldBeAddedIntoPool() && !validate(macAvailable())) {
            return false;
        }

        return true;
    }

    private ValidationResult parentEntityIdMatches() {
        return getInterface().getVmId().equals(getVmId()) ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.NETWORK_INTERFACE_TEMPLATE_CANNOT_BE_SET);
    }

    void initMacPoolData() {
        macShouldBeChanged = !StringUtils.equals(oldIface.getMacAddress(), getMacAddress());

        countMacUsageDifference = new CountMacUsageDifference(snapshotsManager.macsInStatelessSnapshot(getVmId()),
                macsWhichShouldExistAfterCommandIsFinished());
    }

    private Stream<String> macsWhichShouldExistAfterCommandIsFinished() {
        List<String> currentMacs =
                getVm().getInterfaces().stream().map(VmNic::getMacAddress).collect(Collectors.toList());

        //we cannot use filter here, because there can be duplicate mac addresses.
        currentMacs.remove(oldIface.getMacAddress());
        currentMacs.add(getMacAddress());

        return currentMacs.stream();
    }

    void initVmData() {
        oldVmDevice = vmDeviceDao.get(new VmDeviceId(getInterface().getId(), getVmId()));
        vmInterfaces = vmNicDao.getAllForVm(getVmId());
        oldIface = vmInterfaces.stream().filter(i -> i.getId().equals(getInterface().getId())).findFirst().orElse(null);
        oldFilterParameters = vmNicFilterParameterDao.getAllForVmNic(getInterface().getId());
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
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (oldIface.isLinked() != getInterface().isLinked()) {
                AuditLogType customValue =
                        getInterface().isLinked() ? AuditLogType.NETWORK_UPDATE_VM_INTERFACE_LINK_UP
                                : AuditLogType.NETWORK_UPDATE_VM_INTERFACE_LINK_DOWN;
                addCustomValue("LinkState", MessageBundler.getMessage(customValue));
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
            VmNic oldNic = vmNicDao.get(getInterface().getId());
            if (oldNic == null || isVnicProfileChanged(oldNic, getInterface())) {
                permissionList.add(new PermissionSubject(getInterface().getVnicProfileId(),
                        VdcObjectType.VnicProfile,
                        getActionType().getActionGroup()));
            }
        }

        return permissionList;
    }

    /**
     * Check if address must be changed after change NIC type
     * @param oldType - Old nic type
     * @param newType - New nic type
     */
    private boolean mustChangeAddress (int oldType, int newType) {
        int spaprVlanType = VmInterfaceType.spaprVlan.getValue();
        return wasTypeChangedToOrFromTestedType(oldType, newType, spaprVlanType);
    }

    private boolean mustChangeVmDevice() {
        int passthroughType = VmInterfaceType.pciPassthrough.getValue();
        return wasTypeChangedToOrFromTestedType(oldIface.getType(), getInterface().getType(), passthroughType);
    }

    private boolean wasTypeChangedToOrFromTestedType(int oldType, int newType, int testedType) {
        return oldType == testedType ^ newType == testedType;
    }

    private boolean isVnicProfileChanged(VmNic oldNic, VmNic newNic) {
        return !Objects.equals(oldNic.getVnicProfileId(), newNic.getVnicProfileId());
    }

    protected void saveNetworkFilterParameters() {
        List<VmNicFilterParameter> newParameters = getParameters().getFilterParameters();
        if (newParameters != null) {
            for (VmNicFilterParameter parameter : newParameters) {
                boolean toUpdate = oldFilterParameters.stream()
                        .anyMatch(saved -> Objects.equals(parameter.getId(), saved.getId()));
                if (toUpdate) {
                    runInternalAction(ActionType.UpdateVmNicFilterParameter,
                            new VmNicFilterParameterParameters(getParameters().getVmId(), parameter),
                            cloneContextWithNoCleanupCompensation());
                } else {
                    parameter.setVmInterfaceId(getInterface().getId());
                    var actionReturnValue = runInternalAction(ActionType.AddVmNicFilterParameter,
                            new VmNicFilterParameterParameters(getParameters().getVmId(), parameter),
                            cloneContextWithNoCleanupCompensation());
                    getReturnValue().setActionReturnValue(actionReturnValue.getActionReturnValue());
                }
            }
            for (VmNicFilterParameter parameter : oldFilterParameters) {
                if (newParameters.stream()
                        .noneMatch(newParameter -> Objects.equals(parameter.getId(), newParameter.getId()))) {
                    runInternalAction(ActionType.RemoveVmNicFilterParameter,
                            new RemoveVmNicFilterParameterParameters(getParameters().getVmId(), parameter.getId()),
                            cloneContextWithNoCleanupCompensation());
                }
            }
        }
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

        public ValidationResult networkExistsOnHost(Network network) {
            if (network == null) {
                return ValidationResult.VALID;
            }

            Guid vdsId = vmDynamicDao.get(nic.getVmId()).getRunOnVds();
            List<VdsNetworkInterface> hostNics = interfaceDao.getAllInterfacesForVds(vdsId);
            for (VdsNetworkInterface hostNic : hostNics) {
                if (network.getName().equals(hostNic.getNetworkName())) {
                    return ValidationResult.VALID;
                }
            }

            return new ValidationResult(EngineMessage.ACTIVATE_DEACTIVATE_NETWORK_NOT_IN_VDS);
        }

        /**
         * @return An error if hot updated is needed, and either network linking is not supported or the NIC has port
         *         mirroring set.
         */
        public ValidationResult hotUpdatePossible() {
            if (getRequiredAction() == RequiredAction.UPDATE_VM_DEVICE) {
                if (portMirroringEnabled(getInterface().getVnicProfileId())
                        || portMirroringEnabled(oldIface.getVnicProfileId())) {
                    return new ValidationResult(EngineMessage.CANNOT_PERFORM_HOT_UPDATE_WITH_PORT_MIRRORING);
                }
            }

            return ValidationResult.VALID;
        }

        private boolean portMirroringEnabled(Guid profileId) {
            VnicProfile vnicProfile = profileId == null ? null : vnicProfileDao.get(profileId);
            return vnicProfile != null && vnicProfile.isPortMirroring();
        }

        /**
         * @return An error if live action is required and the properties requiring the NIC to be unplugged and then
         *         plugged again have changed.
         */
        public ValidationResult unplugPlugNotRequired() {
            return liveActionRequired() && propertiesRequiringUnplugPlugWereUpdated()
                    ? new ValidationResult(EngineMessage.CANNOT_PERFORM_HOT_UPDATE) : ValidationResult.VALID;
        }

        private boolean propertiesRequiringUnplugPlugWereUpdated() {
            return !oldIface.getType().equals(getInterface().getType())
                    || oldIface.isPassthrough()
                    || !oldIface.getMacAddress().equals(getMacAddress());
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
                            : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_REWIRED);
        }

        public ValidationResult canVnicWithExternalNetworkBePlugged() {
            return ValidationResult.failWith(EngineMessage.PLUGGED_UNLINKED_VM_INTERFACE_WITH_EXTERNAL_NETWORK_IS_NOT_SUPPORTED)
                    .when(RequiredAction.PLUG == getRequiredAction()
                          && !nic.isLinked()
                          && isVnicAttachedToExternalNetwork());
        }

        private boolean isVnicAttachedToExternalNetwork() {
            final Network network = getNetwork();
            return network != null && network.isExternal();
        }
    }
}
