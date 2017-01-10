package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.ovirt.engine.core.compat.Guid;

public class DomainXmlListReturn {
    private static final String STATUS = "status";
    private static final String DOMXMLS = "domxmls";

    private Status status;
    private Map<Guid, String> domxmls = Collections.emptyMap();

    @SuppressWarnings("unchecked")
    public DomainXmlListReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        Map<String, String> vmIdToDomainXml = (Map<String, String>) innerMap.get(DOMXMLS);
        if (vmIdToDomainXml != null) {
            domxmls = vmIdToDomainXml.entrySet().stream().collect(Collectors.toMap(
                    entry -> new Guid(entry.getKey()),
                    Entry::getValue));
        }
    }

    public Status getStatus() {
        return status;
    }

    public Map<Guid, String> getDomainXmls() {
        return domxmls;
    }
}
