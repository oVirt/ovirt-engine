package org.ovirt.engine.core.bll.scheduling.external;

import org.ovirt.engine.core.compat.Guid;

import java.util.LinkedList;
import java.util.List;

public class FilteringResult extends SchedulerResult {
    private List<Guid> possibleHosts;

    public void addHost(Guid host) {
        if (possibleHosts == null) {
            possibleHosts = new LinkedList<>();
        }

        possibleHosts.add(host);
    }

    public List<Guid> getHosts(){
        return possibleHosts;
    }
}
