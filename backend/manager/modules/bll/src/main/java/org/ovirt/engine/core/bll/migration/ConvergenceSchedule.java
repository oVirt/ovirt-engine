package org.ovirt.engine.core.bll.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.migration.ConvergenceConfig;
import org.ovirt.engine.core.common.migration.ConvergenceItem;
import org.ovirt.engine.core.common.migration.ConvergenceItemWithStallingLimit;
import org.ovirt.engine.core.common.migration.NoConvergenceConfig;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class ConvergenceSchedule {

    private List<ConvergenceItem> init = new ArrayList<>();

    // protected only due to testing
    List<ConvergenceItemWithStallingLimit> stalling = new ArrayList<>();

    public ConvergenceSchedule addInitStep(ConvergenceItem item) {
        init.add(item);

        return this;
    }

    public ConvergenceSchedule addStallingStep(ConvergenceItemWithStallingLimit item) {
        stalling.add(item);

        return this;
    }

    public static ConvergenceSchedule from(ConvergenceConfig config) {
        if (config instanceof NoConvergenceConfig) {
            return new NoConvergenceSchedule();
        }

        ConvergenceSchedule schedule = new ConvergenceSchedule();

        addDowntimes(config, schedule);

        addInit(config, schedule);

        return schedule;
    }

    public static void addInit(ConvergenceConfig config, ConvergenceSchedule schedule) {
        if (config.getInitialItems() == null) {
            return;
        }

        config.getInitialItems().stream().forEachOrdered(schedule::addInitStep);
    }

    public static void addDowntimes(ConvergenceConfig config, ConvergenceSchedule schedule) {
        if (config.getConvergenceItems() != null && !config.getConvergenceItems().isEmpty()) {
            // if the convergence items are set directly then use them
            config.getConvergenceItems().stream().forEachOrdered(schedule::addStallingStep);
        }

        // the last step will be executed when all the other steps have been executed already and the
        // migration is still stalling (-1 means that it is stalling for any amount this step will be executed)
        if (config.getLastItems() != null) {
            config.getLastItems().stream()
                    .map(item -> new ConvergenceItemWithStallingLimit(-1, item))
                    .forEachOrdered(schedule::addStallingStep);
        }
    }

    public Map<String, Object> asMap() {
        Map<String, Object> res = new HashMap<>();
        res.put(VdsProperties.MIGRATION_INIT_STEPS, init.stream().map(item -> item.asMap()).collect(Collectors.toList()));
        res.put(VdsProperties.MIGRATION_STALLING_STEPS, stalling.stream().map(item -> item.asMap()).collect(Collectors.toList()));

        return res;
    }

}
