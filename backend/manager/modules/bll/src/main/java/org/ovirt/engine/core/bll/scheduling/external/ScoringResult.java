package org.ovirt.engine.core.bll.scheduling.external;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import java.util.List;

public class ScoringResult extends SchedulerResult {
    private List<Pair<Guid, Integer>> hostsWithScores;

    public void addHost(Guid host, Integer score) {
        if (hostsWithScores == null) {
            hostsWithScores = new ArrayList<>();
        }

        hostsWithScores.add(new Pair<>(host, score));
    }

    public List<Pair<Guid, Integer>> getHosts() {
        return hostsWithScores;
    }
}
