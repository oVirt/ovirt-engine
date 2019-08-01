package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "438b052c-90ab-40e8-9be0-a22560202ea6",
        name = "CPU-Level",
        type = PolicyUnitType.FILTER,
        description = "Runs VMs only on hosts with a proper CPU level"
)
public class CpuLevelFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CpuLevelFilterPolicyUnit.class);

    @Inject
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    public CpuLevelFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        List<VDS> hostsToRunOn = new ArrayList<>();

        // CPU passthrough VM can be started everywhere
        if (vm.getRunOnVds() == null && vm.isUsingCpuPassthrough()) {
            hostsToRunOn.addAll(hosts);
            return hostsToRunOn;
        }

        Version compatibilityVer = vm.getCompatibilityVersion();

        // Migration checks for a VM with CPU passthrough.
        // In case of CPU passthrough enabled, the VM's CPU flags (the flags that the VM started running with)
        // should be identical to target host's CPU flags for migration to be allowed
        // TODO figure out how to handle hostModel
        if (vm.isUsingCpuPassthrough()
                && Objects.nonNull(vm.getCpuName())) {
            Set<String> requiredFlags = Arrays.stream(vm.getCpuName().split(","))
                    .collect(Collectors.toSet());

            if (log.isDebugEnabled()) {
                log.debug("VM uses CPU flags passthrough, checking flags compatibility with: {}", formatFlags(requiredFlags));
            }

            for (VDS host : hosts) {
                Set<String> providedFlags = Arrays.stream(host.getCpuFlags().split(","))
                        .collect(Collectors.toSet());
                if (log.isDebugEnabled()) {
                    log.debug("Host {} provides flags: {}", host.getName(), formatFlags(providedFlags));
                }

                if (requiredFlags.equals(providedFlags)) {
                    hostsToRunOn.add(host);
                } else {
                    String missingFlags = formatFlags(requiredFlags, providedFlags);
                    String additionalFlags = formatFlags(providedFlags, requiredFlags);
                    log.debug("Host {} can't run the VM because it's CPU flags are not exactly identical to VM's required CPU flags."
                                    + " It is missing flags: {}."
                                    + " And it has additional flags: {}",
                            host.getName(),
                            missingFlags,
                            additionalFlags);

                    messages.addMessage(host.getId(), String.format("$missingFlags %1$s", missingFlags));
                    messages.addMessage(host.getId(), String.format("$additionalFlags %1$s", additionalFlags));
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__CPU_FLAGS_NOT_IDENTICAL.toString());
                }
            }
            return hostsToRunOn;
        }

        Cluster cluster = context.getCluster();
        String vmFlags;

        if (StringUtils.isNotEmpty(vm.getCpuName())
                        && StringUtils.isNotEmpty(cluster.getCpuVerb())
                        && StringUtils.isNotEmpty(cluster.getCpuFlags())
                        && vm.getCpuName().equals(cluster.getCpuVerb())) { // dynamic check - used for 1.migrating vms 2.run-once 3.after dynamic field is updated with current static-field\cluster
            vmFlags = cluster.getCpuFlags();
        } else if (StringUtils.isNotEmpty(vm.getCpuName()) && !vm.getCpuName().equals(cluster.getCpuVerb())) {
            String cpuName = cpuFlagsManagerHandler.getCpuNameByCpuId(vm.getCpuName(), compatibilityVer);
            vmFlags = cpuFlagsManagerHandler.getFlagsByCpuName(cpuName, compatibilityVer);
        } else if (StringUtils.isNotEmpty(vm.getCustomCpuName())) { // static check - used only for cases where the
                                                                  // dynamic value hasn't been updated yet(validate)
            String cpuName = cpuFlagsManagerHandler.getCpuNameByCpuId(vm.getCustomCpuName(), compatibilityVer);
            vmFlags = cpuFlagsManagerHandler.getFlagsByCpuName(cpuName, compatibilityVer);
        } else { // use cluster default - all hosts are valid
            return hosts;
        }

        /* find compatible hosts */
        for (VDS host : hosts) {
            List<String> missingFlags = cpuFlagsManagerHandler.missingClusterCpuFlags(vmFlags, host.getCpuFlags());

            if (missingFlags.isEmpty()) {
                hostsToRunOn.add(host);
            } else {
                String formattedFlags = formatFlags(missingFlags);
                if (log.isDebugEnabled()) {
                    log.debug("Host {} can't run the VM because its CPU flags are missing VM's required CPU flags."
                                    + " It is missing flags: {}.",
                            host.getName(),
                            formattedFlags
                    );
                }
                messages.addMessage(host.getId(), String.format("$cpuFlags %1$s", formattedFlags));
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__LOW_CPU_LEVEL.toString());
            }
        }
        return hostsToRunOn;
    }

    private String formatFlags(Collection<String> flags) {
        return formatFlags(flags, null);
    }

    private String formatFlags(Collection<String> flags, Collection<String> removedFlags) {
        return flags.stream()
                .filter(flag -> removedFlags == null || !removedFlags.contains(flag))
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
