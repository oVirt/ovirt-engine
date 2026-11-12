package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RequestScoped
public class EmulatedMachineClusterEditChecker implements ClusterEditChecker<VDS> {

    private Set<String> newClusterEmulatedMachines;

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        // Applicable whenever the set of machine types backing the cluster default changes
        // (a dual default "i440fx;q35" is backed by both of its parts).
        newClusterEmulatedMachines = getClusterEmulatedMachines(newCluster);

        return !newClusterEmulatedMachines.equals(getClusterEmulatedMachines(oldCluster));
    }

    @Override
    public boolean check(VDS vds) {
        if (newClusterEmulatedMachines == null) {
            throw new IllegalStateException("check() called before isApplicable()");
        }

        // Same semantics as VirtMonitoringStrategy.hostCompliesWithClusterEmulationMode for
        // a dual default: a host supporting at least one part can run the VMs of that
        // chipset (per-VM scheduling handles the rest), so it complies with the default.
        return !Collections.disjoint(getHostEmulatedMachines(vds), newClusterEmulatedMachines);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_HOST_DUE_TO_UNSUPPORTED_MACHINE_TYPE.name();
    }

    @Override
    public String getDetailMessage(VDS vds) {
        Set<String> missing = new HashSet<>(newClusterEmulatedMachines);
        missing.removeAll(getHostEmulatedMachines(vds));
        return StringUtils.join(missing, ",");
    }

    private static Set<String> getClusterEmulatedMachines(Cluster cluster) {
        if (cluster.getEmulatedMachine() == null) {
            return new HashSet<>();
        }
        // The cluster default may be the legacy dual format ("i440fx;q35") in clusters below
        // compatibility level 4.8 — split it so a host is checked against the actual
        // machine types, not the combined string.
        return new HashSet<>(Arrays.asList(cluster.getEmulatedMachine().split(";")));
    }

    private static Set<String> getHostEmulatedMachines(VDS vds) {
        if (vds.getSupportedEmulatedMachines() == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(Arrays.asList(vds.getSupportedEmulatedMachines().split(",")));
        }
    }
}
