package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    private VDSGroup targetCluster;
    private boolean dedicatedHostWasCleared;

    public ChangeVMClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        // Set parameters for messaging.
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM__CLUSTER);

        VM vm = getVm();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        } else {

            if (!canRunActionOnNonManagedVm()) {
                return false;
            }

            if (ObjectIdentityChecker.CanUpdateField(vm, "vdsGroupId", vm.getStatus())) {
                targetCluster = DbFacade.getInstance().getVdsGroupDao().get(getParameters().getClusterId());
                if (targetCluster == null) {
                    addCanDoActionMessage(VdcBllMessages.VM_CLUSTER_IS_NOT_VALID);
                    return false;
                }

                // Check that the target cluster is in the same data center.
                if (!targetCluster.getStoragePoolId().equals(vm.getStoragePoolId())) {
                    addCanDoActionMessage(VdcBllMessages.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
                    return false;
                }

                List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getVmId());

                Version clusterCompatibilityVersion = targetCluster.getcompatibility_version();
                if (!validateDestinationClusterContainsNetworks(interfaces)
                        || !validateNics(interfaces, clusterCompatibilityVersion)) {
                    return false;
                }

                // Check if VM static parameters are compatible for new cluster.
                boolean isCpuSocketsValid = AddVmCommand.checkCpuSockets(
                        vm.getStaticData().getNumOfSockets(),
                        vm.getStaticData().getCpuPerSocket(),
                        clusterCompatibilityVersion.getValue(),
                        getReturnValue().getCanDoActionMessages());
                if (!isCpuSocketsValid) {
                    return false;
                }

                // Check that the USB policy is legal
                if (!VmHandler.isUsbPolicyLegal(vm.getUsbPolicy(), vm.getOs(), targetCluster, getReturnValue().getCanDoActionMessages())) {
                    return false;
                }

                // Check if the display type is supported
                if (!VmHandler.isDisplayTypeSupported(vm.getOs(),
                        vm.getDefaultDisplayType(),
                        getReturnValue().getCanDoActionMessages(),
                        clusterCompatibilityVersion)) {
                    return false;
                }

                if (VmDeviceUtils.isVirtioScsiControllerAttached(vm.getId())) {
                    // Verify cluster compatibility
                    if (!FeatureSupported.virtIoScsi(targetCluster.getcompatibility_version())) {
                        return failCanDoAction(VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
                    }

                    // Verify OS compatibility
                    if (!VmHandler.isOsTypeSupportedForVirtioScsi(vm.getOs(), targetCluster.getcompatibility_version(),
                            getReturnValue().getCanDoActionMessages())) {
                        return false;
                    }
                }

                // A existing VM cannot be changed into a cluster without a defined architecture
                if (targetCluster.getArchitecture() == ArchitectureType.undefined) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
                } else if (targetCluster.getArchitecture() != vm.getClusterArch()) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_CLUSTER_DIFFERENT_ARCHITECTURES);
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.VM_STATUS_NOT_VALID_FOR_UPDATE);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that when unlinking/null network is not supported in the destination cluster and a NIC has unlinked/null
     * network, it's not valid.
     *
     * @param interfaces
     *            The NICs to check.
     * @param clusterCompatibilityVersion
     *            The destination cluster's compatibility version.
     * @return Whether the NICs are linked correctly and network name is valid (with regards to the destination
     *         cluster).
     */
    private boolean validateNics(List<VmNic> interfaces,
            Version clusterCompatibilityVersion) {
        for (VmNic iface : interfaces) {
            VmNicValidator nicValidator = new VmNicValidator(iface, clusterCompatibilityVersion);
            if (!validate(nicValidator.emptyNetworkValid()) || !validate(nicValidator.linkedCorrectly())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that the destination cluster has all the networks that the given NICs require.<br>
     * No network on a NIC is allowed (it's checked when running VM).
     *
     * @param interfaces
     *            The NICs to check networks on.
     * @return Whether the destination cluster has all networks configured or not.
     */
    private boolean validateDestinationClusterContainsNetworks(List<VmNic> interfaces) {
        List<Network> networks =
                DbFacade.getInstance().getNetworkDao().getAllForCluster(getParameters().getClusterId());
        StringBuilder missingNets = new StringBuilder();
        for (VmNic iface : interfaces) {
            Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
            if (network != null) {
                boolean exists = false;
                for (Network net : networks) {
                    if (net.getName().equals(network.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    if (missingNets.length() > 0) {
                        missingNets.append(", ");
                    }
                    missingNets.append(network.getName());
                }
            }
        }
        if (missingNets.length() > 0) {
            addCanDoActionMessage(VdcBllMessages.MOVE_VM_CLUSTER_MISSING_NETWORK);
            addCanDoActionMessageVariable("networks", missingNets.toString());
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        // check that the cluster are not the same
        VM vm = getVm();
        if (vm.getVdsGroupId().equals(getParameters().getClusterId())) {
            setSucceeded(true);
            return;
        }

        // update vm interfaces
        List<Network> networks = getNetworkDAO().getAllForCluster(getParameters().getClusterId());
        List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getVmId());

        for (final VmNic iface : interfaces) {
            if (iface.getVnicProfileId() != null) {
                final Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return ObjectUtils.equals(n.getId(), network.getId());
                    }
                });

                // if network not exists in cluster we remove the network to
                // interface connection
                if (net == null) {
                    iface.setVnicProfileId(null);
                    getVmNicDao().update(iface);
                }
            }
        }

        if (vm.getDedicatedVmForVds() != null) {
            vm.setDedicatedVmForVds(null);
            dedicatedHostWasCleared = true;
        }

        // Since CPU profile is coupled to cluster, when changing a cluster
        // the 'old' CPU profile is invalid. The update VM command is called straight after
        // will validate a right profile for VM and its cluster
        vm.setCpuProfileId(null);
        vm.setVdsGroupId(getParameters().getClusterId());
        DbFacade.getInstance().getVmStaticDao().update(vm.getStaticData());

        // change vm cluster should remove the vm from all associated affinity groups
        List<AffinityGroup> allAffinityGroupsByVmId =
                DbFacade.getInstance().getAffinityGroupDao().getAllAffinityGroupsByVmId(vm.getId());
        if (!allAffinityGroupsByVmId.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AffinityGroup affinityGroup : allAffinityGroupsByVmId) {
                sb.append(affinityGroup.getName() + " ");
            }
            log.infoFormat("Due to cluster change, removing VM from associated affinity group(s): {0}", sb.toString());
            DbFacade.getInstance().getAffinityGroupDao().removeVmFromAffinityGroups(vm.getId());
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                dedicatedHostWasCleared ?
                        AuditLogType.USER_UPDATE_VM_CLUSTER_DEFAULT_HOST_CLEARED
                        : AuditLogType.USER_UPDATE_VM
                : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        // In addition to having EDIT_VM_PROPERTIES on the VM, you must have CREATE_VM on the cluster
        permissionList.add(new PermissionSubject(getParameters().getVmId(), VdcObjectType.VM, getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getParameters().getClusterId(), VdcObjectType.VdsGroups, ActionGroup.CREATE_VM));
        return permissionList;
    }
}
