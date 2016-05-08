package org.ovirt.engine.core.bll.network.vm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.network.ExternalNetworkManager;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

/**
 * Activate or deactivate a virtual network interface of a VM in case it is in a valid status. If the VM is down, simply
 * update the device, if it is Up - HotPlug / HotUnPlug the virtual network interface
 */
@NonTransactiveCommandAttribute
public class ActivateDeactivateVmNicCommand<T extends ActivateDeactivateVmNicParameters> extends VmCommand<T> {

    private VmDevice vmDevice;

    private VnicProfile vnicProfile;

    private Network network;

    private NetworkProviderProxy providerProxy;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private VfScheduler vfScheduler;

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    @Inject
    private HostDeviceManager hostDeviceManager;

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

        if (!activateDeactivateVmNicAllowed(getVm().getStatus())) {
            addValidationMessage(EngineMessage.ACTIVATE_DEACTIVATE_NIC_VM_STATUS_ILLEGAL);
            return false;
        }

        // HotPlug in the host needs to be called only if the Vm is UP
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

            if (isPassthrough()) {
                if (!checkSriovHotPlugSupported()) {
                    return false;
                }

                if (getParameters().getAction() == PlugAction.PLUG) {
                    String vfToUse = updateFreeVf();
                    if (vfToUse == null) {
                        return false;
                    }
                }
            }
        }

        vmDevice = getVmDeviceDao().get(new VmDeviceId(getParameters().getNic().getId(), getParameters().getVmId()));
        if (vmDevice == null) {
            addValidationMessage(EngineMessage.VM_INTERFACE_NOT_EXIST);
            return false;
        }

        if (getParameters().getAction() == PlugAction.PLUG && !validate(macAvailable())) {
            return false;
        }

        return true;
    }

    private String updateFreeVf() {
        String vfToUse = vfScheduler.findFreeVfForVnic(getVdsId(), getNetwork(), getVmId());

        if (vfToUse == null) {
            failValidation(EngineMessage.CANNOT_PLUG_PASSTHROUGH_VNIC_NO_SUITABLE_VF,
                    String.format("$vnicName %1$s", getInterfaceName()));
        }

        return vfToUse;
    }

    private boolean isPassthrough() {
        return VmInterfaceType.pciPassthrough == VmInterfaceType.forValue(getParameters().getNic().getType());
    }

    public Network getNetwork() {
        if (getParameters().getNic().getVnicProfileId() != null && network == null) {
            vnicProfile = getDbFacade().getVnicProfileDao().get(getParameters().getNic().getVnicProfileId());
            network = NetworkHelper.getNetworkByVnicProfile(vnicProfile);
        }

        return network;
    }

    public String getInterfaceName() {

        return getParameters().getNic().getName();
    }

    public String getInterfaceType() {

        return VmInterfaceType.forValue(getParameters().getNic().getType()).getDescription();
    }

    @Override
    protected void executeVmCommand() {
        boolean isNicToBePlugged = getParameters().getAction() == PlugAction.PLUG;
        if (isNicToBePlugged){
            clearAddressIfPciSlotIsDuplicated(vmDevice);
        }
        // HotPlug in the host is called only if the Vm is UP
        if (hotPlugVmNicRequired(getVm().getStatus())) {
            boolean externalNetworkIsPlugged = isNicToBePlugged
                    && getNetwork() != null
                    && getNetwork().isExternal();
            if (externalNetworkIsPlugged) {
                plugToExternalNetwork();
            }

            String vfToUse = null;
            try {
                if (isNicToBePlugged) {

                    if (isPassthrough()) {
                        try {
                            hostDeviceManager.acquireHostDevicesLock(getVdsId());
                            vfToUse = updateFreeVf();
                            if (vfToUse == null) {
                                return;
                            }

                            networkDeviceHelper.setVmIdOnVfs(getVdsId(),
                                    getVmId(),
                                    Collections.singleton(vfToUse));
                            vmDevice.setHostDevice(vfToUse);
                        } finally {
                            hostDeviceManager.releaseHostDevicesLock(getVdsId());
                        }
                    }
                }
                VDSReturnValue returnValue = runVdsCommand(getParameters().getAction().getCommandType(),
                    new VmNicDeviceVDSParameters(getVdsId(),
                            getVm(),
                            getParameters().getNic(),
                            vmDevice));

                if (returnValue.getSucceeded()) {
                    boolean passthroughHotPlug = vfToUse != null;
                    boolean passthroughHotUnplug = isPassthrough() && getParameters().getAction() == PlugAction.UNPLUG;

                    if (passthroughHotUnplug) {
                        networkDeviceHelper.setVmIdOnVfs(getVdsId(),
                                null,
                                new HashSet<>(Arrays.asList(vmDevice.getHostDevice())));
                    }

                    if (passthroughHotPlug || passthroughHotUnplug) {
                        runInternalAction(VdcActionType.RefreshHost, new VdsActionParameters(getVdsId()));
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
        // In any case, the device is updated
        TransactionSupport.executeInNewTransaction(updateDevice());
        setSucceeded(true);
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
        Map<String, String> addressMapToHotplug = XmlRpcStringUtils.string2Map(deviceAddress);
        List<VmDevice> allVmDevices = getVmDeviceDao().getVmDeviceByVmId(getVm().getId());
        for (VmDevice vmDevice : allVmDevices) {
            if (!vmDeviceToHotplug.getId().equals(vmDevice.getId())){
                Map<String, String> deviceAddressMap = XmlRpcStringUtils.string2Map(vmDevice.getAddress());
                if(deviceAddressMap.equals(addressMapToHotplug)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void plugToExternalNetwork() {
        Map<String, String> runtimeProperties =
                getProviderProxy().allocate(getNetwork(), vnicProfile, getParameters().getNic(), getVds());

        if (runtimeProperties != null) {
            getVm().getRuntimeDeviceCustomProperties().put(vmDevice.getId(), runtimeProperties);
        }
    }

    private void unplugFromExternalNetwork() {
        new ExternalNetworkManager(getParameters().getNic(), getNetwork()).deallocateIfExternal();
    }

    private NetworkProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            Provider<?> provider = getDbFacade().getProviderDao().get(getNetwork().getProvidedBy().getProviderId());
            providerProxy = ProviderProxyFactory.getInstance().create(provider);
        }

        return providerProxy;
    }

    private TransactionMethod<Void> updateDevice() {
        return () -> {
            vmDevice.setIsPlugged(getParameters().getAction() == PlugAction.PLUG);
            getVmDeviceDao().update(vmDevice);
            VmDeviceUtils.updateBootOrder(getVm().getId());
            return null;
        };
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

    private boolean activateDeactivateVmNicAllowed(VMStatus vmStatus) {
        return vmStatus == VMStatus.Up || vmStatus == VMStatus.Down || vmStatus == VMStatus.ImageLocked;
    }

    private boolean networkAttachedToVds(String networkName, Guid vdsId) {
        List<VdsNetworkInterface> listOfInterfaces = getInterfaceDao().getAllInterfacesForVds(vdsId);
        for (VdsNetworkInterface vdsNetworkInterface : listOfInterfaces) {
            if (networkName.equals(vdsNetworkInterface.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected InterfaceDao getInterfaceDao() {
        return getDbFacade().getInterfaceDao();
    }

    private boolean hotPlugVmNicRequired(VMStatus vmStatus) {
        return vmStatus == VMStatus.Up;
    }

    protected ValidationResult macAvailable() {
        MacPool macPool = getMacPool();
        Boolean allowDupMacs = macPool.isDuplicateMacAddressesAllowed();
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(macPool);
        if (allowDupMacs || !vmInterfaceManager.existsPluggedInterfaceWithSameMac(getParameters().getNic())) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.NETWORK_MAC_ADDRESS_IN_USE);
        }
    }

    protected boolean checkSriovHotPlugSupported() {
        if (!FeatureSupported.sriovHotPlugSupported(getVm().getClusterCompatibilityVersion())) {
            return failValidation(EngineMessage.HOT_PLUG_UNPLUG_PASSTHROUGH_VNIC_NOT_SUPPORTED);
        }
        return true;
    }
}
