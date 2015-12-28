package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RequestScoped
public class EmulatedMachineClusterEditChecker implements ClusterEditChecker<VDS> {

    private Set<String> emulatedMachines;

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        emulatedMachines = getClusterEmulatedMachines(newCluster);
        emulatedMachines.removeAll(getClusterEmulatedMachines(oldCluster));

        return !emulatedMachines.isEmpty();
    }

    @Override
    public boolean check(VDS vds) {
        if (emulatedMachines == null) {
            throw new IllegalStateException("check() called before isApplicable()");
        }

        return getHostEmulatedMachines(vds).containsAll(emulatedMachines);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_HOST_DUE_TO_UNSUPPORTED_MACHINE_TYPE.name();
    }

    @Override
    public String getDetailMessage(VDS vds) {
        Set<String> missing = new HashSet<>(emulatedMachines);
        missing.removeAll(getHostEmulatedMachines(vds));
        return StringUtils.join(missing, ",");
    }

    private static Set<String> getClusterEmulatedMachines(Cluster cluster) {
        if (cluster.getEmulatedMachine() == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(Arrays.asList(cluster.getEmulatedMachine().split(",")));
        }
    }

    private static Set<String> getHostEmulatedMachines(VDS vds) {
        if (vds.getSupportedEmulatedMachines() == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(Arrays.asList(vds.getSupportedEmulatedMachines().split(",")));
        }
    }
}
