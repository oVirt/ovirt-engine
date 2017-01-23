package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
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
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class ChangeVmClusterValidator {

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private VmNicDao vmNicDao;

    @Inject
    private CpuProfileDao cpuProfileDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private VmHandler vmHandler;

    private VmCommand parentCommand;
    private final Guid targetClusterId;
    private Version vmCompatibilityVersion;

    private Cluster targetCluster;

    public static ChangeVmClusterValidator create(VmCommand parentCommand,
            Guid targetClusterId,
            Version vmCompatibilityVersion) {
        return Injector.injectMembers(new ChangeVmClusterValidator(parentCommand, targetClusterId, vmCompatibilityVersion));
    }

    ChangeVmClusterValidator(VmCommand parentCommand,
            Guid targetClusterId,
            Version vmCustomCompatibilityVersion) {
        this.parentCommand = parentCommand;
        this.targetClusterId = targetClusterId;
        this.vmCompatibilityVersion = vmCustomCompatibilityVersion;
    }

    protected boolean validate() {
        VM vm = parentCommand.getVm();
        if (vm == null) {
            parentCommand.addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        } else {
            targetCluster = clusterDao.get(targetClusterId);
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

            List<VmNic> interfaces = vmNicDao.getAllForVm(vm.getId());

            if (!validateDestinationClusterContainsNetworks(interfaces)) {
                return false;
            }

            // Check if VM static parameters are compatible for new cluster.
            ValidationResult isCpuSocketsValid = VmValidator.validateCpuSockets(vm.getStaticData(),
                    vmCompatibilityVersion.getValue());
            if (!isCpuSocketsValid.isValid()) {
                return parentCommand.validate(isCpuSocketsValid);
            }

            // Check if the display type is supported
            if (!vmHandler.isGraphicsAndDisplaySupported(
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
            List<CpuProfile> cpuProfiles = cpuProfileDao.getAllForCluster(
                    targetClusterId, parentCommand.getUserId(), true, ActionGroup.ASSIGN_CPU_PROFILE);

            if (cpuProfiles.isEmpty()) {
                return parentCommand.failValidation(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
            }

        }
        return true;
    }

    public ValidationResult validateCanMoveMacs(ReadMacPool macPoolForTargetCluster, List<String> macsToMigrate) {
        if (macPoolForTargetCluster.isDuplicateMacAddressesAllowed()) {
            return ValidationResult.VALID;
        }

        List<String> nonMigratableMacs = macsToMigrate.stream()
                .filter(macPoolForTargetCluster::isMacInUse)
                .collect(Collectors.toList());

        if (nonMigratableMacs.isEmpty()) {
            return ValidationResult.VALID;
        }

        EngineMessage engineMessage =
                EngineMessage.ACTION_TYPE_FAILED_CANNOT_UPDATE_VM_TARGET_CLUSTER_HAS_DUPLICATED_MACS;
        Collection<String> replacements =
                ReplacementUtils.getListVariableAssignmentString(engineMessage, nonMigratableMacs);
        return new ValidationResult(engineMessage, replacements);
    }

    /**
     * Checks that the destination cluster has all the networks that the given NICs require.<br>
     * No network on a NIC is allowed (it's checked when running VM).
     *
     * @param interfaces The NICs to check networks on.
     * @return Whether the destination cluster has all networks configured or not.
     */
    private boolean validateDestinationClusterContainsNetworks(List<VmNic> interfaces) {
        List<Network> networks = networkDao.getAllForCluster(targetClusterId);
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
