package org.ovirt.engine.core.bll.validator;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.scheduling.OS;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NumaUtils;

@Singleton
public class InClusterUpgradeValidator {

    @Inject
    HostDeviceManager hostDeviceManager;

    public enum UpgradeError {
        HOST_INVALID_OS,
        HOST_RUNS_TOO_OLD_OS,
        VM_NOT_MIGRATABLE,
        VM_CPUS_PINNED,
        VM_NUMA_PINNED,
        VM_NEEDS_PASSTHROUGH,
        VM_SUSPENDED
    }

    public ValidationResult isUpgradePossible(Collection<VDS> hosts, Collection<VM> vms) {
        requireNonNull(hosts);
        requireNonNull(vms);
        final Map<Guid, List<UpgradeError>> hostValidationResults = new LinkedHashMap<>();
        final Map<Guid, List<UpgradeError>> vmValidationResults = new LinkedHashMap<>();
        for (final VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                hostValidationResults.put(host.getId(), Arrays.asList(UpgradeError.HOST_INVALID_OS));
            }
        }

        for (final VM vm : vms) {
            final List<UpgradeError> errors = checkVmReadyForUpgrade(vm);
            if (!errors.isEmpty()) {
                vmValidationResults.put(vm.getId(), errors);
            }
        }
        if (hostValidationResults.isEmpty() && vmValidationResults.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.CLUSTER_UPGRADE_CAN_NOT_BE_STARTED,
                    String.format("$json %1$s", ClusterValidation.toJson(hostValidationResults, vmValidationResults)));
        }
    }

    public ValidationResult isVmReadyForUpgrade(final VM vm) {
        requireNonNull(vm);
        List<UpgradeError> validationResult = checkVmReadyForUpgrade(vm);
        if (validationResult.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER,
                    String.format("$json %1$s", VmValidation.toJson(validationResult)));
        }
    }

    protected List<UpgradeError> checkVmReadyForUpgrade(final VM vm) {
        requireNonNull(vm);
        final List<UpgradeError> errors = new ArrayList<>();
        if (vm.getStatus().isSuspended()) {
            errors.add(UpgradeError.VM_SUSPENDED);
        }
        if (!StringUtils.isEmpty(vm.getCpuPinning())) {
            errors.add(UpgradeError.VM_CPUS_PINNED);
        }
        for (VmNumaNode vmNumaNode : vm.getvNumaNodeList()) {
            if (!NumaUtils.getPinnedNodeIndexList(vmNumaNode.getVdsNumaNodeList()).isEmpty()) {
                errors.add(UpgradeError.VM_NUMA_PINNED);
                break;
            }
        }
        if (MigrationSupport.MIGRATABLE != vm.getMigrationSupport()) {
            errors.add(UpgradeError.VM_NOT_MIGRATABLE);
        }
        //TODO use a more efficient way, this does a db call per VM
        if (hostDeviceManager.checkVmNeedsDirectPassthrough(vm)) {
            errors.add(UpgradeError.VM_NEEDS_PASSTHROUGH);
        }
        return errors;
    }

    public ValidationResult isUpgradeDone(Collection<VDS> hosts) {
        requireNonNull(hosts);
        final Map<Guid, List<UpgradeError>> hostValidationResults = new LinkedHashMap<>();
        final Map<String, Set<Integer>> majorVersions = new LinkedHashMap<>();
        final Map<String, Set<VDS>> osToHostIdMap = new HashMap<>();
        for (final VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                hostValidationResults.put(host.getId(), Arrays.asList(UpgradeError.HOST_INVALID_OS));
            } else {
                putMajorVersion(majorVersions, hostOs);
                putHost(osToHostIdMap, host, hostOs);
            }
        }

        for (Map.Entry<String, Set<Integer>> entry : majorVersions.entrySet()) {
            if (entry.getValue().size() > 1) {
                final int newestMajorVersion = Collections.max(entry.getValue());
                for (VDS host : osToHostIdMap.get(entry.getKey())) {
                    final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
                    if (hostOs.getVersion().getMajor() < newestMajorVersion) {
                        hostValidationResults.put(host.getId(), Arrays.asList(UpgradeError.HOST_RUNS_TOO_OLD_OS));
                    }
                }
            }
        }
        if (hostValidationResults.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.CLUSTER_UPGRADE_NOT_FINISHED,
                    String.format("$json %1$s", ClusterValidation.toJson(hostValidationResults, null)));
        }
    }

    private static void putMajorVersion(Map<String, Set<Integer>> majorVersions, OS hostOs) {
        if (!majorVersions.containsKey(hostOs.getName())) {
            majorVersions.put(hostOs.getName(), new HashSet<Integer>());
        }
        majorVersions.get(hostOs.getName()).add(hostOs.getVersion().getMajor());
    }

    private static void putHost(Map<String, Set<VDS>> osToHostIdMap, VDS host, OS hostOs) {
        if (!osToHostIdMap.containsKey(hostOs.getName())) {
            osToHostIdMap.put(hostOs.getName(), new HashSet<>());
        }
        osToHostIdMap.get(hostOs.getName()).add(host);
    }

    public ValidationResult checkClusterUpgradeIsEnabled(final Cluster cluster) {
        return ValidationResult.failWith(EngineMessage.MIXED_HOST_VERSIONS_NOT_ALLOWED).when(
                Config.<Boolean>getValue(ConfigValues.CheckMixedRhelVersions, cluster.getCompatibilityVersion()
                        .getValue())
        );
    }

    public static class VmValidation {

        List<UpgradeError> vm;

        public VmValidation(List<UpgradeError> vm) {
            this.vm = vm;
        }

        public List<UpgradeError> getVm() {
            return vm;
        }

        public static String toJson(final List<UpgradeError> vm){
             final ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new VmValidation(vm));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ClusterValidation {

        public ClusterValidation(final Map<Guid, List<UpgradeError>> hosts, final Map<Guid, List<UpgradeError>> vms) {
            this.hosts = hosts;
            this.vms = vms;
        }

        Map<Guid, List<UpgradeError>> hosts;

        Map<Guid, List<UpgradeError>> vms;

        public Map<Guid, List<UpgradeError>> getHosts() {
            return hosts;
        }

        public Map<Guid, List<UpgradeError>> getVms() {
            return vms;
        }

        public static String toJson(final Map<Guid, List<UpgradeError>> hosts, final Map<Guid, List<UpgradeError>>
                vms) {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ClusterValidation(hosts, vms));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
