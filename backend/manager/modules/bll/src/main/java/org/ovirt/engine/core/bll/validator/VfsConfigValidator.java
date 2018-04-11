package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.di.Injector;

public class VfsConfigValidator {

    private Guid nicId;
    private VdsNetworkInterface nic;
    private HostNicVfsConfig oldVfsConfig;

    static final String NIC_NAME_REPLACEMENT = "$nicName %s";
    static final String NUM_OF_VFS_REPLACEMENT = "$numOfVfs %d";
    static final String MAX_NUM_OF_VFS_REPLACEMENT = "$maxNumOfVfs %d";
    static final String NETWORK_NAME_REPLACEMENT = "$networkName %s";
    static final String NETWORK_ID_REPLACEMENT = "$networkId %s";
    static final String LABEL_REPLACEMENT = "$label %s";

    public VfsConfigValidator(Guid nicId, HostNicVfsConfig oldVfsConfig) {
        this.nicId = nicId;
        this.oldVfsConfig = oldVfsConfig;
    }

    /**
     * @return An error iff a nic with the specified id doesn't exist.
     */
    public ValidationResult nicExists() {
        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST)
                .when(getNic() == null);
    }

    /**
     * @return An error iff the nic is not SR-IOV enabled
     */
    public ValidationResult nicSriovEnabled() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED,
                getNicNameReplacement())
                .when(oldVfsConfig == null);
    }

    /**
     * @return An error iff there are non-free VFs of the nic
     */
    public ValidationResult allVfsAreFree(NetworkDeviceHelper networkDeviceHelper) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED,
                getNicNameReplacement())
                .unless(networkDeviceHelper.areAllVfsFree(getNic()));
    }

    /**
     * @return An error iff <code>numOfVfs</code> is bigger than the <code>vfsConfig.maxNumOfVfs</code>
     */
    public ValidationResult numOfVfsInValidRange(int numOfVfs) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE,
                getNicNameReplacement(),
                String.format(NUM_OF_VFS_REPLACEMENT, numOfVfs),
                String.format(MAX_NUM_OF_VFS_REPLACEMENT, oldVfsConfig.getMaxNumOfVfs()))
                .when(numOfVfs > oldVfsConfig.getMaxNumOfVfs() || numOfVfs < 0);
    }

    /**
     * @return An error iff <code>allNetworkAllowed</code> is <code>true</code>
     */
    public ValidationResult settingSpecificNetworksAllowed() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_SET_SPECIFIC_NETWORKS,
                getNicNameReplacement())
                .when(oldVfsConfig.isAllNetworksAllowed());
    }

    /**
     * @return An error iff a network with the specified id doesn't exist
     */
    public ValidationResult networkExists(Guid networkId) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NOT_EXIST,
                getNicNameReplacement(), String.format(NETWORK_ID_REPLACEMENT, networkId))
                .when(getNetwork(networkId) == null);
    }

    /**
     * @return An error iff the network is already part of the VFs configuration
     */
    public ValidationResult networkNotInVfsConfig(Guid networkId) {
        String networkName = getNetwork(networkId).getName();
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ALREADY_IN_VFS_CONFIG,
                getNicNameReplacement(), String.format(NETWORK_NAME_REPLACEMENT, networkName))
                .when(oldVfsConfig.getNetworks().contains(networkId));
    }

    /**
     * @return An error iff the network is not part of the VFs configuration
     */
    public ValidationResult networkInVfsConfig(Guid networkId) {
        String networkName = getNetwork(networkId).getName();
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NOT_IN_VFS_CONFIG,
                getNicNameReplacement(), String.format(NETWORK_NAME_REPLACEMENT, networkName))
                .when(!oldVfsConfig.getNetworks().contains(networkId));
    }

    /**
     * @return An error iff the label is already part of the VFs configuration
     */
    public ValidationResult labelNotInVfsConfig(String label) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_LABEL_ALREADY_IN_VFS_CONFIG,
                getNicNameReplacement(), String.format(LABEL_REPLACEMENT, label))
                .when(oldVfsConfig.getNetworkLabels().contains(label));
    }

    /**
     * @return An error iff the label is not part of the VFs configuration
     */
    public ValidationResult labelInVfsConfig(String label) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_LABEL_NOT_IN_VFS_CONFIG,
                getNicNameReplacement(), String.format(LABEL_REPLACEMENT, label))
                .unless(oldVfsConfig.getNetworkLabels().contains(label));
    }

    Network getNetwork(Guid networkId) {
        return Injector.get(NetworkDao.class).get(networkId);
    }

    VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = Injector.get(InterfaceDao.class).get(nicId);
        }
        return nic;
    }

    String getNicNameReplacement() {
        return String.format(NIC_NAME_REPLACEMENT, getNic().getName());
    }

    void setOldVfsConfig(HostNicVfsConfig oldVfsConfig) {
        this.oldVfsConfig = oldVfsConfig;
    }
}
