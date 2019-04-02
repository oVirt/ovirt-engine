package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class NbdServerURLReturn extends StatusReturn {
    private static final String STATUS = "status";
    private static final String INFO = "info";

    private Status status;
    private String nbdServerURL;

    @SuppressWarnings("unchecked")
    public NbdServerURLReturn(Map<String, Object> innerMap) {
        super(innerMap);
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        nbdServerURL = (String) innerMap.get(INFO);
    }

    public Status getStatus() {
        return status;
    }

    public String getNBDServerURL() {
        return nbdServerURL;
    }
}
