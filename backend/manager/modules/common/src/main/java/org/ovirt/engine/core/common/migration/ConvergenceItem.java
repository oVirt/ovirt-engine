package org.ovirt.engine.core.common.migration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvergenceItem implements Serializable {

    private String action;

    private List<Object> params;

    public ConvergenceItem() {}

    public ConvergenceItem(String action, Object... params) {
        this(action, Arrays.asList(params));
    }

    public ConvergenceItem(String action, List<Object> params) {
        this.action = action;
        this.params = params;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("name", action);
        res.put("params", params);
        return res;
    }
}
