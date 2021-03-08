package org.ovirt.engine.core.bll.network.vm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ExternalNetworkManagerFactory;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.validator.MacAddressValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.HostProviderBindingDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoReturn;

/**
 * Activate or deactivate a virtual network interface of a VM in case it is in a valid status. If the VM is down, simply
 * update the device, if it is Up - HotPlug / HotUnPlug the virtual network interface
 */
@NonTransactiveCommandAttribute
public class ActivateDeactivateVmNicCommand<T extends ActivateDeactivateVmNicParameters> extends VmCommand<T> {

    private static List<VMStatus> ALLOWED_VM_STATES =
            Arrays.asList(VMStatus.Up, VMStatus.Down, VMStatus.ImageLocked, VMStatus.PoweringDown, VMStatus.PoweringUp);

    private static final List<VMStatus> VM_STATUSES_FOR_WHICH_HOT_PLUG_IS_REQUIRED =
            Arrays.asList(VMStatus.Up, VMStatus.PoweringUp);

    private VmDevice vmDevice;

    private VnicProfile vnicProfile;

    private Network network;

    private NetworkProviderProxy providerProxy;

    private Provider<?> provider;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private VfScheduler vfScheduler;

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    @Inject
    private HostLocking hostLocking;

    @Inject
    private VmDevicesMonitoring vmDevicesMonitoring;

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private HostProviderBindingDao hostProviderBindingDao;

    @Inject
    private ExternalNetworkManagerFactory externalNetworkManagerFactory;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private NetworkDao networkDao;

    public ActivateDeactivateVmNicCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getVmId());
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

        if (getVm().isHostedEngine() && !getVm().isManagedHostedEngine()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE);
            return false;
        }

        if (getNetwork() != null
                && managementNetworkUtil.isManagementNetwork(getNetwork().getId(), getVm().getClusterId())
                && getVm().isManagedHostedEngine()) {
            addValidationMessage(EngineMessage.DEACTIVATE_MANAGEMENT_NETWORK_FOR_HOSTED_ENGINE);
            return false;
        }

        if (!ALLOWED_VM_STATES.contains(getVm().getStatus())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
            addValidationMessage(ReplacementUtils.createSetVariableString("vmStatus", getVm().getStatus()));

            return false;
        }

        if (hotPlugVmNicRequired(getVm().getStatus())) {
            setVdsId(getVm().getRunOnVds());
            if (!isNicSupportedForPlugUnPlug()) {
                return false;
            }

            // External networks are handled by their provider, so only check if exists on host for internal networks.
            if (getNetwork() != null
                    && !getNetwork().isExternal()
                    && !isPassthrough()
                    && !networkAttachedToVds(getNetwork().getName(), getVdsId())) {
                addValidationMessage(EngineMessage.ACTIVATE_DEACTIVATE_NETWORK_NOT_IN_VDS);
                return false;
            }

            // External network linked to physical checks if the physical is attached on host
            if (getNetwork() != null
                    && getParameters().getAction().equals(PlugAction.PLUG)
                    && getNetwork().isExternal()
                    && !validate(validateExternalNetwork())) {
                return false;
            }

            if (isPassthrough() && getParameters().getAction() == PlugAction.PLUG && getVfPreallocatedForNic() == null
                    && findFreeVf() == null) {
                return failValidationCannotPlugPassthroughVnicNoSuitableVf();
            }
        }

        vmDevice = vmDeviceDao.get(new VmDeviceId(getParameters().getNic().getId(), getParameters().getVmId()));
        if (vmDevice == null) {
            addValidationMessage(EngineMessage.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (getParameters().getAction() == PlugAction.PLUG && !validate(macAvailable())) {
            return false;
        }

        return true;
    }

    private String getVfPreallocatedForNic() {
        Guid nicId = getParameters().getNic().getId();
        Map<Guid, String> vnicToVfMap = vfScheduler.getVnicToVfMap(getVmId(), getVdsId());

        if (vnicToVfMap == null) {
            return null;
        }

        return vnicToVfMap.get(nicId);
    }

    private String findFreeVf() {
        return vfScheduler.findFreeVfForVnic(getVdsId(), getNetwork(), getVmId());
    }

    private boolean failValidationCannotPlugPassthroughVnicNoSuitableVf() {
        return failValidation(EngineMessage.CANNOT_PLUG_PASSTHROUGH_VNIC_NO_SUITABLE_VF,
                String.format("$vnicName %1$s", getInterfaceName()));
    }

    private boolean isPassthrough() {
        return VmInterfaceType.pciPassthrough == VmInterfaceType.forValue(getParameters().getNic().getType());
    }

    public Network getNetwork() {
        if (getParameters().getNic().getVnicProfileId() != null && network == null) {
            vnicProfile = getVnicProfile();
            network = networkHelper.getNetworkByVnicProfile(vnicProfile);
        }

        return network;
    }

    private VnicProfile getVnicProfile() {
        if (getParameters().getNic().getVnicProfileId() != null && vnicProfile == null) {
            vnicProfile = vnicProfileDao.get(getParameters().getNic().getVnicProfileId());
        }
        return vnicProfile;
    }

    public String getInterfaceName() {

        return getParameters().getNic().getName();
    }

    public String getInterfaceType() {

        return VmInterfaceType.forValue(getParameters().getNic().getType()).getDescription();
    }

    @Override
    protected void executeVmCommand() {
        switch (getParameters().getAction()) {
        case PLUG:
            plugNic();
            break;
        case UNPLUG:
            unplugNic();
            break;
        default:
            throw new RuntimeException("Coding error: unknown enum value");
        }

        var success = handleFailoverIfNeeded();
        // In any case, the device is updated
        updateDevice();
        setSucceeded(success);
    }

    private void plugNic() {
        clearAddressIfPciSlotIsDuplicated(vmDevice);
        // HotPlug in the host is called only if the Vm is UP
        if (hotPlugVmNicRequired(getVm().getStatus())) {
            boolean externalNetworkIsPlugged = getNetwork() != null && getNetwork().isExternal();
            if (externalNetworkIsPlugged) {
                plugToExternalNetwork();
            }

            String vfToUse = null;
            try {
                if (isPassthrough()) {
                    String preallocatedVfForNic = getVfPreallocatedForNic();
                    boolean preallocatedVfExist = preallocatedVfForNic != null;
                    if (preallocatedVfExist) {
                        vfToUse = preallocatedVfForNic;
                    } else {
                        vfToUse = acquireVF();
                        if (vfToUse == null) {
                            failValidationCannotPlugPassthroughVnicNoSuitableVf();
                            return;
                        }

                        networkDeviceHelper.setVmIdOnVfs(getVdsId(), getVmId(), Collections.singleton(vfToUse));
                    }

                    vmDevice.setHostDevice(vfToUse);
                }

                if (executePlugOrUnplug(PlugAction.PLUG)) {
                    if (isPassthrough()) {
                        runInternalAction(ActionType.RefreshHost, new VdsActionParameters(getVdsId()));
                    }
                } else {
                    clearPassthroughData(vfToUse);
                }
            } catch (EngineException e) {
                if (externalNetworkIsPlugged && getParameters().isNewNic()) {
                    unplugFromExternalNetwork();
                }

                clearPassthroughData(vfToUse);
                throw e;
            }
        }
    }

    private void unplugNic() {
        if (hotPlugVmNicRequired(getVm().getStatus())) {
            if (executePlugOrUnplug(PlugAction.UNPLUG)) {
                if (isPassthrough()) {
                    clearPassthroughData(vmDevice.getHostDevice());
                    runInternalAction(ActionType.RefreshHost, new VdsActionParameters(getVdsId()));
                }
            }
        }
    }

    private boolean executePlugOrUnplug(PlugAction action) {
        VDSReturnValue vdsReturnValue = runVdsCommand(action.getvNicVdsCommandType(),
                new VmNicDeviceVDSParameters(getVdsId(),
                        getVm(),
                        getParameters().getNic(),
                        vmDevice));

        updateVmDeviceWithDataReturnedFromHost(vdsReturnValue);
        return vdsReturnValue.getSucceeded();
    }

    private void updateVmDeviceWithDataReturnedFromHost(VDSReturnValue vdsReturnValue) {
        if (vdsReturnValue.getSucceeded() && getParameters().getAction() == PlugAction.PLUG) {
            VmInfoReturn vmInfoReturn = (VmInfoReturn) vdsReturnValue.getReturnValue();
            if (vmInfoReturn.getVmInfo() == null) {
                log.warn("failed to retrieve the device dynamic properties");
                return;
            }

            Map<String, Object> vmInfo = (Map<String, Object>) vmInfoReturn.getVmInfo();
            for (Object o : (Object[]) vmInfo.get(VdsProperties.Devices)) {
                Map<String, Object> vdsmDevice = (Map<String, Object>) o;
                String reportedMacAddress = (String) vdsmDevice.get(VdsProperties.MAC_ADDR);
                if (StringUtils.equalsIgnoreCase(reportedMacAddress, getParameters().getNic().getMacAddress())) {
                    vmDevice.setAlias(StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.Alias)));
                    break;
                }
            }
        }
    }

    private String acquireVF() {
        try {
            hostLocking.acquireHostDevicesLock(getVdsId());
            return findFreeVf();
        } finally {
            hostLocking.releaseHostDevicesLock(getVdsId());
        }
    }

    private void clearPassthroughData(String vfToUse) {

        if (vfToUse != null) {
            networkDeviceHelper.setVmIdOnVfs(getVdsId(), null, Collections.singleton(vfToUse));
        }
    }

    private void clearAddressIfPciSlotIsDuplicated(VmDevice vmDeviceToHotplug) {
        if (searchForDuplicatesWithExistingVmDevices(vmDeviceToHotplug)){
            vmDeviceToHotplug.setAddress("");
        }
    }

    private boolean searchForDuplicatesWithExistingVmDevices(VmDevice vmDeviceToHotplug){
        String deviceAddress = vmDeviceToHotplug.getAddress();
        if (StringUtils.isEmpty(deviceAddress)){
            return false;
        }
        Map<String, String> addressMapToHotplug = StringMapUtils.string2Map(deviceAddress);
        List<VmDevice> allVmDevices = vmDeviceDao.getVmDeviceByVmId(getVm().getId());
        for (VmDevice vmDevice : allVmDevices) {
            if (!vmDeviceToHotplug.getId().equals(vmDevice.getId())){
                Map<String, String> deviceAddressMap = StringMapUtils.string2Map(vmDevice.getAddress());
                boolean duplicatedAddress = deviceAddressMap.equals(addressMapToHotplug);

                boolean ambiguousInterfaceState = StringUtils.isEmpty(vmDevice.getAddress()) && vmDevice.isPlugged()
                        && VmDeviceGeneralType.INTERFACE.equals(vmDevice.getType());

                if(duplicatedAddress || ambiguousInterfaceState) {
                    return true;
                }
            }
        }
        return false;
    }

    private void plugToExternalNetwork() {
        Provider provider = getProvider();
        if (provider == null) {
            throw new RuntimeException("No provider for network");
        }
        String pluginType = ((OpenstackNetworkProviderProperties) provider.getAdditionalProperties()).getPluginType();
        String hostBindingId = hostProviderBindingDao.get(getVds().getId(), pluginType);
        Map<String, String> runtimeProperties =
                getProviderProxy().allocate(getNetwork(), vnicProfile, getParameters().getNic(), getVds(),
                false, hostBindingId);

        if (runtimeProperties != null) {
            getVm().getRuntimeDeviceCustomProperties().put(vmDevice.getId(), runtimeProperties);
        }
    }

    private void unplugFromExternalNetwork() {
        externalNetworkManagerFactory.create(getParameters().getNic(), getNetwork()).deallocateIfExternal();
    }

    private Provider<?> getProvider() {
        if (provider == null ) {
            Network network = getNetwork();
            if (network != null && network.isExternal()) {
                provider = providerDao.get(network.getProvidedBy().getProviderId());
            }
        }
        return provider;
    }

    private NetworkProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            Provider<?> provider = getProvider();
            providerProxy = providerProxyFactory.create(provider);
        }

        return providerProxy;
    }

    private void updateDevice() {
        if (getParameters().getAction() == PlugAction.PLUG
                && hotPlugVmNicRequired(getVm().getStatus())) {
            VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(getVdsId(), System.nanoTime());
            change.updateVm(getVmId(), VmDevicesMonitoring.UPDATE_HASH);
            change.flush();
        } else {
            vmDevice.setPlugged(getParameters().getAction() == PlugAction.PLUG);
            vmDeviceDao.update(vmDevice);
        }
    }

    private boolean handleFailoverIfNeeded() {
        var vnicProfile = getVnicProfile();
        if (vnicProfile == null || vnicProfile.getFailoverVnicProfileId() == null || !getParameters().isWithFailover()
                || !hotPlugVmNicRequired(getVm().getStatus())) {
            return true;
        }

        var failoverId = vnicProfile.getFailoverVnicProfileId();
        var failoverDevice = VmDeviceCommonUtils.createFailoverVmDevice(failoverId, getVmId());
        var failoverNic = VmDeviceCommonUtils.createFailoverVmNic(failoverId,
                getVmId(),
                getParameters().getNic().getMacAddress());
        boolean success;
        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(getParameters().getAction().getvNicVdsCommandType(),
                    new VmNicDeviceVDSParameters(getVdsId(),
                            getVm(),
                            failoverNic,
                            failoverDevice));
            success = vdsReturnValue.getSucceeded();
        } catch (EngineException e) {
            revertAfterFailoverOperationFail();
            throw e;
        }
        if (!success) {
            // Failover plug/unplug failed, we need to revert the VF action
            revertAfterFailoverOperationFail();
        }
        return success;
    }

    private void revertAfterFailoverOperationFail() {
        switch (getParameters().getAction()) {
        case PLUG:
            getParameters().setAction(PlugAction.UNPLUG);
            unplugNic();
            break;
        case UNPLUG:
            getParameters().setAction(PlugAction.PLUG);
            plugNic();
            break;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage((getParameters().getAction() == PlugAction.PLUG) ?
                EngineMessage.VAR__ACTION__ACTIVATE : EngineMessage.VAR__ACTION__DEACTIVATE);
        addValidationMessage(EngineMessage.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().getAction() == PlugAction.PLUG) {
            return getSucceeded() ? AuditLogType.NETWORK_ACTIVATE_VM_INTERFACE_SUCCESS
                    : AuditLogType.NETWORK_ACTIVATE_VM_INTERFACE_FAILURE;
        } else {
            return getSucceeded() ? AuditLogType.NETWORK_DEACTIVATE_VM_INTERFACE_SUCCESS
                    : AuditLogType.NETWORK_DEACTIVATE_VM_INTERFACE_FAILURE;
        }
    }

    private boolean networkAttachedToVds(String networkName, Guid vdsId) {
        List<VdsNetworkInterface> listOfInterfaces = interfaceDao.getAllInterfacesForVds(vdsId);
        for (VdsNetworkInterface vdsNetworkInterface : listOfInterfaces) {
            if (networkName.equals(vdsNetworkInterface.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hotPlugVmNicRequired(VMStatus vmStatus) {
        return VM_STATUSES_FOR_WHICH_HOT_PLUG_IS_REQUIRED.contains(vmStatus);
    }

    protected ValidationResult macAvailable() {
        VmNic nic = getParameters().getNic();

        Optional<VM> optionalVm = new VmInterfaceManager().getVmWithSameMacIfDuplicateIsNotAllowed(nic, getMacPool());
        if (optionalVm.isPresent()) {
            return new ValidationResult(EngineMessage.NETWORK_MAC_ADDRESS_IN_USE,
                    ReplacementUtils.createSetVariableString(MacAddressValidator.VAR_MAC_ADDRESS, nic.getMacAddress()),
                    ReplacementUtils.createSetVariableString(MacAddressValidator.VAR_VM_NAME,
                            optionalVm.get().getName()));
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateExternalNetwork() {
        if (!getNetwork().getProvidedBy().isSetPhysicalNetworkId()) {
            return ValidationResult.VALID;
        }

        Network physicalNetwork = networkDao.get(getNetwork().getProvidedBy().getPhysicalNetworkId());

        return ValidationResult.failWith(EngineMessage.PHYSICAL_NETWORK_LINKED_TO_EXTERNAL_NOT_IN_VDS)
                .when(!networkAttachedToVds(physicalNetwork.getName(), getVdsId()));
    }
}
