package org.ovirt.engine.core.bll.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.migration.ConvergenceItem;
import org.ovirt.engine.core.common.migration.ConvergenceItemWithStallingLimit;

public class ConvergenceScheduleTest {
    private ConvergenceSchedule schedule = new ConvergenceSchedule();

    @Test
    public void asMapEmpty() {
        assertEquals(new ExpectedConvergence(), schedule.asMap());
    }

    @Test
    public void asMapOneInit() {
        schedule.addInitStep(new ConvergenceItem("action", 10));

        assertEquals(new ExpectedConvergence().addInit("action", 10), schedule.asMap());
    }

    @Test
    public void asMapTwoInits() {
        schedule.addInitStep(new ConvergenceItem("action", 10))
            .addInitStep(new ConvergenceItem("action2", 15));

        assertEquals(new ExpectedConvergence().addInit("action", 10).addInit("action2", 15), schedule.asMap());
    }

    @Test
    public void asMapOneConvItem() {
        schedule.addStallingStep(new ConvergenceItemWithStallingLimit(8, "action", 10));
        assertEquals(new ExpectedConvergence().addStalling(8, "action", 10), schedule.asMap());
    }

    @Test
    public void asMapTwoConvItems() {
        schedule.addStallingStep(new ConvergenceItemWithStallingLimit(8, "action", 10))
            .addStallingStep(new ConvergenceItemWithStallingLimit(12, "action2", 12));

        assertEquals(new ExpectedConvergence().addStalling(8, "action", 10).addStalling(12, "action2", 12), schedule.asMap());
    }

    @Test
    public void asMapBoth() {
        schedule.addInitStep(new ConvergenceItem("action", 10))
            .addInitStep(new ConvergenceItem("action2", 15))
            .addStallingStep(new ConvergenceItemWithStallingLimit(8, "action", 10))
            .addStallingStep(new ConvergenceItemWithStallingLimit(12, "action2", 12));

        Map<String, Object> expected = new ExpectedConvergence().
                addInit("action", 10).
                addInit("action2", 15).
                addStalling(8, "action", 10).
                addStalling(12, "action2", 12);

        assertEquals(expected, schedule.asMap());
    }

    private class ExpectedConvergence extends HashMap<String, Object> {

        private ExpectedConvergence() {
            put("init", new ArrayList<>());
            put("stalling", new ArrayList<>());
        }

        public ExpectedConvergence addInit(String name, Object... params) {
            ((List<Object>) get("init")).add(action(name, params));
            return this;
        }

        public ExpectedConvergence addStalling(int limit, String name, Object... params) {
            Map<String, Object> withStalling = new HashMap<>();
            withStalling.put("limit", limit);
            withStalling.put("action", action(name, params));
            ((List<Object>) get("stalling")).add(withStalling);
            return this;
        }

        private Map<String, Object> action(String actionName, Object... actionParams) {
            Map<String, Object> action = new HashMap<>();
            action.put("name", actionName);
            action.put("params", Arrays.asList(actionParams));
            return action;
        }

    }
}
