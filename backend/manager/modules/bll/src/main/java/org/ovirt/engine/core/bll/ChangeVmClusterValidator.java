package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class ChangeVmClusterValidator {

    private VmCommand parentCommand;
    private final Guid targetClusterId;
    private Version vmCompatibilityVersion;

    private Cluster targetCluster;

    private final VmDeviceUtils vmDeviceUtils;

    public ChangeVmClusterValidator(VmCommand parentCommand,
            Guid targetClusterId,
            Version vmCustomCompatibilityVersion,
            VmDeviceUtils vmDeviceUtils) {
        this.parentCommand = parentCommand;
        this.targetClusterId = targetClusterId;
        this.vmCompatibilityVersion = vmCustomCompatibilityVersion;
        this.vmDeviceUtils = vmDeviceUtils;
    }

    protected boolean validate() {
        VM vm = parentCommand.getVm();
        if (vm == null) {
            parentCommand.addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        } else {
            targetCluster = DbFacade.getInstance().getClusterDao().get(targetClusterId);
            if (targetCluster == null) {
                parentCommand.addValidationMessage(EngineMessage.VM_CLUSTER_IS_NOT_VALID);
                return false;
            }

            // Check that the target cluster is in the same data center.
            if (!targetCluster.getStoragePoolId().equals(vm.getStoragePoolId())) {
                parentCommand.addValidationMessage(EngineMessage.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
                return false;
            }

            if (vmCompatibilityVersion == null) {
                vmCompatibilityVersion = targetCluster.getCompatibilityVersion();
            }

            List<VmNic> interfaces = DbFacade.getInstance().getVmNicDao().getAllForVm(vm.getId());

            if (!validateDestinationClusterContainsNetworks(interfaces)) {
                return false;
            }

            // Check if VM static parameters are compatible for new cluster.
            ValidationResult isCpuSocketsValid = VmValidator.validateCpuSockets(vm.getStaticData(),
                    vmCompatibilityVersion.getValue());
            if (!isCpuSocketsValid.isValid()) {
                return parentCommand.validate(isCpuSocketsValid);
            }

            // Check that the USB policy is legal
            if (!VmHandler.isUsbPolicyLegal(
                    vm.getUsbPolicy(),
                    vm.getOs(),
                    parentCommand.getReturnValue().getValidationMessages())) {
                return false;
            }

            // Check if the display type is supported
            if (!VmHandler.isGraphicsAndDisplaySupported(
                    vm.getOs(),
                    vmDeviceUtils.getGraphicsTypesOfEntity(vm.getId()),
                    vm.getDefaultDisplayType(),
                    parentCommand.getReturnValue().getValidationMessages(),
                    vmCompatibilityVersion)) {
                return false;
            }

            if (vmDeviceUtils.hasVirtioScsiController(vm.getId())) {
                // Verify OS compatibility
                if (!VmHandler.isOsTypeSupportedForVirtioScsi(
                        vm.getOs(),
                        vmCompatibilityVersion,
                        parentCommand.getReturnValue().getValidationMessages())) {
                    return false;
                }
            }

            // A existing VM cannot be changed into a cluster without a defined architecture
            if (targetCluster.getArchitecture() == ArchitectureType.undefined) {
                return parentCommand.failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
            } else if (targetCluster.getArchitecture() != vm.getClusterArch()) {
                return parentCommand.failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CLUSTER_DIFFERENT_ARCHITECTURES);
            }

            // Cluster must have a cpu profile
            List<CpuProfile> cpuProfiles = DbFacade.getInstance().getCpuProfileDao().getAllForCluster(
                    targetClusterId, parentCommand.getUserId(), true, ActionGroup.ASSIGN_CPU_PROFILE);

            if (cpuProfiles.isEmpty()) {
                return parentCommand.failValidation(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
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
            parentCommand.addValidationMessage(EngineMessage.MOVE_VM_CLUSTER_MISSING_NETWORK);
            parentCommand.addValidationMessageVariable("networks", missingNets.toString());
            return false;
        }

        return true;
    }
}
