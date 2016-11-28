package org.ovirt.engine.core.bll.scheduling.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class AffinityHostsResult {

    private List<VDS> acceptableHosts;
    private Map<Guid, Integer> hostViolations;

    public AffinityHostsResult(List<VDS> acceptableHosts) {
        this.acceptableHosts = acceptableHosts;
        this.hostViolations = new HashMap<>();
    }

    public AffinityHostsResult(List<VDS> acceptableHosts, Map<Guid, Integer> hostViolations) {
        this.acceptableHosts = acceptableHosts;
        this.hostViolations = hostViolations;
    }

    public Map<Guid, Integer> getHostViolations() {
        return hostViolations;
    }

    public List<VDS> getAcceptableHosts() {
        return acceptableHosts;
    }

}
