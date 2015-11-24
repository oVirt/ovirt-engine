package org.ovirt.engine.core.common.migration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConvergenceItemWithStallingLimit implements Serializable {

    private int stallingLimit;

    private ConvergenceItem convergenceItem;

    public ConvergenceItemWithStallingLimit() {}

    public ConvergenceItemWithStallingLimit(int stallingLimit, String action, Object... params) {
        this(stallingLimit, new ConvergenceItem(action, params));
    }

    public ConvergenceItemWithStallingLimit(int stallingLimit, ConvergenceItem convergenceItem) {
        this.stallingLimit = stallingLimit;
        this.convergenceItem = convergenceItem;
    }

    public int getStallingLimit() {
        return stallingLimit;
    }

    public void setStallingLimit(int stallingLimit) {
        this.stallingLimit = stallingLimit;
    }

    public ConvergenceItem getConvergenceItem() {
        return convergenceItem;
    }

    public void setConvergenceItem(ConvergenceItem convergenceItem) {
        this.convergenceItem = convergenceItem;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("limit", stallingLimit);
        res.put("action", convergenceItem.asMap());

        return res;
    }
}
