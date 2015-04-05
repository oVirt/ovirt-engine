package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.host.HostNicVfsConfigHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class VfsConfigValidator {

    private Guid nicId;
    private VdsNetworkInterface nic;
    private HostNicVfsConfig oldVfsConfig;

    static final String NIC_NAME_REPLACEMENT = "$nicName %s";
    static final String NUM_OF_VFS_REPLACEMENT = "$numOfVfs %d";
    static final String MAX_NUM_OF_VFS_REPLACEMENT = "$maxNumOfVfs %d";
    static final String NETWORK_NAME_REPLACEMENT = "$networkName %s";

    public VfsConfigValidator(Guid nicId, HostNicVfsConfig oldVfsConfig) {
        this.nicId = nicId;
        this.oldVfsConfig = oldVfsConfig;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * @return An error iff a nic with the specified id doesn't exist.
     */
    public ValidationResult nicExists() {
        return ValidationResult.failWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST)
                .when(getNic() == null);
    }

    /**
     * @return An error iff the nic is not SR-IOV enabled
     */
    public ValidationResult nicSriovEnabled() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED,
                getNicNameReplacement())
                .when(oldVfsConfig == null);
    }

    /**
     * @return An error iff SR-IOV feature doesn't supported in the nic's cluster compatibility version
     */
    public ValidationResult sriovFeatureSupported() {

        VDS host = getDbFacade().getVdsDao().get(getNic().getVdsId());
        Version clusterCompVer = host.getVdsGroupCompatibilityVersion();

        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_SRIOV_FEATURE_NOT_SUPPORTED)
                .unless(FeatureSupported.sriov(clusterCompVer));
    }

    /**
     * @return An error iff there are non-free VFs of the nic
     */
    public ValidationResult allVfsAreFree(HostNicVfsConfigHelper hostNicVfsConfigHelper) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED,
                getNicNameReplacement())
                .unless(hostNicVfsConfigHelper.areAllVfsFree(getNic()));
    }

    /**
     * @param numOfVfs
     *
     * @return An error iff <code>numOfVfs</code> is bigger than the <code>vfsConfig.maxNumOfVfs</code>
     */
    public ValidationResult numOfVfsInValidRange(int numOfVfs) {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE,
                getNicNameReplacement(),
                String.format(NUM_OF_VFS_REPLACEMENT, numOfVfs),
                String.format(MAX_NUM_OF_VFS_REPLACEMENT, oldVfsConfig.getMaxNumOfVfs()))
                .when(numOfVfs > oldVfsConfig.getMaxNumOfVfs() || numOfVfs < 0);
    }

    VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = getDbFacade().getInterfaceDao().get(nicId);
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
