package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryStatus {
    private int totalCount;
    private Map<String, Integer> statuses = new HashMap<>();
    private Map<String, List<String>> statusValues = new HashMap<>();

    public int getTotalCount() {
        return totalCount;
    }

    public List<Status> getStatuses() {
        List<Status> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: statuses.entrySet()) {
            result.add(new Status(entry.getKey(), entry.getValue(), statusValues.get(entry.getKey())));
        }
        return result;
    }

    public void setTotalCount(int count) {
        this.totalCount = count;
    }

    public void addCount() {
        totalCount++;
    }

    public void setStatusCount(String type, int value) {
        statuses.put(type, value);
    }

    public void addStatus(String type) {
        if (statuses.get(type) == null) {
            statuses.put(type, 0);
        }
        int statusCount = statuses.get(type);
        statusCount++;
        statuses.put(type, statusCount);
    }

    public void setStatusValues(String type, List<String> values) {
        statusValues.put(type, values);
    }

    public void resetCounts() {
        totalCount = 0;
        statuses.clear();
    }
}
