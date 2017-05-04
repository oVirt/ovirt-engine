package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ScoringResult extends SchedulerResult {
    private List<WeightResultEntry> hostsWithScores = new ArrayList<>();

    public void addHost(String policyUnit, Guid host, Integer score) {
        hostsWithScores.add(new WeightResultEntry(host, score, policyUnit));
    }

    public List<WeightResultEntry> getHosts() {
        return hostsWithScores;
    }
}
