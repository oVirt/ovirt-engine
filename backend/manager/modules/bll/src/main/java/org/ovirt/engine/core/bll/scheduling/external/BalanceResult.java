package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;


public class BalanceResult extends SchedulerResult {
    private List<Guid> underUtilizedHosts;
    private Guid vmToMigrate = null;
    private Pair<List<Guid>, Guid> balancingData;

    public void addHost(Guid host) {
        if (underUtilizedHosts == null) {
            underUtilizedHosts = new ArrayList<>();
        }

        underUtilizedHosts.add(host);
    }

    public void setVmToMigrate(Guid vm) {
        this.vmToMigrate = vm;
    }


    public Pair<List<Guid>, Guid> getResult() {
        if (balancingData == null) {
            balancingData = new Pair<>(underUtilizedHosts, vmToMigrate);
        }

        return balancingData;
    }
}
