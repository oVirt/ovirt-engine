package org.ovirt.engine.core.bll.scheduling.selector;

import java.util.List;
import java.util.Optional;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface SelectorInstance {
    void init(List<Pair<Guid, Integer>> policyUnits, List<Guid> hosts);
    void record(Guid policyUnit, Guid host, Integer weight);
    Optional<Guid> best();
}
