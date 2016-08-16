package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmNicValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class ChangeVmClusterValidator {

    private final Guid targetClusterId;

    private VmCommand parentCommand;

    private VDSGroup targetCluster;

    public ChangeVmClusterValidator(VmCommand parentCommand, Guid targetClusterId) {
        this.parentCommand = parentCommand;
        this.targetClusterId = targetClusterId;
    }

    protected boolean validate() {
        VM vm = parentCommand.getVm();
        if (vm == null) {
            parentCommand.addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        } else {
            targetCluster = DbFacade.getInstance().getVdsGroupDao().get(targetClusterId);
            if (targetCluster == null) {
                parentCommand.addCanDoActionMessage(EngineMessage.VM_CLUSTER_IS_NOT_VALID);
                return false;
            }

            // Check that the target cluster is in the same data center.
            if (!targetCluster.getStoragePoolId().equals(vm.getStoragePoolId())) {
                parentCommand.addCanDoActionMessage(EngineMessage.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
                return false;
            }

            List<VmNic> interfaces = DbFacade.getInstance().getVmNicDao().getAllForVm(vm.getId());

            Version clusterCompatibilityVersion = targetCluster.getCompatibilityVersion();
            if (!validateDestinationClusterContainsNetworks(interfaces)
                    || !validateNics(interfaces, clusterCompatibilityVersion)) {
                return false;
            }

            // Check if VM static parameters are compatible for new cluster.
            boolean isCpuSocketsValid = AddVmCommand.checkCpuSockets(
                    vm.getStaticData().getNumOfSockets(),
                    vm.getStaticData().getCpuPerSocket(),
                    vm.getStaticData().getThreadsPerCpu(),
                    clusterCompatibilityVersion.getValue(),
                    parentCommand.getReturnValue().getCanDoActionMessages());
            if (!isCpuSocketsValid) {
                return false;
            }

            // Check that the USB policy is legal
            if (!VmHandler.isUsbPolicyLegal(vm.getUsbPolicy(), vm.getOs(), targetCluster, parentCommand.getReturnValue().getCanDoActionMessages())) {
                return false;
            }

            // Check if the display type is supported
            if (!VmHandler.isGraphicsAndDisplaySupported(vm.getOs(),
                    VmDeviceUtils.getGraphicsTypesOfEntity(vm.getId()),
                    vm.getDefaultDisplayType(),
                    parentCommand.getReturnValue().getCanDoActionMessages(),
                    clusterCompatibilityVersion)) {
                return false;
            }

            if (VmDeviceUtils.hasVirtioScsiController(vm.getId())) {
                // Verify cluster compatibility
                if (!FeatureSupported.virtIoScsi(targetCluster.getCompatibilityVersion())) {
                    return parentCommand.failCanDoAction(EngineMessage.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
                }

                // Verify OS compatibility
                if (!VmHandler.isOsTypeSupportedForVirtioScsi(vm.getOs(), targetCluster.getCompatibilityVersion(),
                        parentCommand.getReturnValue().getCanDoActionMessages())) {
                    return false;
                }
            }

            // A existing VM cannot be changed into a cluster without a defined architecture
            if (targetCluster.getArchitecture() == ArchitectureType.undefined) {
                return parentCommand.failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
            } else if (targetCluster.getArchitecture() != vm.getClusterArch()) {
                return parentCommand.failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_CLUSTER_DIFFERENT_ARCHITECTURES);
            }

            // Cluster must have a cpu profile
            List<CpuProfile> cpuProfiles = DbFacade.getInstance().getCpuProfileDao().getAllForCluster(
                    targetClusterId, parentCommand.getUserId(), true, ActionGroup.ASSIGN_CPU_PROFILE);

            if (cpuProfiles.isEmpty()) {
                return parentCommand.failCanDoAction(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
            }

        }
        return true;
    }

    /**
     * Checks that the destination cluster has all the networks that the given NICs require.<br>
     * No network on a NIC is allowed (it's checked when running VM).
     *
     * @param interfaces The NICs to check networks on.
     * @return Whether the destination cluster has all networks configured or not.
     */
    private boolean validateDestinationClusterContainsNetworks(List<VmNic> interfaces) {
        List<Network> networks =
                DbFacade.getInstance().getNetworkDao().getAllForCluster(targetClusterId);
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
            parentCommand.addCanDoActionMessage(EngineMessage.MOVE_VM_CLUSTER_MISSING_NETWORK);
            parentCommand.addCanDoActionMessageVariable("networks", missingNets.toString());
            return false;
        }

        return true;
    }

    /**
     * Checks that when unlinking/null network is not supported in the destination cluster and a NIC has unlinked/null
     * network, it's not valid.
     *
     * @param interfaces                  The NICs to check.
     * @param clusterCompatibilityVersion The destination cluster's compatibility version.
     * @return Whether the NICs are linked correctly and network name is valid (with regards to the destination
     * cluster).
     */
    private boolean validateNics(List<VmNic> interfaces,
                                 Version clusterCompatibilityVersion) {
        for (VmNic iface : interfaces) {
            VmNicValidator nicValidator = new VmNicValidator(iface, clusterCompatibilityVersion);
            if (!parentCommand.validate(nicValidator.emptyNetworkValid())
                    || !parentCommand.validate(nicValidator.linkedOnlyIfSupported())) {
                return false;
            }
        }

        return true;
    }
}
