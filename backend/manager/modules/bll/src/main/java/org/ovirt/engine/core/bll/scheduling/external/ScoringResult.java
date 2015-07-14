package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

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
