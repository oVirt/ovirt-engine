package org.ovirt.engine.core.bll.validator;

import static java.util.Objects.requireNonNull;

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
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.utils.OS;

@Singleton
public class InClusterUpgradeValidator {

    @Inject
    HostDeviceManager hostDeviceManager;

    public ValidationResult isUpgradePossible(Collection<VDS> hosts, Collection<VM> vms) {
        requireNonNull(hosts);
        requireNonNull(vms);
        final List<String> errors = new ArrayList<>();
        for (final VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                errors.addAll(toHostEngineMessage(host, EngineMessage.CLUSTER_UPGRADE_DETAIL_HOST_INVALID_OS));
            }
        }

        for (final VM vm : vms) {
            errors.addAll(checkVmReadyForUpgrade(vm));
        }
        if (errors.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.CLUSTER_UPGRADE_CAN_NOT_BE_STARTED, errors);
        }
    }

    public ValidationResult isVmReadyForUpgrade(final VM vm) {
        requireNonNull(vm);
        List<String> vmErrors = checkVmReadyForUpgrade(vm);
        if (vmErrors.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.BOUND_TO_HOST_WHILE_UPGRADING_CLUSTER, vmErrors);
        }
    }

    protected List<String> checkVmReadyForUpgrade(final VM vm) {
        requireNonNull(vm);
        final List<String> errors = new ArrayList<>();
        if (vm.getStatus().isSuspended()) {
            errors.addAll(toVmEngineMessage(vm, EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_SUSPENDED));
        }
        if (!StringUtils.isEmpty(VmCpuCountHelper.isAutoPinning(vm) ? vm.getCurrentCpuPinning() : vm.getCpuPinning())) {
            errors.addAll(toVmEngineMessage(vm, EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_CPUS_PINNED));
        }
        for (VmNumaNode vmNumaNode : vm.getvNumaNodeList()) {
            if (!vmNumaNode.getVdsNumaNodeList().isEmpty()) {
                errors.addAll(toVmEngineMessage(vm, EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NUMA_PINNED));
                break;
            }
        }
        if (MigrationSupport.MIGRATABLE != vm.getMigrationSupport()) {
            errors.addAll(toVmEngineMessage(vm, EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NOT_MIGRATABLE));
        }
        //TODO use a more efficient way, this does a db call per VM
        if (hostDeviceManager.checkVmNeedsDirectPassthrough(vm)) {
            errors.addAll(toVmEngineMessage(vm, EngineMessage.CLUSTER_UPGRADE_DETAIL_VM_NEEDS_PASSTHROUGH));
        }
        return errors;
    }

    public ValidationResult isUpgradeDone(Collection<VDS> hosts) {
        requireNonNull(hosts);
        final List<String> errors = new ArrayList<>();
        final Map<String, Set<Integer>> majorVersions = new LinkedHashMap<>();
        final Map<String, Set<VDS>> osToHostIdMap = new HashMap<>();
        for (final VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                errors.addAll(toHostEngineMessage(host, EngineMessage.CLUSTER_UPGRADE_DETAIL_HOST_INVALID_OS));
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
                        errors.addAll(toHostEngineMessage(host, EngineMessage.CLUSTER_UPGRADE_DETAIL_HOST_RUNS_TOO_OLD_OS));
                    }
                }
            }
        }
        if (errors.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.CLUSTER_UPGRADE_NOT_FINISHED, errors);
        }
    }

    private static void putMajorVersion(Map<String, Set<Integer>> majorVersions, OS hostOs) {
        if (!majorVersions.containsKey(hostOs.getOsFamily())) {
            majorVersions.put(hostOs.getOsFamily(), new HashSet<>());
        }
        majorVersions.get(hostOs.getOsFamily()).add(hostOs.getVersion().getMajor());
    }

    private static void putHost(Map<String, Set<VDS>> osToHostIdMap, VDS host, OS hostOs) {
        if (!osToHostIdMap.containsKey(hostOs.getOsFamily())) {
            osToHostIdMap.put(hostOs.getOsFamily(), new HashSet<>());
        }
        osToHostIdMap.get(hostOs.getOsFamily()).add(host);
    }

    private List<String> toVmEngineMessage(final VM vm, final EngineMessage error) {
        return Arrays.asList(error.name(),
                String.format("$%1$s %2$s", "vmName", vm.getName()),
                String.format("$%1$s %2$s", "vmId", vm.getId()));
    }

    private List<String> toHostEngineMessage(final VDS host, final EngineMessage error) {
        return Arrays.asList(error.name(),
                String.format("$%1$s %2$s", "hostName", host.getName()),
                String.format("$%1$s %2$s", "hostId", host.getId()));
    }
}
