package org.ovirt.engine.core.bll.scheduling.external;

import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class FilteringResult extends SchedulerResult {
    private List<Guid> possibleHosts = new LinkedList<>();

    public void addHost(Guid host) {
        possibleHosts.add(host);
    }

    @NotNull
    public List<Guid> getHosts(){
        return possibleHosts;
    }
}
