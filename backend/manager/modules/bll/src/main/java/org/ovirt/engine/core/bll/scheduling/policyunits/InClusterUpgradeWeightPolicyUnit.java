package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure that hosts with newer major versions of an OS will be preferred when a VM is migrating.
 * Hosts with older major OS versions than the source host are heavy penalized.
 * Hosts with the same major OS version as the source host are slightly penalized.
 * Hosts with a newer major OS version are preferred.
 *
 * When running this policy in combination with {@link InClusterUpgradeFilterPolicyUnit} hosts with older major OS
 * versions will be filtered by the other policy.
 *
 */
@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c298889db568",
        name = "InClusterUpgrade",
        description = "Penalize hosts with older OS version more than hosts with the same OS version where a vm is "
                + "currently running on. Newer OS versions are not penalized",
        type = PolicyUnitType.WEIGHT
)
public class InClusterUpgradeWeightPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(InClusterUpgradeWeightPolicyUnit.class);

    public static final int BAD_WEIGHT = 1000000;
    public static final int BETTER_WEIGHT = 100000;
    public static final int BEST_WEIGHT = 0;

    public InClusterUpgradeWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        VdsDynamic referenceHost = getLastHost(vm);
        boolean isVmStartup = false;

        // Check if the VM is starting
        if (referenceHost == null) {
            isVmStartup = true;
            final Map<String, Version> highestVersions = new HashMap<>();
            for (VDS host : hosts) {
                OS os = OS.fromPackageVersionString(host.getHostOs());
                if (!os.isValid()) {
                    continue;
                }
                Version version = highestVersions.get(os.getOsFamily());
                if (version == null || version.getMajor() < os.getVersion().getMajor()) {
                    highestVersions.put(os.getOsFamily(), os.getVersion());
                    referenceHost = host.getDynamicData();
                }
            }
            if (highestVersions.isEmpty()) {
                log.debug("No valid OS descriptors detected. Will not weight hosts on VM startup.");
                return noWeights(hosts);
            } else if (highestVersions.size() > 1) {
                log.debug("More than one OS family detected. Will not weight hosts on VM startup.");
                return noWeights(hosts);
            }
        }

        final OS lastHostOs = OS.fromPackageVersionString(referenceHost.getHostOs());
        if (!lastHostOs.isValid()) {
            log.debug("Reference host {} provides an invalid or incomplete OS identifier. Found {}.",
                    referenceHost.getId(),
                    referenceHost.getHostOs());
            return noWeights(hosts);
        }

        final List<Pair<Guid, Integer>> weights = new ArrayList<>();
        for (final VDS host : hosts) {
            final OS hostOs = OS.fromPackageVersionString(host.getHostOs());
            if (!hostOs.isValid()) {
                log.debug("Host {} does not provide an valid OS identifier. Found {}.",
                        host.getId(),
                        host.getHostOs());
                weights.add(toWeight(host, BAD_WEIGHT));
            } else if (!hostOs.isSameOsFamily(lastHostOs)) {
                log.debug("Host {} does not run the same operating system. Expected {}, found {}",
                        host.getId(),
                        lastHostOs.getName(),
                        hostOs.getName());
                weights.add(toWeight(host, BAD_WEIGHT));
            } else if (hostOs.isOlderThan(lastHostOs) && !hostOs.isSameMajorVersion(lastHostOs)) {
                weights.add(toWeight(host, BAD_WEIGHT));
            } else if (hostOs.isSameMajorVersion(lastHostOs) && !isVmStartup) {
                weights.add(toWeight(host, BETTER_WEIGHT));
            } else {
                weights.add(toWeight(host, BEST_WEIGHT));
            }
        }
        return weights;
    }

    private static List<Pair<Guid, Integer>> noWeights(final List<VDS> hosts) {
        final List<Pair<Guid, Integer>> weights = new ArrayList<>();
        for (final VDS host : hosts) {
            weights.add(new Pair<>(host.getId(), BEST_WEIGHT));
        }
        return weights;
    }

    private static Pair<Guid, Integer> toWeight(final VDS host, int weight) {
        return new Pair<>(host.getId(), weight);
    }
}
