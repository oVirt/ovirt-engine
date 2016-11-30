package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class HostJobsReturn extends StatusReturn {
    private static final String JOBS_KEY = "jobs";

    private Map<String, Object> hostJobsInfo;

    @SuppressWarnings("unchecked")
    public HostJobsReturn(Map<String, Object> innerMap) {
        super(innerMap);
        hostJobsInfo = (Map<String, Object>) innerMap.get(JOBS_KEY);
    }

    public Map<String, Object> getHostJobsInfo() {
        return hostJobsInfo;
    }

    public void setHostJobsInfo(Map<String, Object> hostJobsInfo) {
        this.hostJobsInfo = hostJobsInfo;
    }
}
