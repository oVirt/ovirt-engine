package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;


public class BalanceResult extends SchedulerResult {
    private List<Guid> candidateHosts = new ArrayList<>();
    private Guid vmToMigrate = null;
    private Guid currentHost = null;

    public BalanceResult(Guid vmToMigrate, List<Guid> candidateHosts) {
        this.vmToMigrate = vmToMigrate;
        this.candidateHosts = candidateHosts;
    }

    public BalanceResult(Guid vmToMigrate, List<Guid> candidateHosts, Guid currentHost) {
        this(vmToMigrate, candidateHosts);
        this.currentHost = currentHost;
    }

    public BalanceResult() {
    }

    public void addHost(Guid host) {
        candidateHosts.add(host);
    }

    public void setVmToMigrate(Guid vm) {
        this.vmToMigrate = vm;
    }

    public List<Guid> getCandidateHosts() {
        return candidateHosts;
    }

    public Guid getVmToMigrate() {
        return vmToMigrate;
    }

    public boolean isValid() {
        return vmToMigrate != null;
    }

    public Guid getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(Guid currentHost) {
        this.currentHost = currentHost;
    }
}
