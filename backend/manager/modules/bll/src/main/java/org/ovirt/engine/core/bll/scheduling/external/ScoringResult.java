package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ScoringResult extends SchedulerResult {
    private List<WeightResultEntry> hostsWithScores;

    public void addHost(String policyUnit, Guid host, Integer score) {
        if (hostsWithScores == null) {
            hostsWithScores = new ArrayList<>();
        }

        hostsWithScores.add(new WeightResultEntry(host, score, policyUnit));
    }

    public List<WeightResultEntry> getHosts() {
        return hostsWithScores;
    }
}
