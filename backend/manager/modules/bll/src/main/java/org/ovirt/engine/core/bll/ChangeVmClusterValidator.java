package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

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
import org.ovirt.engine.core.common.osinfo.OsRepository;
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

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private OsRepository osRepository;

    private VM vm;
    private final Guid targetClusterId;
    private Version vmCompatibilityVersion;
    private Guid userId;

    public static ChangeVmClusterValidator create(VM vm,
            Guid targetClusterId,
            Version vmCompatibilityVersion,
            Guid userId) {
        return Injector.injectMembers(new ChangeVmClusterValidator(vm, targetClusterId, vmCompatibilityVersion, userId));
    }

    ChangeVmClusterValidator(VM vm,
            Guid targetClusterId,
            Version vmCustomCompatibilityVersion,
            Guid userId) {
        this.vm = vm;
        this.targetClusterId = targetClusterId;
        this.vmCompatibilityVersion = vmCustomCompatibilityVersion;
        this.userId = userId;
    }

    protected ValidationResult validate() {
        if (vm == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        Cluster targetCluster = clusterDao.get(targetClusterId);
        if (targetCluster == null) {
            return new ValidationResult(EngineMessage.VM_CLUSTER_IS_NOT_VALID);
        }

        // Check that the target cluster is in the same data center.
        if (!targetCluster.getStoragePoolId().equals(vm.getStoragePoolId())) {
            return new ValidationResult(EngineMessage.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
        }

        if (vmCompatibilityVersion == null) {
            vmCompatibilityVersion = targetCluster.getCompatibilityVersion();
        }

        List<VmNic> interfaces = vmNicDao.getAllForVm(vm.getId());
        ValidationResult destinationClusterContainsNetworks = validateDestinationClusterContainsNetworks(interfaces);
        if (!destinationClusterContainsNetworks.isValid()) {
            return destinationClusterContainsNetworks;
        }

        // Check if VM static parameters are compatible for new cluster.
        ValidationResult isCpuSocketsValid = VmValidator.validateCpuSockets(vm.getStaticData(),
                vmCompatibilityVersion,
                targetCluster.getArchitecture(),
                osRepository);
        if (!isCpuSocketsValid.isValid()) {
            return isCpuSocketsValid;
        }

        // Check if the display type is supported
        ValidationResult isGraphicsAndDisplaySupported = vmHandler.isGraphicsAndDisplaySupported(
                vm.getOs(),
                vmDeviceUtils.getGraphicsTypesOfEntity(vm.getId()),
                vm.getDefaultDisplayType(),
                vm.getBiosType(),
                vmCompatibilityVersion);
        if (!isGraphicsAndDisplaySupported.isValid()) {
            return isGraphicsAndDisplaySupported;
        }

        if (vmDeviceUtils.hasVirtioScsiController(vm.getId())) {
            // Verify OS compatibility
            ValidationResult isOsTypeSupportedForVirtioScsi = vmHandler.isOsTypeSupportedForVirtioScsi(vm.getOs(),
                    vmCompatibilityVersion);
            if (!isOsTypeSupportedForVirtioScsi.isValid()) {
                return isOsTypeSupportedForVirtioScsi;
            }
        }

        // A existing VM cannot be changed into a cluster without a defined architecture
        if (targetCluster.getArchitecture() == ArchitectureType.undefined) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }
        if (targetCluster.getArchitecture() != vm.getClusterArch()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_CLUSTER_DIFFERENT_ARCHITECTURES);
        }

        // Cluster must have a cpu profile
        List<CpuProfile> cpuProfiles = cpuProfileDao.getAllForCluster(
                targetClusterId, userId, true, ActionGroup.ASSIGN_CPU_PROFILE);

        if (cpuProfiles.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_CPU_PROFILE_EMPTY);
        }

        return ValidationResult.VALID;
    }

    /**
     * Checks that the destination cluster has all the networks that the given NICs require.<br>
     * No network on a NIC is allowed (it's checked when running VM).
     *
     * @param interfaces The NICs to check networks on.
     * @return Whether the destination cluster has all networks configured or not.
     */
    private ValidationResult validateDestinationClusterContainsNetworks(List<VmNic> interfaces) {
        List<Network> networks = networkDao.getAllForCluster(targetClusterId);
        StringBuilder missingNets = new StringBuilder();
        for (VmNic iface : interfaces) {
            Network network = networkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
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
            String replacement = ReplacementUtils.createSetVariableString("networks", missingNets.toString());
            return new ValidationResult(EngineMessage.MOVE_VM_CLUSTER_MISSING_NETWORK, replacement);
        }

        return ValidationResult.VALID;
    }
}
