package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public class LeaseTaskInfoReturn extends StatusReturn {

    private static final String TASK_ID_KEY = "info";

    public LeaseTaskInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
    }

    public String getTaskId(){
        return (String) innerMap.get(TASK_ID_KEY);
    }
}
